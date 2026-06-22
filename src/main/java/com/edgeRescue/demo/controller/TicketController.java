package com.edgeRescue.demo.controller;

import com.edgeRescue.demo.engine.RoutingEngine;
import com.edgeRescue.demo.engine.TriageAiService;
import com.edgeRescue.demo.model.EmergencyTicket;
import com.edgeRescue.demo.model.TriageResponse;
import com.edgeRescue.demo.repository.TicketRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*") // Allows your frontends to talk to this API without security blocks
public class TicketController {

    private final TriageAiService aiService;
    private final TicketRepository ticketRepository;
    private final RoutingEngine routingEngine;



    public TicketController(TriageAiService aiService, TicketRepository ticketRepository, RoutingEngine routingEngine) {
        this.aiService = aiService;
        this.ticketRepository = ticketRepository;
        this.routingEngine = routingEngine;
    }


    /**
     * Endpoint 1: For the Citizen Portal. Accepts raw distress text, sends it to Ollama,
     * structures it, persists it, and returns the result.
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitTicket(@RequestBody Map<String, Object> payload) {
        try {
            // Safely extract message string
            Object messageObj = payload == null ? null : payload.get("message");
            String rawMessage = messageObj == null ? "" : messageObj.toString();
            rawMessage = rawMessage == null ? "" : rawMessage.trim();

            // Safely extract latitude/longitude as doubles (supports number or string)
            double lat = 0.0;
            double lon = 0.0;
            try {
                lat = parseCoordinate(payload == null ? null : payload.get("latitude"));
                lon = parseCoordinate(payload == null ? null : payload.get("longitude"));
            } catch (Exception ignored) {
                lat = 0.0;
                lon = 0.0;
            }

            // Validate message presence
            if (rawMessage.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "BAD_REQUEST",
                        "error", "Emergency message text cannot be blank",
                        "message", "Please provide a non-empty 'message' field"
                ));
            }

            // Pass the message through AI parser
            TriageResponse triage = aiService.parseEmergency(rawMessage);

            // Manually instantiate and set EmergencyTicket fields
            EmergencyTicket ticket = new EmergencyTicket(rawMessage, triage);
            ticket.setLatitude(lat);
            ticket.setLongitude(lon);
            ticket.setStatus("OPEN");

            // Apply structural region routing conditions (BANGALORE / KERALA / GLOBAL)
            String region;
            if (lat >= 12.0 && lat <= 14.0) {
                region = "BANGALORE";
            } else if (lat >= 8.0 && lat <= 11.5) {
                region = "KERALA";
            } else {
                region = resolveRegion(lat, triage);
            }
            ticket.setRegion(region);

            EmergencyTicket saved = ticketRepository.save(ticket);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace(); // log so Render logs capture it
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "INTERNAL_ERROR",
                            "error", "Internal Server Processing Failure",
                            "message", e.getMessage() == null ? "Unexpected server error" : e.getMessage()
                    ));
        }
    }


    /**
     * Endpoint 2: Allows a field responder to claim a ticket, preventing resource conflicts.
     */
    @PostMapping("/{id}/claim")
    public EmergencyTicket claimTicket(@PathVariable String id, @RequestParam String workerName) {
        EmergencyTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Ticket not found: " + id));
        ticket.setStatus("CLAIMED");
        ticket.setAssignedWorker(workerName);
        return ticketRepository.save(ticket);
    }

    /**
     * Resolves the region for a ticket based on coordinates and AI-parsed text.
     */
    private String resolveRegion(double lat, TriageResponse triage) {
        // Priority 1: Coordinate-based region detection
        if (lat >= 12.0 && lat <= 14.0) {
            return "BANGALORE";
        }
        if (lat >= 8.0 && lat <= 11.5) {
            return "KERALA";
        }

        // Priority 2: Fallback to AI-parsed text (summary / category)
        String haystack = (triage.getSummary() + " " + triage.getCategory()).toUpperCase();

        // Known Indian metro cities / regions
        String[] knownRegions = {
            "MUMBAI", "DELHI", "CHENNAI", "KOLKATA", "HYDERABAD",
            "BANGALORE", "KERALA", "PUNE", "AHMEDABAD", "JAIPUR",
            "LUCKNOW", "PATNA", "BHUBANESWAR", "GUWAHATI", "CHANDIGARH"
        };

        for (String known : knownRegions) {
            if (haystack.contains(known)) {
                return known;
            }
        }

        // Priority 3: Default
        return "GLOBAL";
    }

    /**
     * Endpoint 2: For the Command Center Dashboard.
     * Returns tickets (optionally filtered) sorted from highest threat to lowest threat.
     */
    @GetMapping("/live")
    public List<EmergencyTicket> getLiveQueue(
            @RequestParam(name = "priority", required = false) String priority,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "search", required = false) String search
    ) {
        return ticketRepository.findSortedFiltered(normalize(priority), normalize(category), normalize(search));
    }

    /**
     * Endpoint 3: Allows operators to resolve and remove a ticket once emergency teams handle it.
     */
    @DeleteMapping("/{id}/resolve")
    public Map<String, Boolean> resolveTicket(@PathVariable String id) {
        boolean exists = ticketRepository.existsById(id);
        if (exists) {
            ticketRepository.deleteById(id);
        }
        return Map.of("success", exists);
    }

    @GetMapping("/stats")
public Map<String, Object> getQueueStats() {
    // FORCE the stats to read directly from your live H2 Database Repository
    List<EmergencyTicket> allTickets = ticketRepository.findAll(); 
    
    long totalActive = allTickets.size();
    long criticalCount = allTickets.stream().filter(t -> "CRITICAL".equalsIgnoreCase(t.getPriority())).count();
    long mediumCount = allTickets.stream().filter(t -> "MEDIUM".equalsIgnoreCase(t.getPriority())).count();
    long lowCount = allTickets.stream().filter(t -> "LOW".equalsIgnoreCase(t.getPriority())).count();
    
    long medicalCount = allTickets.stream().filter(t -> "MEDICAL".equalsIgnoreCase(t.getCategory())).count();
    long floodCount = allTickets.stream().filter(t -> "FLOOD".equalsIgnoreCase(t.getCategory())).count();
    long rescueCount = allTickets.stream().filter(t -> "RESCUE".equalsIgnoreCase(t.getCategory())).count();

    return Map.of(
        "totalActive", totalActive,
        "critical", criticalCount,
        "medium", mediumCount,
        "low", lowCount,
        "medical", medicalCount,
        "flood", floodCount,
        "rescue", rescueCount
    );
}

    private String normalize(String v) {
        if (v == null) return null;
        String trimmed = v.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private double parseCoordinate(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }
}

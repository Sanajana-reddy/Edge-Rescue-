package com.edgeRescue.demo.controller;

import com.edgeRescue.demo.engine.RoutingEngine;
import com.edgeRescue.demo.engine.TriageAiService;
import com.edgeRescue.demo.model.EmergencyTicket;
import com.edgeRescue.demo.model.TriageResponse;
import com.edgeRescue.demo.repository.TicketRepository;

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
    public EmergencyTicket submitTicket(@RequestBody Map<String, Object> payload) {
        String rawMessage = (String) payload.get("message");
        double lat = parseCoordinate(payload.get("latitude"));
        double lon = parseCoordinate(payload.get("longitude"));

        TriageResponse triage = aiService.parseEmergency(rawMessage);
        EmergencyTicket newTicket = new EmergencyTicket(rawMessage, triage);

        newTicket.setLatitude(lat);
        newTicket.setLongitude(lon);

        ticketRepository.save(newTicket);
        return newTicket;
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


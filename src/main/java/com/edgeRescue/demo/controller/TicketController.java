package com.edgeRescue.demo.controller;

import com.edgeRescue.demo.engine.RoutingEngine;
import com.edgeRescue.demo.engine.TriageAiService;
import com.edgeRescue.demo.model.EmergencyTicket;
import com.edgeRescue.demo.model.TriageResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*") // Allows your frontends (Lovable/Replit) to talk to this API without security blocks
public class TicketController {

    private final TriageAiService aiService;
    private final RoutingEngine routingEngine;

    public TicketController(TriageAiService aiService, RoutingEngine routingEngine) {
        this.aiService = aiService;
        this.routingEngine = routingEngine;
    }

    /**
     * Endpoint 1: For the Citizen Portal. Accepts raw distress text, sends it to Ollama,
     * structures it, places it into our sorted queue, and returns the result.
     */
    @PostMapping("/submit")
    public EmergencyTicket submitTicket(@RequestBody Map<String, String> payload) {
        String rawMessage = payload.get("message");
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        // 1. Analyze with our background Ollama AI
        TriageResponse triage = aiService.parseEmergency(rawMessage);

        // 2. Create the ticket and add it to the priority sorting engine
        EmergencyTicket newTicket = new EmergencyTicket(rawMessage, triage);
        routingEngine.addTicket(newTicket);

        return newTicket;
    }

    /**
     * Endpoint 2: For the Command Center Dashboard. Returns the live list of all 
     * active emergency tickets, strictly sorted from highest threat to lowest threat.
     */
    @GetMapping("/live")
    public List<EmergencyTicket> getLiveQueue() {
        return routingEngine.getAllSortedTickets();
    }

    /**
     * Endpoint 3: Allows operators to clear out a ticket once emergency teams resolve it.
     */
    @DeleteMapping("/{id}/resolve")
    public Map<String, Boolean> resolveTicket(@PathVariable String id) {
        boolean removed = routingEngine.resolveTicket(id);
        return Map.of("success", removed);
    }
}
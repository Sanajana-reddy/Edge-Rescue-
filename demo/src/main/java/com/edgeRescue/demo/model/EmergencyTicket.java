package com.edgeRescue.demo.model;

import java.time.Instant;
import java.util.UUID;

public class EmergencyTicket implements Comparable<EmergencyTicket> {
    private String id;
    private String rawMessage;
    private String priority;  
    private String category;
    private String summary;
    private Instant timestamp;

    public EmergencyTicket(String rawMessage, TriageResponse triage) {
        this.id = UUID.randomUUID().toString();
        this.rawMessage = rawMessage;
        this.priority = triage.getPriority().toUpperCase();
        this.category = triage.getCategory();
        this.summary = triage.getSummary();
        this.timestamp = Instant.now();
    }

    private int getPriorityWeight() {
        switch (this.priority) {
            case "CRITICAL": return 3;
            case "MEDIUM": return 2;
            case "LOW": return 1;
            default: return 0;
        }
    }

    @Override
    public int compareTo(EmergencyTicket other) {
        int weightCompare = Integer.compare(other.getPriorityWeight(), this.getPriorityWeight());
        if (weightCompare != 0) {
            return weightCompare;
        }
        return this.timestamp.compareTo(other.timestamp);
    }

    public String getId() { return id; }
    public String getRawMessage() { return rawMessage; }
    public String getPriority() { return priority; }
    public String getCategory() { return category; }
    public String getSummary() { return summary; }
    public Instant getTimestamp() { return timestamp; }
}
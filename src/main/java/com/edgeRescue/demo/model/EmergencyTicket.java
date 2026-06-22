package com.edgeRescue.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "emergency_tickets")
public class EmergencyTicket implements Comparable<EmergencyTicket> {

    @Id
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @Column(name = "raw_message", columnDefinition = "TEXT", nullable = false)
    private String rawMessage;

    @Column(length = 16, nullable = false)
    private String priority;  // CRITICAL, MEDIUM, LOW

    @Column(length = 32, nullable = false)
    private String category;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Column(name = "created_at", nullable = false)
    private Instant timestamp;

    @Column(columnDefinition = "DOUBLE DEFAULT 0 NOT NULL")
    private double latitude = 0.0;

    @Column(columnDefinition = "DOUBLE DEFAULT 0 NOT NULL")
    private double longitude = 0.0;

    public double getLatitude() { return latitude; }
    public void setLatitude(double lat) { this.latitude = lat; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double lon) { this.longitude = lon; }

    /** Required by JPA */
    protected EmergencyTicket() {
    }

    public EmergencyTicket(String rawMessage, TriageResponse triage) {
        this.id = UUID.randomUUID().toString();
        this.rawMessage = rawMessage;
        this.priority = triage.getPriority().toUpperCase();
        this.category = triage.getCategory();
        this.summary = triage.getSummary();
        this.timestamp = Instant.now();
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    private int getPriorityWeight() {
        return switch (this.priority) {
            case "CRITICAL" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
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

    public void setPriority(String priority) { 
        this.priority = priority; 
    }

    public void appendToSummary(String suffix) {
        if (suffix == null || suffix.isBlank()) {
            return;
        }
        if (this.summary == null) {
            this.summary = suffix;
            return;
        }
        this.summary = this.summary + suffix;
    }




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmergencyTicket that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


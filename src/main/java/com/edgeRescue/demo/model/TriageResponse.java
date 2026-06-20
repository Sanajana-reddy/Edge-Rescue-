package com.edgeRescue.demo.model;

public class TriageResponse {
    private String priority; // CRITICAL, MEDIUM, LOW
    private String category; // MEDICAL, FLOOD, RESCUE, BLOCKAGE, OTHER
    private String summary;

    public TriageResponse() {}

    public TriageResponse(String priority, String category, String summary) {
        this.priority = priority;
        this.category = category;
        this.summary = summary;
    }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
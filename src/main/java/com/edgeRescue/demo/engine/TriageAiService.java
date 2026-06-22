package com.edgeRescue.demo.engine;

import com.edgeRescue.demo.model.TriageResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class TriageAiService {

    private final ChatClient chatClient;
    private final boolean isCloudEnvironment;

    // Spring AI automatically configures and injects this Builder based on application.properties
    public TriageAiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        // Render automatically sets the environment variable RENDER=true
        this.isCloudEnvironment = System.getenv("RENDER") != null || "true".equalsIgnoreCase(System.getenv("IS_CLOUD"));
    }

    public TriageResponse parseEmergency(String rawMessage) {
        // 🛡️ CRITICAL FIX: If running on Render, completely skip Spring AI to prevent 502 loop timeouts
        if (isCloudEnvironment) {
            System.out.println("☁️ Render Environment Detected: Bypassing Ollama network calls to use local rules engine.");
            return executeDeterministicFallback(rawMessage);
        }

        String systemPrompt = """
            You are a disaster response triage AI operating during critical monsoons.
            Analyze the user's distress message. Extract and return exactly three values in this precise format:
            PRIORITY: [Choose only from: CRITICAL, MEDIUM, LOW]
            CATEGORY: [Choose only from: MEDICAL, FLOOD, RESCUE, BLOCKAGE, OTHER]
            SUMMARY: [A brief 1-sentence plain-text clean summary of the danger]
            
            Do not include any greetings, markdown, or extra text.
            """;

        try {
            String aiRawResult = chatClient.prompt()
                    .system(systemPrompt)
                    .user(rawMessage)
                    .call()
                    .content();

            return cleanAndParseResponse(aiRawResult);
        } catch (Exception e) {
            System.out.println("⚠️ Local Ollama call failed. Executing fallback rules.");
            return executeDeterministicFallback(rawMessage);
        }
    }

    /**
     * Reusable, high-speed fallback processing method used both for cloud environments 
     * and local system fallback failures.
     */
    private TriageResponse executeDeterministicFallback(String rawMessage) {
        String msgLower = rawMessage == null ? "" : rawMessage.toLowerCase();

        // FLOOD rules
        if (containsAny(msgLower, "flood", "water", "submerged", "river")) {
            boolean trappedOrDrowning = containsAny(msgLower, "trapped", "drowning", "roof");
            String priority = trappedOrDrowning ? "CRITICAL" : "MEDIUM";
            String summary = trappedOrDrowning
                    ? "Possible flood victims are trapped or drowning. Immediate rescue is required."
                    : "Potential flood or submerged danger reported. Dispatch assistance and monitor closely.";
            return new TriageResponse(priority, "FLOOD", summary);
        }

        // MEDICAL rules
        if (containsAny(msgLower, "injury", "blood", "accident", "medical", "bleeding")) {
            boolean unconsciousOrSevere = containsAny(msgLower, "unconscious", "severe", "dying");
            String priority = unconsciousOrSevere ? "CRITICAL" : "MEDIUM";
            String summary = unconsciousOrSevere
                    ? "Reported medical emergency with possible unconscious/severe injuries. Urgent medical response needed."
                    : "Reported injury/medical incident. Send medical help and assess severity.";
            return new TriageResponse(priority, "MEDICAL", summary);
        }

        // RESCUE rules
        if (containsAny(msgLower, "rescue", "trapped", "stuck", "landslide")) {
            String summary = "People may be trapped or require rescue due to ongoing danger. Immediate rescue team deployment recommended.";
            return new TriageResponse("CRITICAL", "RESCUE", summary);
        }

        // Default OTHER rules
        return new TriageResponse("LOW", "OTHER", "[Cloud Engine] " + snippet(rawMessage));
    }

    private boolean containsAny(String msgLower, String... keywords) {
        if (msgLower == null) {
            return false;
        }
        for (String k : keywords) {
            if (k != null && msgLower.contains(k.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String snippet(String rawMessage) {
        String safe = rawMessage == null ? "" : rawMessage.trim();
        if (safe.isEmpty()) {
            return "No message content provided.";
        }
        int maxLen = 160;
        return safe.length() <= maxLen ? safe : safe.substring(0, maxLen).trim() + "...";
    }

    private TriageResponse cleanAndParseResponse(String rawText) {
        TriageResponse response = new TriageResponse("MEDIUM", "OTHER", "Emergency logged.");
        try {
            String[] lines = rawText.split("\\n");
            for (String line : lines) {
                if (line.toUpperCase().startsWith("PRIORITY:")) {
                    response.setPriority(line.substring(9).trim().toUpperCase());
                } else if (line.toUpperCase().startsWith("CATEGORY:")) {
                    response.setCategory(line.substring(9).trim().toUpperCase());
                } else if (line.toUpperCase().startsWith("SUMMARY:")) {
                    response.setSummary(line.substring(8).trim());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed parsing LLM text, using fallbacks: " + rawText);
        }
        return response;
    }
}
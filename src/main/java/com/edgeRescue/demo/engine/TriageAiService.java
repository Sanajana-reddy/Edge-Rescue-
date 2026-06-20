package com.edgeRescue.demo.engine;

import com.edgeRescue.demo.model.TriageResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class TriageAiService {

    private final ChatClient chatClient;

    // Spring AI automatically configures and injects this Builder based on application.properties
    public TriageAiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public TriageResponse parseEmergency(String rawMessage) {
        String systemPrompt = """
            You are a disaster response triage AI operating during critical monsoons.
            Analyze the user's distress message. Extract and return exactly three values in this precise format:
            PRIORITY: [Choose only from: CRITICAL, MEDIUM, LOW]
            CATEGORY: [Choose only from: MEDICAL, FLOOD, RESCUE, BLOCKAGE, OTHER]
            SUMMARY: [A brief 1-sentence plain-text clean summary of the danger]
            
            Do not include any greetings, markdown, or extra text.
            """;

        String aiRawResult = chatClient.prompt()
                .system(systemPrompt)
                .user(rawMessage)
                .call()
                .content();

        return cleanAndParseResponse(aiRawResult);
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
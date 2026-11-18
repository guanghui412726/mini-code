package com.minicode.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.Map;

public final class ToolCallHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ToolCallHelper() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> arguments(AssistantMessage.ToolCall call) {
        if (call == null) {
            return Map.of();
        }
        String argumentsJson = call.arguments();
        if (argumentsJson.isBlank()) {
            return Map.of();
        }
        try {
            return MAPPER.readValue(argumentsJson, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }
}

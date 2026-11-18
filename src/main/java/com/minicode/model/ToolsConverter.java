package com.minicode.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minicode.tool.Tool;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class ToolsConverter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ToolsConverter() {
    }

    public static List<ToolCallback> toToolsCallbacks(Collection<Tool> tools) {
        List<ToolCallback> callbacks = new ArrayList<>();
        for (Tool tool : tools) {
            ToolDefinition def = ToolDefinition.builder()
                    .name(tool.name())
                    .description(tool.description())
                    .inputSchema(toJsonString(tool.paramsSchema()))
                    .build();
            callbacks.add(new ToolCallback() {
                @Override
                public ToolDefinition getToolDefinition() {
                    return def;
                }

                @Override
                public @NonNull String call(@NonNull String toolInput) {
                    throw new UnsupportedOperationException(
                            "工具" + def.name() + " 由mini code 控制循环执行");
                }
            });
        }
        return callbacks;
    }

    private static String toJsonString(Map<String, Object> schema) {
        try {
            return MAPPER.writeValueAsString(schema);
        } catch (Exception e) {
            return "{\"type\":\"object\",\"properties\":{}}";
        }
    }
}

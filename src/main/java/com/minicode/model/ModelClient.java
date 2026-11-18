package com.minicode.model;

import com.minicode.config.MccConfig;
import com.minicode.tool.Tool;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ModelClient {

    private final ChatModel chatModel;
    private final MccConfig config;

    public ModelClient(MccConfig config) {
        this(buildChatModel(config), config);
    }

    public ModelClient(ChatModel chatModel, MccConfig config) {
        this.chatModel = chatModel;
        this.config = config;
    }

    public AssistantMessage call(List<Message> messages, Collection<Tool> tools) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(config.model())
                .toolCallbacks(ToolsConverter.toToolsCallbacks(tools))
                .build();
        ChatResponse resp = chatModel.call(new Prompt(messages, options));
        return Objects.requireNonNull(resp.getResult()).getOutput();

    }

    private static ChatModel buildChatModel(MccConfig config) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .baseUrl(config.baseUrl())
                .apiKey(config.apiKey())
                .model(config.model())
                .build();
        return OpenAiChatModel.builder().options(options).build();
    }
}

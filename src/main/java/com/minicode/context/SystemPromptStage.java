package com.minicode.context;

import com.minicode.session.Session;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;

import java.util.List;

/**
 * 系统提示词。系统提示词一般都放第一个
 */
public class SystemPromptStage implements AssembleStage {

    private final String systemPrompt;

    public SystemPromptStage(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    @Override
    public void contribute(List<Message> messages, Session session) {
        messages.add(new SystemMessage(systemPrompt));
    }
}

package com.minicode.context;

import com.minicode.session.Session;
import org.springframework.ai.chat.messages.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * 上下文装配器
 */
public class ContextAssembler {

    private final List<AssembleStage> stages;

    public ContextAssembler(List<AssembleStage> stages) {
        this.stages = stages;
    }

    public List<Message> assemble(Session session) {
        List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
        for (AssembleStage stage : stages) {
            stage.contribute(messages, session);
        }
        return messages;
    }
}

package com.minicode.session;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 会话
 */
public class Session {

    /**
     * 本地会话绑定的项目目录
     */
    private final Path workingDir;

    /**
     * 按照时间排序好的消息历史
     */
    private final List<Message> history = new ArrayList<>();

    public Session(Path workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * 追加用户信息
     * @param text
     */
    public void appendUserMessage(String text) {
        history.add(new UserMessage(text));
    }

    /**
     * 追加这一轮返回的assistant 消息
     * @param message
     */
    public void appendAssistantMessage(AssistantMessage message) {
        history.add(message);
    }

    /**
     * 只读的返回消息历史
     * @return
     */
    public List<Message> history() {
        return List.copyOf(history);
    }
}

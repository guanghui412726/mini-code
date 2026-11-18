package com.minicode.context;

import com.minicode.session.Session;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

public interface AssembleStage {

    /**
     * 将本阶段处理的结果追加到消息列表中
     * @param messages
     * @param session
     */
    void contribute(List<Message> messages, Session session);
}

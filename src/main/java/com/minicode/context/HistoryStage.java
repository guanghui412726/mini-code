package com.minicode.context;

import com.minicode.session.Session;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * 会话历史。按照时间顺序排好的消息，直接追加进去
 */
public class HistoryStage implements AssembleStage {

    @Override
    public void contribute(List<Message> messages, Session session) {
        messages.addAll(session.history());
    }
}

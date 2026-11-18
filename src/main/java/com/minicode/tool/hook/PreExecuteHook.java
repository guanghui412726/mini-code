package com.minicode.tool.hook;

import com.minicode.tool.Tool;
import com.minicode.tool.ToolContext;
import org.springframework.ai.chat.messages.AssistantMessage;

/**
 * 执行前钩子，类似于Spring的Filter
 */
public interface PreExecuteHook {

    /**
     * 工具执行前判断是否放行
     * @param call
     * @param tool
     * @param ctx
     * @return
     */
    Decision beforeExecute(AssistantMessage.ToolCall call, Tool tool, ToolContext ctx);

    record Decision(boolean allowed, String reason) {
        public static Decision allow() {
            return new Decision(true, null);
        }

        public static Decision deny(String reason) {
            return new Decision(false, reason);
        }
    }
}


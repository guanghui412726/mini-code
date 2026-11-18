package com.minicode.tool.hook;

import com.minicode.tool.Tool;
import com.minicode.tool.ToolContext;
import org.springframework.ai.chat.messages.AssistantMessage;

/**
 * 钩子执行链.工具执行前可能有多个钩子，按照钩子的顺序判断
 */
public class HookChain {

    public PreExecuteHook.Decision beforeExecute(AssistantMessage.ToolCall call, Tool tool, ToolContext ctx) {
        // TODO
        return null;
    }
}

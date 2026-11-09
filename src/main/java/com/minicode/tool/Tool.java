package com.minicode.tool;

import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.Map;

public interface Tool {

    /**
     * 工具名称
     * @return
     */
    String name();

    /**
     * 工具描述
     * @return
     */
    String description();

    /**
     * 工具参数的json shcema
     * @return
     */
    Map<String, Object> paramsSchema();

    /**
     * 是否只读。只读工具不会产生副作用，无需审批
     * @return
     */
    boolean isReadOnly();

    /**
     * 是否可并行执行.只读工具可以并行
     * @return
     */
    boolean isConcurrentSafe();

    /**
     * 执行工具调用，返回结果
     * @param call
     * @return
     */
    ToolResult execute(AssistantMessage.ToolCall call, ToolContext context);
}

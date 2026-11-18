package com.minicode.agent;

import com.minicode.cli.Renderer;
import com.minicode.config.MccConfig;
import com.minicode.context.ContextAssembler;
import com.minicode.model.ModelClient;
import com.minicode.session.Session;
import com.minicode.tool.Tool;
import com.minicode.tool.ToolRegistry;
import com.minicode.tool.ToolResult;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent循环控制中心
 */
public class AgentLoop {

    private final MccConfig config;
    private final ContextAssembler assembler;
    private final ModelClient modelClient;
    private final ToolRegistry registry;
    private final Renderer renderer;

    public AgentLoop(MccConfig config, ModelClient modelClient, ContextAssembler assembler,
                     ToolRegistry registry, Renderer renderer) {
        this.config = config;
        this.modelClient = modelClient;
        this.assembler = assembler;
        this.registry = registry;
        this.renderer = renderer;
    }

    public void run(Session session) {
        int step = 0;
        while (true) {
            // 边界控制: 超过最大步数就停，防止模型在工具间死循环
            if (step++ > config.matSteps()) {
                renderer.warn("已达到最大步数上限，停止本轮任务");
            }

            // 组装上下文.涉及到上下文的压缩等
            List<Message> messages = assembler.assemble(session);
            // 执行模型的调用
            AssistantMessage assistantMessage = modelClient.call(messages, registry.all());
            session.appendAssistantMessage(assistantMessage);
            // 获取AI消息的toolCalls
            List<AssistantMessage.ToolCall> calls = assistantMessage.getToolCalls();
            // 没有工具调用直接输出结果
            if (CollectionUtils.isEmpty(calls)) {
                renderReasoning(assistantMessage);
                renderer.finalAnswer(assistantMessage.getText());
                return;
            }

            // 思维链展示
            renderReasoning(assistantMessage);

            // 如果有工具调用，需要先将工具调用文本展示出来
            if (assistantMessage.getText() != null && !assistantMessage.getText().isBlank()) {
                renderer.thought(assistantMessage.getText());
            }

            // 有工具调用，逐个执行并把结果作为观察回填
            for (AssistantMessage.ToolCall call : calls) {
                renderer.toolUse(call);
                Tool tool = registry.find(call.name()).orElse(null);
                if (tool == null) {
                    ToolResult unknown = ToolResult.error("未知工具: " + call.name());
                    session.appendToolResult(call, unknown);
                    continue;
                }
                // 执行前钩子 TODO
                // 执行工具
                ToolResult result = ToolResult.ok("成功");
                renderer.toolResult(result);
                session.appendToolResult(call, result);
            }
        }
    }

    // 把思考模式的思维链打到终端。没有则跳过。
    private void renderReasoning(AssistantMessage message) {
        Object reasoning = message.getMetadata().get("reasoningContent");
        if (reasoning instanceof String text) {
            renderer.reasoning(text);
        }
    }

}

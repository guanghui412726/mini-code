package com.minicode.cli;

import com.minicode.tool.ToolCallHelper;
import com.minicode.tool.ToolResult;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 终端渲染器：把 Agent 的每一步渲染成终端文本。
 *
 * <p>约定：行首 ⏺ 表示一步动作、⎿ 表示该步结果，工具调用写成 工具名(参数)。
 * M1 全是只读工具，直接执行，没有审批卡片。
 */
public class Renderer {

    private static final String DOT = "⏺";
    private static final String BRANCH = "  ⎿ "; // 结果首行的分支标记
    private static final String INDENT = "    "; // 结果后续行的对齐缩进（与首行 ⎿ 之后对齐）

    // ANSI 颜色：把"工具调用过程"整体调暗（灰），把"最终答复"用高亮绿色加粗，
    // 让用户一眼区分"中间过程"与"给我的结果"。设置环境变量 NO_COLOR 可关闭上色。
    private static final boolean COLOR = System.getenv("NO_COLOR") == null;
    private static final String RESET = "\u001B[0m";
    private static final String DIM = "\u001B[2m";          // 暗淡（工具过程）
    private static final String GREEN_BOLD = "\u001B[1;32m"; // 绿色加粗（最终答复标记）
    private static final String BOLD = "\u001B[1m";          // 加粗（最终答复正文）

    private static String dim(String s) {
        return COLOR ? DIM + s + RESET : s;
    }

    private static String finalMark() {
        return COLOR ? GREEN_BOLD + DOT + RESET : DOT;
    }

    private static String bold(String s) {
        return COLOR ? BOLD + s + RESET : s;
    }

    /** 打印启动横幅。 */
    public void banner(String version, String model, String projectDir) {
        System.out.println("mini Claude Code " + version + "  ·  模型：" + model + "  ·  项目：" + projectDir);
        System.out.println("输入你的需求，/help 查看命令，/exit 退出。");
        System.out.println();
    }

    /** 打印输入提示符。 */
    public void prompt() {
        System.out.print("> ");
        System.out.flush();
    }

    /**
     * 渲染思考模式的思维链（{@code reasoning_content}）。中间过程，调暗显示，和正文、最终答复分开。
     * 空白则不输出。
     */
    public void reasoning(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        String[] lines = text.strip().split("\n", -1);
        if (lines.length == 1) {
            System.out.println(dim(DOT + " 思考：" + lines[0]));
            return;
        }
        System.out.println(dim(DOT + " 思考"));
        for (String line : lines) {
            System.out.println(dim(INDENT + line));
        }
    }

    /** 渲染模型在调用工具前的说明性文字（属于中间过程，调暗显示）。 */
    public void thought(String text) {
        System.out.println(dim(DOT + " " + text.strip()));
    }

    public void toolUse(AssistantMessage.ToolCall call) {
        System.out.println(dim(DOT + " " + call.name() + "(" + summarizeArgs(ToolCallHelper.arguments(call)) + ")"));
    }

    /**
     * 渲染一次工具结果：仅首行带 ⎿ 分支标记，后续行用空格对齐缩进。
     * 中间过程，调暗显示；只显示前几行，避免刷屏。
     */
    public void toolResult(ToolResult result) {
        // 只去尾部空白：保留首行前导空格（如 cat -n 的右对齐行号），否则首行会与后续行错位。
        String content = result.content() == null ? "" : result.content().stripTrailing();
        String[] lines = content.split("\n", -1);
        int show = Math.min(lines.length, 6);
        for (int i = 0; i < show; i++) {
            String prefix = (i == 0) ? BRANCH : INDENT;
            System.out.println(dim(prefix + lines[i]));
        }
        if (lines.length > show) {
            System.out.println(dim(INDENT + "…（共 " + lines.length + " 行）"));
        }
    }

    /**
     * 渲染最终答复。与中间过程（调暗的工具调用）形成对比：
     * 上下留空行、绿色加粗标记、正文加粗，让用户一眼看出"这是给我的结果"。
     */
    public void finalAnswer(String text) {
        System.out.println();
        System.out.println(finalMark() + " " + bold(text == null ? "" : text.strip()));
        System.out.println();
    }

    /**
     * 渲染"已中断"（M3 Ctrl-C 取消）。只简短打印一行。
     * 取消不是错误：会话历史、待办、已应用改动都保留，用户可调整需求继续。
     */
    public void interrupted() {
        System.out.println(dim(BRANCH + "Interrupted by user（已中断）"));
        System.out.println();
    }

    /** 渲染警告（如达到步数上限）。 */
    public void warn(String message) {
        System.out.println("⚠ " + message);
        System.out.println();
    }

    /** 渲染错误。 */
    public void error(String message) {
        System.out.println("✗ " + message);
        System.out.println();
    }

    /** 打印一行普通信息（供斜杠命令使用）。 */
    public void info(String message) {
        System.out.println(message);
    }

    /**
     * 渲染「已进入 Plan Mode」提示（M8）：由 {@code /plan} 命令进入计划态后调用。
     * 行首用 ⏸ 表示会话被切进「只调研、不修改」的计划态。
     *
     * <p>（模型 {@code EnterPlanMode} 进入、{@code ExitPlanMode} 退出走的是「工具返回观察」那条路，
     * 提示文字已由工具结果渲染，不走这里。）
     */
    public void planModeEntered() {
        System.out.println("⏸ 已进入 Plan Mode：只调研与规划，不做任何修改。");
    }

    /**
     * 渲染「已保存记忆」提示（M5）：主 Agent 把一条记忆写进记忆目录后，给人一行轻量回执。
     * 调暗显示，属于中间过程。记忆写入不弹审批卡片，这行提示让用户知道它确实记了。
     */
    public void memorySaved(String fileName) {
        System.out.println(dim(DOT + " 已保存记忆：" + fileName));
    }

    /** 渲染一条记忆系统的旁路提示（M5，如轮末补充提取的结果）。调暗显示。 */
    public void memoryNote(String message) {
        System.out.println(dim(BRANCH + message));
    }

    /**
     * 渲染「正在补充提取记忆」的标题行（M5）。
     *
     * <p>轮末提取是一次<b>同步</b>的模型调用，会占住几秒；而这期间 Repl 还没回到
     * {@code readLine("> ")}，用户在终端的观感就是「答复完了但 {@code >} 迟迟不出现」。
     * 先打一行提示，把这段静默期明确成「正在做某事」，避免误以为卡死。
     * 写法与 M6 的 {@link #compactionStart()} 同构（⏺ 开头、调暗、属于中间过程）。
     */
    public void memoryExtractionStart() {
        System.out.println(dim(DOT + " 正在整理记忆…"));
    }

    /**
     * 渲染「记忆整理完成」的收尾行（M5）。
     *
     * <p>与 {@link #memoryExtractionStart()} 成对出现：开头那行只说明「开始做事」，
     * 这行才告诉用户「这段静默期结束了」，可以接着输入下一条。
     * 只有提取回合正常跑完才会打印；提取失败走 {@link #memoryNote(String)} 的「已跳过」分支。
     */
    public void memoryExtractionEnd() {
        System.out.println(dim(DOT + " 本轮对话的记忆已经整理完成。"));
    }

    /**
     * 渲染"开始压缩上下文"的标题行（M6）。
     *
     * <p>真实工具通常不展示压缩过程；mini-cc 为教学刻意把它打出来，
     * 方便学员理解每一层在做什么。
     */
    public void compactionStart() {
        System.out.println(dim(DOT + " 正在压缩上下文…"));
    }

    /** 渲染压缩链里某一层做了什么（M6，如"Snip：清理空结果…（X → Y Token）"）。调暗显示。 */
    public void compactionNote(String message) {
        System.out.println(dim(BRANCH + message));
    }

    /** 渲染压缩前后的总 Token 变化（M6，如"压缩后：182k → 96k Token（估算）"）。 */
    public void compactionSummary(int beforeTokens, int afterTokens) {
        System.out.println(dim(BRANCH + "压缩后：" + formatTokens(beforeTokens)
                + " → " + formatTokens(afterTokens) + " Token（估算）"));
        System.out.println();
    }

    /** 把 Token 数格式化成带 k 的紧凑形式（如 18234 → 18.2k），小于 1000 直接显示。 */
    public static String formatTokens(int tokens) {
        if (tokens < 1000) {
            return String.valueOf(tokens);
        }
        return String.format("%.1fk", tokens / 1000.0);
    }

    /** 把参数 Map 压成一行简短摘要，过长则截断。 */
    private String summarizeArgs(Map<String, Object> args) {
        if (args == null || args.isEmpty()) {
            return "";
        }
        String joined = args.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", "));
        return joined.length() > 120 ? joined.substring(0, 117) + "..." : joined;
    }
}

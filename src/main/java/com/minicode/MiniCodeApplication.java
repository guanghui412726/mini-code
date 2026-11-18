package com.minicode;

import com.minicode.context.ContextAssembler;
import com.minicode.context.HistoryStage;
import com.minicode.context.SystemPromptStage;

import java.util.List;

public class MiniCodeApplication {

    public static void main(String[] args) {

        // 上下文管线组装
        ContextAssembler contextAssembler = new ContextAssembler(List.of(
                new SystemPromptStage("系统提示词"),
                new HistoryStage()
        ));
    }
}

package com.minicode.tool;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 工具注册中心
 */
public class ToolRegistry {

    private final Map<String, Tool> tools = new LinkedHashMap<>();

    /**
     * 注册工具
     * @param tool
     */
    public void register(Tool tool) {
        tools.put(tool.name(), tool);
    }

    /**
     * 工具查找
     * @param name
     * @return
     */
    public Optional<Tool> find(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    public Collection<Tool> all() {
        return tools.values();
    }
}

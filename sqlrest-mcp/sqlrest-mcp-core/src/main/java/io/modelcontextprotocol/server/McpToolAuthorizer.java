package io.modelcontextprotocol.server;

import java.util.Set;

/**
 * sqlrest 定制：按客户端令牌解析可见 Tool 名称集合的过滤抽象。
 * 令牌为空或未配置授权时返回空集合（默认全禁）。
 */
@FunctionalInterface
public interface McpToolAuthorizer {

  Set<String> getAllowedToolNames(String clientToken);
}

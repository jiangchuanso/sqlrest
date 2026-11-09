package io.modelcontextprotocol.server;

import java.util.Collections;
import java.util.Set;

/**
 * sqlrest 定制：按令牌解析可见 Tool 的公共逻辑，供 SSE 与 StreamableHTTP 两条链路复用。
 * authorizer 为 null 表示不过滤（保持 SDK 原生行为）；否则以解析结果为准，空集合表示全禁。
 */
public final class McpToolAccess {

  private McpToolAccess() {
  }

  public static Set<String> resolveAllowed(McpToolAuthorizer authorizer, String clientToken) {
    if (null == authorizer) {
      return null;
    }
    Set<String> allowed = authorizer.getAllowedToolNames(clientToken);
    return null == allowed ? Collections.emptySet() : allowed;
  }

  public static boolean isAllowed(Set<String> allowed, String toolName) {
    return null == allowed || allowed.contains(toolName);
  }
}

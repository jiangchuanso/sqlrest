package io.modelcontextprotocol.server;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * sqlrest 定制：按令牌解析可见 Tool 的公共逻辑，供 SSE 与 StreamableHTTP 两条链路复用。
 * authorizer 为 null 表示不过滤（保持 SDK 原生行为）；否则以解析结果为准，空集合表示全禁。
 */
public final class McpToolAccess {

  /**
   * 诊断日志中最多列举的工具名称个数，避免工具很多时刷屏。
   */
  private static final int MAX_LOGGED_TOOL_NAMES = 20;

  private static final Logger log = LoggerFactory.getLogger(McpToolAccess.class);

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

  /**
   * 记录一次 tools/list 的结果与过滤依据。
   * <p>
   * 空结果有两种完全不同的成因（内存未注册 / 令牌未授权），仅看响应无法区分，
   * 因此这里按成因分别输出 WARN，便于生产上直接定位。
   */
  public static void logListResult(String transport, String clientToken, List<String> inMemoryToolNames,
      Set<String> allowed, int returnedCount) {
    String maskedToken = maskToken(clientToken);
    if (returnedCount > 0) {
      log.info("[MCP tools/list][{}] token={} inMemoryTools={} authorizedTools={} returned={}",
          transport, maskedToken, inMemoryToolNames.size(), null == allowed ? "ALL" : allowed.size(), returnedCount);
      return;
    }
    if (inMemoryToolNames.isEmpty()) {
      log.warn("[MCP tools/list][{}] token={} returned EMPTY list: no tool is registered in memory. "
              + "Check the manager startup log for 'skip add mcp tool': that tool's api_id has no "
              + "SQLREST_API_ONLINE record, so it can never be listed until the api is published again.",
          transport, maskedToken);
      return;
    }
    log.warn("[MCP tools/list][{}] token={} returned EMPTY list: this token has no authorized tool, "
            + "SQLREST_MCP_CLIENT_TOOL has no row for it. inMemoryTools={} authorizedTools={}",
        transport, maskedToken, brief(inMemoryToolNames), allowed);
  }

  private static String brief(List<String> toolNames) {
    if (toolNames.size() <= MAX_LOGGED_TOOL_NAMES) {
      return toolNames.toString();
    }
    return toolNames.stream().limit(MAX_LOGGED_TOOL_NAMES).collect(Collectors.toList())
        + "...(total " + toolNames.size() + ")";
  }

  private static String maskToken(String clientToken) {
    if (null == clientToken || clientToken.length() <= 8) {
      return "***";
    }
    return clientToken.substring(0, 8) + "***";
  }
}

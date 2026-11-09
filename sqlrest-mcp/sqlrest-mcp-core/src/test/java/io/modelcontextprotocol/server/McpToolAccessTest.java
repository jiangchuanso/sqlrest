package io.modelcontextprotocol.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class McpToolAccessTest {

  @Test
  public void nullAuthorizerMeansNoFiltering() {
    assertNull(McpToolAccess.resolveAllowed(null, "any"));
    assertTrue(McpToolAccess.isAllowed(null, "tool1"));
  }

  @Test
  public void authorizerResolvesAllowedNames() {
    McpToolAuthorizer authorizer = token -> new HashSet<>(Arrays.asList("tool1", "tool2"));
    Set<String> allowed = McpToolAccess.resolveAllowed(authorizer, "tokenA");
    assertTrue(McpToolAccess.isAllowed(allowed, "tool1"));
    assertFalse(McpToolAccess.isAllowed(allowed, "tool3"));
  }

  @Test
  public void nullResultBecomesEmptySetDenyAll() {
    McpToolAuthorizer authorizer = token -> null;
    Set<String> allowed = McpToolAccess.resolveAllowed(authorizer, "tokenA");
    assertEquals(Collections.emptySet(), allowed);
    assertFalse(McpToolAccess.isAllowed(allowed, "tool1"));
  }

  @Test
  public void emptySetDeniesAll() {
    McpToolAuthorizer authorizer = token -> Collections.emptySet();
    Set<String> allowed = McpToolAccess.resolveAllowed(authorizer, "tokenB");
    assertFalse(McpToolAccess.isAllowed(allowed, "tool1"));
  }
}

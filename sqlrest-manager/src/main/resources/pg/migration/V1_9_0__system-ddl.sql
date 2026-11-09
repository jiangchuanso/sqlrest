CREATE TABLE SQLREST_MCP_CLIENT_TOOL (
  "id"        bigserial   not null,
  "client_id" int8        not null,
  "tool_id"   int8        not null,
  primary key ("id"),
  FOREIGN KEY ("client_id") REFERENCES SQLREST_MCP_CLIENT ("id") ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY ("tool_id")   REFERENCES SQLREST_MCP_TOOL ("id")   ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE UNIQUE INDEX "SQLREST_MCP_CLIENT_TOOL_UK" ON SQLREST_MCP_CLIENT_TOOL("client_id", "tool_id");

-- 存量兼容：为升级前已存在的令牌回填当时已配置的全部 Tool 授权，保持升级前的“全量可见”行为
INSERT INTO SQLREST_MCP_CLIENT_TOOL ("client_id", "tool_id")
SELECT c."id", t."id"
FROM SQLREST_MCP_CLIENT c
CROSS JOIN SQLREST_MCP_TOOL t;

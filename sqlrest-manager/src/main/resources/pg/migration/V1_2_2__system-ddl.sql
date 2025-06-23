CREATE TABLE SQLREST_MCP_CLIENT  (
  "id"              bigserial             not null,
  "name"            varchar(256)          not null,
  "token"           varchar(64)           not null,
  "create_time"     timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
  "update_time"     timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
  primary key ("id")
);
CREATE UNIQUE INDEX "SQLREST_MCP_CLIENT_NAME_IDX" ON SQLREST_MCP_CLIENT("name");
CREATE UNIQUE INDEX "SQLREST_MCP_CLIENT_TOKEN_IDX" ON SQLREST_MCP_CLIENT("token");

CREATE TABLE SQLREST_MCP_TOOL  (
  "id"              bigserial             not null,
  "name"            varchar(256)          not null,
  "description"     varchar(1024)         not null,
  "api_id"          int8                  not null,
  "create_time"     timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
  "update_time"     timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
  primary key ("id"),
  FOREIGN KEY ("api_id") REFERENCES SQLREST_API_ASSIGNMENT ("id") ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE UNIQUE INDEX "SQLREST_MCP_TOOL_NAME_IDX" ON SQLREST_MCP_TOOL("name");

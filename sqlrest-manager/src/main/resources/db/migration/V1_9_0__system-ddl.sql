create table `SQLREST_MCP_CLIENT_TOOL`  (
  `id`        bigint(20)  not null auto_increment               comment '主键id',
  `client_id` bigint(20)  not null                              comment 'MCP令牌ID',
  `tool_id`   bigint(20)  not null                              comment 'MCP工具ID',
  primary key (`id`),
  unique key `uk_client_tool` (`client_id`, `tool_id`),
  FOREIGN KEY (`client_id`) REFERENCES `SQLREST_MCP_CLIENT` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`tool_id`)   REFERENCES `SQLREST_MCP_TOOL` (`id`)   ON DELETE CASCADE ON UPDATE CASCADE
) engine=InnoDB character set = utf8 comment = 'MCP令牌工具授权关联表';

-- 存量兼容：为升级前已存在的令牌回填当时已配置的全部 Tool 授权，保持升级前的“全量可见”行为
INSERT INTO `SQLREST_MCP_CLIENT_TOOL` (`client_id`, `tool_id`)
SELECT c.`id`, t.`id`
FROM `SQLREST_MCP_CLIENT` c
CROSS JOIN `SQLREST_MCP_TOOL` t;

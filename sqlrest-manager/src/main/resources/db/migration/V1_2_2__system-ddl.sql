create table `SQLREST_MCP_CLIENT`  (
  `id`                  bigint(20)            not null auto_increment   comment '主键id',
  `name`                varchar(256)          not null                  comment '客户端名称',
  `token`               varchar(64)           not null                  comment '连接TOKEN',
  `create_time`         timestamp             not null default current_timestamp comment '创建时间',
  `update_time`         timestamp             not null default current_timestamp on update current_timestamp comment '修改时间',
  primary key (`id`),
  unique key `name` (`name`),
  KEY `idx_token` (`token`) USING BTREE
) engine=InnoDB character set = utf8 comment = 'MCP连接客户端表';

create table `SQLREST_MCP_TOOL`  (
  `id`                  bigint(20)            not null auto_increment   comment '主键id',
  `name`                varchar(256)          not null                  comment '工具名',
  `description`         varchar(1024)         not null                  comment '工具描述',
  `api_id`              bigint(20) unsigned   not null                  comment 'API接口ID',
  `create_time`         timestamp             not null default current_timestamp comment '创建时间',
  `update_time`         timestamp             not null default current_timestamp on update current_timestamp comment '修改时间',
  primary key (`id`),
  unique key `name` (`name`),
  FOREIGN KEY (`api_id`) REFERENCES `SQLREST_API_ASSIGNMENT` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) engine=InnoDB character set = utf8 comment = 'MCP工具配置';

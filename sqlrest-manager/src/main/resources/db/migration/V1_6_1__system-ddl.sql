CREATE TABLE `SQLREST_VERSION_COMMIT` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_id` bigint(20) unsigned NOT NULL COMMENT '对象ID',
  `version` bigint(20) unsigned NOT NULL COMMENT '版本号',
  `description`  varchar(256) NULL COMMENT '描述',
  `content` longtext NOT NULL COMMENT '内容',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `api_id_version` (`biz_id`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='版本管理表';

CREATE TABLE `SQLREST_API_ONLINE` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT '接口名称',
  `method` varchar(16) NOT NULL DEFAULT 'GET' COMMENT '请求方法',
  `path` varchar(255) NOT NULL DEFAULT '' COMMENT '请求路径',
  `api_id` bigint(20) unsigned NOT NULL COMMENT '分组ID',
  `group_id` bigint(20) unsigned NOT NULL COMMENT '分组ID',
  `module_id` bigint(20) unsigned NOT NULL COMMENT '模块ID',
  `datasource_id` bigint(20) unsigned NOT NULL COMMENT '数据源ID',
  `open` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否公开',
  `alarm` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用告警',
  `flow_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否开启流量控制',
  `commit_id` bigint(20) unsigned NOT NULL COMMENT '版本CommitId',
  `version` bigint(20) unsigned NOT NULL COMMENT '版本号',
  `content` longtext NOT NULL COMMENT '详细内容JSON',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_method_path` (`method`,`path`),
  KEY `api_id` (`api_id`),
  KEY `group_id` (`group_id`),
  KEY `module_id` (`module_id`),
  KEY `datasource_id` (`datasource_id`),
  FOREIGN KEY (`api_id`) REFERENCES `SQLREST_API_ASSIGNMENT` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`group_id`) REFERENCES `SQLREST_API_GROUP` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`module_id`) REFERENCES `SQLREST_API_MODULE` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`datasource_id`) REFERENCES `SQLREST_DATASOURCE` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`commit_id`) REFERENCES `SQLREST_VERSION_COMMIT` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='接口在线表';


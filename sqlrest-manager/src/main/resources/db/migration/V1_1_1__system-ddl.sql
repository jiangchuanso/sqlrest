ALTER TABLE `SQLREST_API_ASSIGNMENT` ADD COLUMN `cache_key_type` varchar(16) not null default 'NONE' COMMENT '缓存键类型' AFTER `content_type`;
ALTER TABLE `SQLREST_API_ASSIGNMENT` ADD COLUMN `cache_key_expr` varchar(255) null COMMENT '缓存key的SpEL表达式' AFTER `cache_key_type`;
ALTER TABLE `SQLREST_API_ASSIGNMENT` ADD COLUMN `cache_expire_seconds` bigint(20)   unsigned not null default 0 COMMENT '缓存过期时长(秒)' AFTER `cache_key_expr`;

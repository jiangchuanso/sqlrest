ALTER TABLE `SQLREST_ACCESS_RECORD`
ADD COLUMN `executor_addr`  varchar(128) NULL COMMENT '执行器的IP地址' AFTER `create_time`,
ADD COLUMN `gateway_addr`  varchar(128) NULL COMMENT '网关的IP地址' AFTER `executor_addr`;

ALTER TABLE `SQLREST_ACCESS_RECORD`
MODIFY COLUMN `exception` longtext  DEFAULT NULL COMMENT '错误日志' AFTER `api_id`,
ADD COLUMN `parameters` longtext DEFAULT NULL COMMENT '请求入参' AFTER `api_id`;

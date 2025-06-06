create table `SQLREST_UNIFY_ALARM`  (
  `id`                  bigint(20)            not null auto_increment   comment '主键id',
  `status`              varchar(4)            not null default 'OFF'    comment '状态：OFF-关闭;ON-启用',
  `endpoint`            varchar(256)          not null                  comment '告警系统地址',
  `content_type`        varchar(128)          not null                  comment '接口入参ContentType',
  `input_template`      varchar(4096)         not null                  comment '接口入参模板',
  `create_time`         timestamp             not null default current_timestamp comment '创建时间',
  `update_time`         timestamp             not null default current_timestamp on update current_timestamp comment '修改时间',
  primary key (`id`)
) engine=InnoDB character set = utf8 comment = '统一告警参数配置';

INSERT INTO `SQLREST_UNIFY_ALARM` (`id`, `status`, `endpoint`,`content_type`, `input_template`)
VALUES ('1', 'OFF', 'http://127.0.0.1:8000/api/v1/message/send', 'application/json', '{}');

ALTER TABLE `SQLREST_API_ASSIGNMENT` ADD COLUMN `alarm` tinyint(1) not null default 0 COMMENT '是否启用告警' AFTER `open`;

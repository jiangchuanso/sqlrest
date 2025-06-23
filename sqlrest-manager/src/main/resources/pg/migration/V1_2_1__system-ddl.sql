CREATE TABLE SQLREST_UNIFY_ALARM  (
  "id"              bigserial             not null,
  "status"          varchar(4)            not null default 'OFF',
  "endpoint"        varchar(256)          not null,
  "content_type"    varchar(128)          not null,
  "input_template"  varchar(4096)         not null,
  "create_time"     timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
  "update_time"     timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
  primary key ("id")
);

INSERT INTO SQLREST_UNIFY_ALARM ("id", "status", "endpoint","content_type", "input_template")
VALUES ('1', 'OFF', 'http://127.0.0.1:8000/api/v1/message/send', 'application/json', '{}');

ALTER TABLE SQLREST_API_ASSIGNMENT ADD COLUMN "alarm" boolean not null default false;


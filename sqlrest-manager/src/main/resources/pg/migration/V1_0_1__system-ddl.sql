CREATE TABLE SQLREST_SYSTEM_USER  (
  "id"                  bigserial             not null,
  "username"            varchar(255)          not null,
  "password"            varchar(128)          not null,
  "salt"                varchar(128)          not null,
  "real_name"           varchar(255)          not null default '',
  "email"               varchar(255)          not null default '',
  "address"             varchar(255)          not null default '',
  "locked"              boolean               not null default false,
  "create_time"         timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
  "update_time"         timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
  PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "SQLREST_SYSTEM_USER_USERNAME_IDX" ON SQLREST_SYSTEM_USER("username");

CREATE TABLE SQLREST_DATASOURCE (
  "id"                  bigserial             not null,
  "name"                varchar(200)          not null,
  "type"                varchar(200)          not null,
  "version"             varchar(255)          not null,
  "driver"              varchar(200)          not null,
  "url"                 text                          ,
  "username"            varchar(200)          not null default '',
  "password"            varchar(200)          not null default '',
  "create_time"         timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
  "update_time"         timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
  primary key ("id")
);
CREATE UNIQUE INDEX "SQLREST_DATASOURCE_NAME_IDX" ON SQLREST_DATASOURCE("name");

CREATE TABLE SQLREST_API_GROUP
(
    "id"             bigserial             not null,
    "name"           varchar(255)          not null,
    "create_time"    timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
    "update_time"    timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "SQLREST_API_GROUP_NAME_IDX" ON SQLREST_API_GROUP("name");

CREATE TABLE SQLREST_API_MODULE
(
    "id"             bigserial             not null,
    "name"           varchar(255)          not null,
    "create_time"    timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
    "update_time"    timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "SQLREST_API_MODULE_NAME_IDX" ON SQLREST_API_MODULE("name");

CREATE TABLE SQLREST_API_ASSIGNMENT
(
    "id"               bigserial             not null,
    "group_id"         int8                  not null,
    "module_id"        int8                  not null,
    "datasource_id"    int8                  not null,
    "name"             varchar(255)          not null,
    "description"      varchar(1024)                  default null,
    "method"           varchar(16)           not null default 'GET',
    "path"             varchar(255)          not null default '',
    "params"           text                      null,
    "outputs"          text                      null,
    "status"           boolean               not null default false,
    "open"             boolean               not null default false,
    "engine"           varchar(16)           not null default 'SQL',
    "response_format"  text                           default null,
    "naming_strategy"  varchar(16)           not null default 'NONE',
    "flow_status"      boolean               not null default false,
    "flow_grade"       int8                  not null,
    "flow_count"       int8                  not null,
    "content_type"     varchar(50)           not null default '',
    "create_time"    timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
    "update_time"    timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
    PRIMARY KEY ("id"),
    foreign key ("group_id") references SQLREST_API_GROUP ("id") on delete cascade on update cascade,
    foreign key ("module_id") references SQLREST_API_MODULE ("id") on delete cascade on update cascade,
    foreign key ("datasource_id") references SQLREST_DATASOURCE ("id") on delete cascade on update cascade
);
CREATE UNIQUE INDEX "SQLREST_API_ASSIGNMENT_METHOD_PATH_IDX" ON SQLREST_API_ASSIGNMENT("method","path");

CREATE TABLE SQLREST_API_CONTEXT
(
    "id"                      bigserial             not null,
    "api_id"                  int8                  not null,
    "sql_text"                text                  not null,
    primary key ("id"),
    foreign key ("api_id") references SQLREST_API_ASSIGNMENT ("id") on delete cascade on update cascade
);

CREATE TABLE SQLREST_FIREWALL_RULES
(
    "id"             bigserial        not null,
    "status"         varchar(4)       not null default 'OFF',
    "mode"           varchar(16)      not null default 'BLACK',
    "addresses"      text                      default null,
    "create_time"    timestamp(6)     not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
    "update_time"    timestamp(6)     not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
    PRIMARY KEY ("id")
);

CREATE TABLE SQLREST_APP_CLIENT
(
    "id"              bigserial             not null,
    "name"            varchar(255)          not null,
    "description"     varchar(1024)                   default null,
    "app_key"         varchar(64)           not null,
    "app_secret"      varchar(64)           not null,
    "expire_duration" varchar(16)           not null default 'FOR_EVER',
    "expire_at"       int8                  not null default '0',
    "access_token"    varchar(64)                    default null,
    "create_time"     timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
    "update_time"     timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "SQLREST_APP_CLIENT_APP_KEY_IDX" ON SQLREST_APP_CLIENT("app_key");

CREATE TABLE SQLREST_CLIENT_GROUP
(
    "id"        bigserial   not null,
    "client_id" int8        not null,
    "group_id"  int8        not null,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "SQLREST_CLIENT_GROUP_IDX" ON SQLREST_CLIENT_GROUP("client_id","group_id");

CREATE TABLE SQLREST_ACCESS_RECORD
(
    "id"              bigserial          not null,
    "path"            varchar(255)       default null,
    "status"          int8               default null,
    "duration"        int8               default null,
    "ip_addr"         varchar(64)        default null,
    "user_agent"      varchar(255)       default null,
    "client_key"      varchar(64)        default null,
    "api_id"          int8           not null,
    "exception"       varchar(1024)      default null,
    "create_time"     timestamp(6)   not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
    PRIMARY KEY ("id")
);

CREATE TABLE SQLREST_SYSTEM_PARAM (
  "id"               bigserial         NOT NULL,
  "param_key"        varchar(128)      NOT NULL,
  "param_type"       varchar(64)       NOT NULL,
  "param_value"      varchar(255)      NOT NULL,
  PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "SQLREST_SYSTEM_PARAM_PARAM_KEY_IDX" ON SQLREST_SYSTEM_PARAM("param_key");

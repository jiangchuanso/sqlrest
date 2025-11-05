CREATE TABLE SQLREST_VERSION_COMMIT (
  "id"             bigserial             not null,
  "biz_id"         int8                  not null,
  "version"        int8                  not null,
  "description"    varchar(256)          not null,
  "content"        text                  not null,
  "create_time"    timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
  PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX "SQLREST_VERSION_COMMIT_API_ID_VERSION_IDX" ON SQLREST_VERSION_COMMIT("biz_id","version");

CREATE TABLE SQLREST_API_ONLINE (
  "id"                  bigserial             not null,
  "name"                varchar(255)          not null,
  "method"              varchar(16)           not null,
  "path"                varchar(255)          not null,
  "api_id"              int8                  not null,
  "group_id"            int8                  not null,
  "module_id"           int8                  not null,
  "datasource_id"       int8                  not null,
  "open"                boolean               not null default false,
  "alarm"               boolean               not null default false,
  "flow_status"         boolean               not null default false,
  "commit_id"           int8                  not null,
  "version"             int8                  not null,
  "content"             text                  not null,
  "create_time"         timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
  "update_time"         timestamp(6)          not null default (CURRENT_TIMESTAMP(0))::timestamp(0) without time zone,
  PRIMARY KEY ("id"),
  FOREIGN KEY ("api_id") REFERENCES SQLREST_API_ASSIGNMENT ("id") ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY ("group_id") REFERENCES SQLREST_API_GROUP ("id") ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY ("module_id") REFERENCES SQLREST_API_MODULE ("id") ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY ("datasource_id") REFERENCES SQLREST_DATASOURCE ("id") ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY ("commit_id") REFERENCES SQLREST_VERSION_COMMIT ("id") ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE UNIQUE INDEX "SQLREST_API_ONLINE_METHOD_PATH_IDX" ON SQLREST_API_ONLINE("method","path");
CREATE INDEX "SQLREST_API_ONLINE_API_ID_IDX" ON SQLREST_API_ONLINE("api_id");
CREATE INDEX "SQLREST_API_ONLINE_GROUP_ID_IDX" ON SQLREST_API_ONLINE("group_id");
CREATE INDEX "SQLREST_API_ONLINE_MODULE_ID_IDX" ON SQLREST_API_ONLINE("module_id");
CREATE INDEX "SQLREST_API_ONLINE_DATASOURCE_ID_IDX" ON SQLREST_API_ONLINE("datasource_id");

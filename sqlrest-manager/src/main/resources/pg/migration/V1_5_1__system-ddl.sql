ALTER TABLE SQLREST_ACCESS_RECORD ADD COLUMN "executor_addr" varchar(128) default null;
ALTER TABLE SQLREST_ACCESS_RECORD ADD COLUMN "gateway_addr" varchar(128) default null;

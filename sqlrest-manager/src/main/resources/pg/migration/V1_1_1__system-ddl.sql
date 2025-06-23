ALTER TABLE SQLREST_API_ASSIGNMENT ADD COLUMN "cache_key_type" varchar(16) not null default 'NONE';
ALTER TABLE SQLREST_API_ASSIGNMENT ADD COLUMN "cache_key_expr" varchar(255) null;
ALTER TABLE SQLREST_API_ASSIGNMENT ADD COLUMN "cache_expire_seconds" int8 not null default 0;

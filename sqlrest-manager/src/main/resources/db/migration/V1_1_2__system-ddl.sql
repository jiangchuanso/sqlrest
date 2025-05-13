ALTER TABLE `SQLREST_APP_CLIENT` ADD COLUMN `token_alive` varchar(16) not null default 'PERIOD' COMMENT 'TOKEN的生命期' AFTER `access_token`;
ALTER TABLE `SQLREST_APP_CLIENT` ADD INDEX `idx_access_token` (`access_token`) USING BTREE ;

INSERT INTO SQLREST_SYSTEM_USER("username","password","salt","real_name","locked","email")
VALUES ('admin', '$2a$10$eUanVjvzV27BBxAb4zuBCugwnngHkRZ7ZB4iI5tdx9ETJ2tnXJJDy', '$2a$10$eUanVjvzV27BBxAb4zuBCu', 'Administrator', false,'admin@126.com');

INSERT INTO SQLREST_FIREWALL_RULES ("id", "status", "mode")
VALUES ('1', 'OFF', 'BLACK');

INSERT INTO SQLREST_API_GROUP ("name")
VALUES ('DefaultGroup');

INSERT INTO SQLREST_API_MODULE ("name")
VALUES ('DefaultModule');

INSERT INTO SQLREST_APP_CLIENT ("name", "description", "app_key", "app_secret", "expire_duration", "expire_at", "access_token")
VALUES ('test', 'usage for test', 'test', 'test', 'FOR_EVER', '-1', '9097ac1ab13198dfa4ddb2ecc1079693');

INSERT INTO SQLREST_CLIENT_GROUP ("client_id", "group_id")
VALUES ('1', '1');

INSERT INTO SQLREST_SYSTEM_PARAM ("param_key", "param_type", "param_value") VALUES ('apiDocOpen', 'BOOLEAN', 'true');
INSERT INTO SQLREST_SYSTEM_PARAM ("param_key", "param_type", "param_value") VALUES ('apiDocInfoTitle', 'STRING', 'Online Interface Document');
INSERT INTO SQLREST_SYSTEM_PARAM ("param_key", "param_type", "param_value") VALUES ('apiDocInfoVersion', 'STRING', '1.0');
INSERT INTO SQLREST_SYSTEM_PARAM ("param_key", "param_type", "param_value") VALUES ('apiDocInfoDescription', 'STRING', 'Swagger Online Document');




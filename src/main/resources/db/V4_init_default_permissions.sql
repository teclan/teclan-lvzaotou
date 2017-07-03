--- 初始化登录登出权限
INSERT INTO permissions (app_key, role, permission) VALUES ('teclan-lvzaotou', 'system', '/login');
INSERT INTO permissions (app_key, role, permission) VALUES ('teclan-lvzaotou', 'audit', '/login');
INSERT INTO permissions (app_key, role, permission) VALUES ('teclan-lvzaotou', 'security', '/login');

INSERT INTO permissions (app_key, role, permission) VALUES ('teclan-lvzaotou', 'system', '/sign-in');
INSERT INTO permissions (app_key, role, permission) VALUES ('teclan-lvzaotou', 'audit', '/sign-in');
INSERT INTO permissions (app_key, role, permission) VALUES ('teclan-lvzaotou', 'security', '/sign-in');

INSERT INTO permissions (app_key, role, permission) VALUES ('teclan-lvzaotou', 'system', '/sign-up');
INSERT INTO permissions (app_key, role, permission) VALUES ('teclan-lvzaotou', 'audit', '/sign-up');
INSERT INTO permissions (app_key, role, permission) VALUES ('teclan-lvzaotou', 'security', '/sign-up');

INSERT INTO permissions (app_key, role, permission) VALUES ('teclan-lvzaotou', 'system', '/sign-out');
INSERT INTO permissions (app_key, role, permission) VALUES ('teclan-lvzaotou', 'audit', '/sign-out');
INSERT INTO permissions (app_key, role, permission) VALUES ('teclan-lvzaotou', 'security', '/sign-out');

--- 修改密码权限
INSERT INTO permissions (app_key, role, permission) VALUES ('teclan-lvzaotou', 'system', '/password');
INSERT INTO permissions (app_key, role, permission) VALUES ('teclan-lvzaotou', 'audit', '/password');
INSERT INTO permissions (app_key, role, permission) VALUES ('teclan-lvzaotou', 'security', '/password');
-- 补充执行用户角色管理模块数据
-- 由于字符编码问题，分步执行

-- 插入角色定义（使用英文避免编码问题）
INSERT INTO roles (code, name_zh, name_en, description_zh, description_en) VALUES
('ADMIN', 'Administrator', 'Administrator', 'Has all permissions', 'Has all permissions'),
('OPERATOR', 'Operator', 'Operator', 'Can execute all dial test related operations', 'Can execute all dial test related operations'),
('BROWSER', 'Browser', 'Browser', 'View only', 'View only'),
('EXECUTOR', 'Executor', 'Executor', 'For executor registration', 'For executor registration')
ON CONFLICT (code) DO NOTHING;

-- 插入默认管理员
INSERT INTO user_roles (username, role) VALUES ('admin', 'ADMIN')
ON CONFLICT (username, role) DO NOTHING;

-- 验证数据插入
SELECT 'Roles inserted:' as status, count(*) as count FROM roles;
SELECT 'Admin user inserted:' as status, count(*) as count FROM user_roles WHERE username = 'admin' AND role = 'ADMIN';

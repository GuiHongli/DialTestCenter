-- 操作记录模块数据库脚本
-- 创建时间: 2024-01-15

-- 1. 操作类型枚举表
CREATE TABLE operation_types (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name_zh VARCHAR(50) NOT NULL,
    name_en VARCHAR(50) NOT NULL,
    description_zh TEXT,
    description_en TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. 操作目标枚举表
CREATE TABLE operation_targets (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name_zh VARCHAR(50) NOT NULL,
    name_en VARCHAR(50) NOT NULL,
    description_zh TEXT,
    description_en TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. 操作记录表
CREATE TABLE operation_logs (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    operation_type VARCHAR(20) NOT NULL,
    operation_target VARCHAR(50) NOT NULL,
    operation_description_zh TEXT,
    operation_description_en TEXT,
    operation_data JSONB,
    operation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. 插入基础操作类型
INSERT INTO operation_types (code, name_zh, name_en, description_zh, description_en) VALUES
('CREATE', '创建', 'Create', '创建新记录', 'Create new record'),
('UPDATE', '更新', 'Update', '更新现有记录', 'Update existing record'),
('DELETE', '删除', 'Delete', '删除记录', 'Delete record'),
('VIEW', '查看', 'View', '查看记录', 'View record'),
('LOGIN', '登录', 'Login', '用户登录', 'User login'),
('LOGOUT', '登出', 'Logout', '用户登出', 'User logout'),
('EXPORT', '导出', 'Export', '导出数据', 'Export data'),
('IMPORT', '导入', 'Import', '导入数据', 'Import data'),
('UPLOAD', '上传', 'Upload', '上传文件', 'Upload file'),
('DOWNLOAD', '下载', 'Download', '下载文件', 'Download file');

-- 5. 插入基础操作目标
INSERT INTO operation_targets (code, name_zh, name_en, description_zh, description_en) VALUES
('USER', '用户管理', 'User Management', '用户相关操作', 'User related operations'),
('USER_ROLE', '角色管理', 'Role Management', '用户角色相关操作', 'User role related operations'),
('TEST_CASE_SET', '测试用例集', 'Test Case Set', '测试用例集相关操作', 'Test case set related operations'),
('SOFTWARE_PACKAGE', '软件包管理', 'Software Package Management', '软件包相关操作', 'Software package related operations'),
('OPERATION_LOG', '操作记录', 'Operation Log', '操作记录相关操作', 'Operation log related operations'),
('SYSTEM', '系统', 'System', '系统相关操作', 'System related operations');

-- 6. 创建索引
CREATE INDEX idx_operation_logs_username ON operation_logs(username);
CREATE INDEX idx_operation_logs_operation_type ON operation_logs(operation_type);
CREATE INDEX idx_operation_logs_operation_target ON operation_logs(operation_target);
CREATE INDEX idx_operation_logs_operation_time ON operation_logs(operation_time);

-- 7. 创建复合索引
CREATE INDEX idx_operation_logs_username_time ON operation_logs(username, operation_time);
CREATE INDEX idx_operation_logs_type_time ON operation_logs(operation_type, operation_time);

-- 8. 添加检查约束
ALTER TABLE operation_logs 
ADD CONSTRAINT chk_description_not_empty 
CHECK (
    operation_description_zh IS NOT NULL OR operation_description_en IS NOT NULL
);

-- 9. 插入测试数据
INSERT INTO operation_logs (
    username, operation_type, operation_target, 
    operation_description_zh, operation_description_en, 
    operation_data, operation_time
) VALUES 
(
    'admin', 'CREATE', 'USER', 
    '创建用户: testuser1', 
    'Create user: testuser1',
    '{"username": "testuser1", "role": "USER"}', 
    '2024-01-15 14:30:25'
),
(
    'admin', 'CREATE', 'USER', 
    '创建用户: testuser2', 
    'Create user: testuser2',
    '{"username": "testuser2", "role": "USER"}', 
    '2024-01-15 14:31:10'
),
(
    'admin', 'UPDATE', 'USER', 
    '更新用户: testuser1', 
    'Update user: testuser1',
    '{"old": {"role": "USER"}, "new": {"role": "ADMIN"}}', 
    '2024-01-15 14:35:00'
);

-- 10. 验证数据
SELECT 
    ol.id,
    ol.username,
    ol.operation_description_zh,
    ol.operation_description_en,
    ot.name_zh as operation_type_name,
    otg.name_zh as operation_target_name,
    ol.operation_time
FROM operation_logs ol
LEFT JOIN operation_types ot ON ol.operation_type = ot.code
LEFT JOIN operation_targets otg ON ol.operation_target = otg.code
ORDER BY ol.operation_time DESC;

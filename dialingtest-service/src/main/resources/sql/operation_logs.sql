-- 操作记录模块数据库脚本
-- 创建时间: 2024-01-15

-- 1. 操作记录表
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

-- 2. 创建索引
CREATE INDEX idx_operation_logs_username ON operation_logs(username);
CREATE INDEX idx_operation_logs_operation_type ON operation_logs(operation_type);
CREATE INDEX idx_operation_logs_operation_target ON operation_logs(operation_target);
CREATE INDEX idx_operation_logs_operation_time ON operation_logs(operation_time);

-- 3. 创建复合索引
CREATE INDEX idx_operation_logs_username_time ON operation_logs(username, operation_time);
CREATE INDEX idx_operation_logs_type_time ON operation_logs(operation_type, operation_time);

-- 4. 添加检查约束
ALTER TABLE operation_logs 
ADD CONSTRAINT chk_description_not_empty 
CHECK (
    operation_description_zh IS NOT NULL OR operation_description_en IS NOT NULL
);

-- 5. 插入测试数据
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

-- 6. 验证数据
SELECT 
    ol.id,
    ol.username,
    ol.operation_description_zh,
    ol.operation_description_en,
    ol.operation_type,
    ol.operation_target,
    ol.operation_time
FROM operation_logs ol
ORDER BY ol.operation_time DESC;

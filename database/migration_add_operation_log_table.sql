-- 创建操作记录表的数据库迁移脚本
-- 用于记录用户所有的操作，包括新增、修改、删除各种数据

-- 1. 创建operation_log表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    operation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    operation_type VARCHAR(50) NOT NULL,
    target VARCHAR(200) NOT NULL,
    description TEXT
);

-- 2. 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_operation_log_username ON operation_log(username);
CREATE INDEX IF NOT EXISTS idx_operation_log_operation_time ON operation_log(operation_time);
CREATE INDEX IF NOT EXISTS idx_operation_log_operation_type ON operation_log(operation_type);
CREATE INDEX IF NOT EXISTS idx_operation_log_target ON operation_log(target);

-- 3. 添加表注释
COMMENT ON TABLE operation_log IS '操作记录表，存储用户所有操作记录';
COMMENT ON COLUMN operation_log.id IS '操作记录ID，主键';
COMMENT ON COLUMN operation_log.username IS '操作用户名';
COMMENT ON COLUMN operation_log.operation_time IS '操作时间';
COMMENT ON COLUMN operation_log.operation_type IS '操作类型：CREATE/UPDATE/DELETE/LOGIN/LOGOUT等';
COMMENT ON COLUMN operation_log.target IS '操作对象：如用户、角色、测试用例集等';
COMMENT ON COLUMN operation_log.description IS '操作描述：详细的操作说明';

-- 4. 插入示例数据
INSERT INTO operation_log (username, operation_type, target, description) VALUES 
('admin', 'CREATE', '用户管理', '创建用户：testuser'),
('admin', 'UPDATE', '角色管理', '更新角色：ADMIN权限'),
('admin', 'DELETE', '测试用例集', '删除测试用例集：ID=1'),
('admin', 'LOGIN', '系统登录', '用户登录系统'),
('admin', 'LOGOUT', '系统登出', '用户登出系统')
ON CONFLICT DO NOTHING;

-- 6. 验证表创建结果
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'operation_log' 
ORDER BY ordinal_position;

-- 用户角色管理模块数据库初始化脚本
-- 创建时间: 2024-01-15
-- 描述: 用户角色管理模块相关表结构

-- 1. 用户角色关系表
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    UNIQUE(username, role)
);

-- 2. 创建索引
CREATE INDEX IF NOT EXISTS idx_user_roles_username ON user_roles(username);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role);

-- 3. 插入默认管理员
INSERT INTO user_roles (username, role) VALUES ('admin', 'ADMIN')
ON CONFLICT (username, role) DO NOTHING;

-- 4. 关联操作记录表（operation_logs）
-- 用户角色管理模块的所有操作都会记录到operation_logs表
-- 操作类型：CREATE, UPDATE, DELETE, VIEW, PERMISSION_CHECK, PERMISSION_DENIED
-- 操作目标：USER_ROLE
-- 支持中英文操作描述存储

-- 5. 添加注释
COMMENT ON TABLE user_roles IS 'User role relationship table';

COMMENT ON COLUMN user_roles.id IS 'Primary key ID, auto increment';
COMMENT ON COLUMN user_roles.username IS 'Username';
COMMENT ON COLUMN user_roles.role IS 'Role code';

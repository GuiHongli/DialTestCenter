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

-- 2. 角色枚举表（可选，用于角色描述）
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name_zh VARCHAR(50) NOT NULL,
    name_en VARCHAR(50) NOT NULL,
    description_zh TEXT,
    description_en TEXT
);

-- 3. 创建索引
CREATE INDEX IF NOT EXISTS idx_user_roles_username ON user_roles(username);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role);

-- 4. 插入角色定义
INSERT INTO roles (code, name_zh, name_en, description_zh, description_en) VALUES
('ADMIN', 'Administrator', 'Administrator', 'Has all permissions', 'Has all permissions'),
('OPERATOR', 'Operator', 'Operator', 'Can execute all dial test related operations', 'Can execute all dial test related operations'),
('BROWSER', 'Browser', 'Browser', 'View only', 'View only'),
('EXECUTOR', 'Executor', 'Executor', 'For executor registration', 'For executor registration')
ON CONFLICT (code) DO NOTHING;

-- 5. 插入默认管理员
INSERT INTO user_roles (username, role) VALUES ('admin', 'ADMIN')
ON CONFLICT (username, role) DO NOTHING;

-- 6. 关联操作记录表（operation_logs）
-- 用户角色管理模块的所有操作都会记录到operation_logs表
-- 操作类型：CREATE, UPDATE, DELETE, VIEW, PERMISSION_CHECK, PERMISSION_DENIED
-- 操作目标：USER_ROLE
-- 支持中英文操作描述存储

-- 7. 添加注释
COMMENT ON TABLE user_roles IS 'User role relationship table';
COMMENT ON TABLE roles IS 'Role enumeration table';

COMMENT ON COLUMN user_roles.id IS 'Primary key ID, auto increment';
COMMENT ON COLUMN user_roles.username IS 'Username';
COMMENT ON COLUMN user_roles.role IS 'Role code';

COMMENT ON COLUMN roles.id IS 'Primary key ID, auto increment';
COMMENT ON COLUMN roles.code IS 'Role code';
COMMENT ON COLUMN roles.name_zh IS 'Chinese name';
COMMENT ON COLUMN roles.name_en IS 'English name';
COMMENT ON COLUMN roles.description_zh IS 'Chinese description';
COMMENT ON COLUMN roles.description_en IS 'English description';

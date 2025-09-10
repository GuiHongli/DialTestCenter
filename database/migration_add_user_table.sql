-- 创建用户表的数据库迁移脚本
-- 包含username、password、last_login_time等字段

-- 1. 创建user表
CREATE TABLE IF NOT EXISTS "user" (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    last_login_time TIMESTAMP,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP
);

-- 2. 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_user_username ON "user"(username);
CREATE INDEX IF NOT EXISTS idx_user_created_time ON "user"(created_time);
CREATE INDEX IF NOT EXISTS idx_user_last_login_time ON "user"(last_login_time);

-- 3. 添加表注释
COMMENT ON TABLE "user" IS '用户表，存储用户基本信息';
COMMENT ON COLUMN "user".id IS '用户ID，主键';
COMMENT ON COLUMN "user".username IS '用户名，唯一标识';
COMMENT ON COLUMN "user".password IS '用户密码，加密存储';
COMMENT ON COLUMN "user".last_login_time IS '最后登录时间';
COMMENT ON COLUMN "user".created_time IS '创建时间';
COMMENT ON COLUMN "user".updated_time IS '更新时间';

-- 4. 插入默认管理员用户（密码为admin123，实际使用时应该修改）
INSERT INTO "user" (username, password, created_time) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- 5. 验证表创建结果
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'user' 
ORDER BY ordinal_position;

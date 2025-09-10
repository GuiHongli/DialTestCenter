-- 重命名user表为dial_user的数据库迁移脚本
-- 如果user表已存在，则重命名为dial_user

-- 1. 检查user表是否存在，如果存在则重命名
DO $$
BEGIN
    -- 检查user表是否存在
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user') THEN
        -- 重命名user表为dial_user
        ALTER TABLE "user" RENAME TO dial_user;
        
        -- 重命名相关索引
        IF EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_user_username') THEN
            ALTER INDEX idx_user_username RENAME TO idx_dial_user_username;
        END IF;
        
        IF EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_user_created_time') THEN
            ALTER INDEX idx_user_created_time RENAME TO idx_dial_user_created_time;
        END IF;
        
        IF EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_user_last_login_time') THEN
            ALTER INDEX idx_user_last_login_time RENAME TO idx_dial_user_last_login_time;
        END IF;
        
        -- 更新表注释
        COMMENT ON TABLE dial_user IS '用户表，存储用户基本信息';
        COMMENT ON COLUMN dial_user.id IS '用户ID，主键';
        COMMENT ON COLUMN dial_user.username IS '用户名，唯一标识';
        COMMENT ON COLUMN dial_user.password IS '用户密码，加密存储';
        COMMENT ON COLUMN dial_user.last_login_time IS '最后登录时间';
        COMMENT ON COLUMN dial_user.created_time IS '创建时间';
        COMMENT ON COLUMN dial_user.updated_time IS '更新时间';
        
        RAISE NOTICE 'Successfully renamed user table to dial_user';
    ELSE
        RAISE NOTICE 'User table does not exist, no action needed';
    END IF;
END $$;

-- 2. 验证重命名结果
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'dial_user' 
ORDER BY ordinal_position;

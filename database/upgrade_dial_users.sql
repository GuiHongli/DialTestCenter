-- ============================================
-- dial_users 表改造（统一执行机用户管理）
-- 目标：使用 dial_users 表统一支持前端管理和执行机 CHAP 认证
-- ============================================

-- 1. 添加唯一约束（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'dial_users_username_key'
    ) THEN
        ALTER TABLE dial_users 
        ADD CONSTRAINT dial_users_username_key UNIQUE (username);
    END IF;
END$$;

-- 2. 添加索引（如果不存在）
CREATE INDEX IF NOT EXISTS idx_dial_users_username ON dial_users(username);

-- 3. 更新表注释
COMMENT ON TABLE dial_users IS '执行机用户表，密码存储NTLM Hash用于CHAP认证';
COMMENT ON COLUMN dial_users.password IS 'NTLM Hash格式（32位16进制字符串）';
COMMENT ON COLUMN dial_users.username IS '用户名，唯一标识';
COMMENT ON COLUMN dial_users.last_login_time IS '最后登录时间';

-- 4. 数据迁移（如果存在 agent_user 表）
-- 选项A：从 agent_user 同步NTLM Hash到dial_users
DO $$
DECLARE
    migrated_count INTEGER;
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name = 'agent_user'
    ) THEN
        -- 同步NTLM Hash
        UPDATE dial_users du
        SET password = au.password
        FROM agent_user au
        WHERE du.username = au.username;
        
        GET DIAGNOSTICS migrated_count = ROW_COUNT;
        RAISE NOTICE 'Migrated % users NTLM Hash from agent_user to dial_users', migrated_count;
    ELSE
        RAISE NOTICE 'agent_user table does not exist, skipping migration';
    END IF;
END$$;

-- 5. 验证密码长度（可选）
-- 检查是否所有密码都是32位16进制字符串
DO $$
DECLARE
    invalid_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO invalid_count
    FROM dial_users
    WHERE LENGTH(password) != 32 OR password !~ '^[0-9A-Fa-f]{32}$';
    
    IF invalid_count > 0 THEN
        RAISE WARNING 'Found % users with non-NTLM Hash passwords. These users may need password reset.', invalid_count;
    ELSE
        RAISE NOTICE 'All passwords are valid NTLM Hash format (32-char hex string)';
    END IF;
END$$;

-- 6. 备份建议（在执行此脚本前，建议先备份）
-- pg_dump -t dial_users -t agent_user > backup_users_$(date +%Y%m%d).sql

-- 完成提示
DO $$
BEGIN
    RAISE NOTICE 'dial_users table upgrade completed successfully';
END$$;


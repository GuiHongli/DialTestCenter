-- 为执行机管理集成测试创建测试用户
-- 
-- 根据《执行机管理软件实现设计》文档：
-- - agent_user.password 字段存储 NTLM Hash
-- - CHAP 认证：Response = MD5(NTLM-Hash + Challenge)
--
-- 使用说明：
-- 1. 连接到 PostgreSQL 数据库
-- 2. 执行此脚本创建测试用户
-- 3. 记录 password 的值（NTLM Hash），用于设置 EXEC_AGENT_NTLM_HASH 环境变量
--
-- 测试用户密码：test123
-- 对应 NTLM Hash：cc03e747a6afbbcbf8be7668acfebee5
-- 
-- NTLM Hash 生成方法（Python）：
-- import hashlib
-- password = "test123"
-- ntlm_hash = hashlib.new('md4', password.encode('utf-16le')).hexdigest()
-- print(ntlm_hash)  # cc03e747a6afbbcbf8be7668acfebee5

-- 清理可能存在的测试用户
DELETE FROM agent_user WHERE username = 'test_agent';

-- 插入测试用户
-- username: test_agent
-- password: cc03e747a6afbbcbf8be7668acfebee5 (test123 的 NTLM Hash)
INSERT INTO agent_user (username, password, last_login_time)
VALUES ('test_agent', 'cc03e747a6afbbcbf8be7668acfebee5', NOW())
ON CONFLICT (username) DO UPDATE
SET password = EXCLUDED.password,
    last_login_time = EXCLUDED.last_login_time;

-- 验证插入
SELECT id, username, password, last_login_time 
FROM agent_user 
WHERE username = 'test_agent';

-- 提示信息
SELECT '测试用户创建成功！' as status,
       'test_agent' as username,
       'test123' as raw_password,
       'cc03e747a6afbbcbf8be7668acfebee5' as ntlm_hash,
       '请设置环境变量: export EXEC_AGENT_NTLM_HASH=cc03e747a6afbbcbf8be7668acfebee5' as instruction;


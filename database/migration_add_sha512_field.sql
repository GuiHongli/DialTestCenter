-- 添加SHA512字段的数据库迁移脚本
-- 执行时间：2025-09-08

-- 添加sha512字段到test_case_set表
ALTER TABLE test_case_set 
ADD COLUMN sha512 VARCHAR(128);

-- 添加注释
COMMENT ON COLUMN test_case_set.sha512 IS '文件内容的SHA512哈希值';

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_test_case_set_sha512 ON test_case_set(sha512);

-- 验证表结构
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default
FROM information_schema.columns 
WHERE table_name = 'test_case_set' 
ORDER BY ordinal_position;

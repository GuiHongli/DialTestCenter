-- 重命名 test_case_set 表中的 business 字段为 business_zh
-- 执行时间: 2025-09-23

-- 1. 重命名字段
ALTER TABLE test_case_set RENAME COLUMN business TO business_zh;

-- 2. 添加注释
COMMENT ON COLUMN test_case_set.business_zh IS '业务类型（中文）';

-- 3. 验证变更
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'test_case_set' 
AND column_name IN ('business_zh', 'business_en')
ORDER BY column_name;

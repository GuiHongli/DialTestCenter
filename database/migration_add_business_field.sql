-- 添加business字段的数据库迁移脚本
-- 执行时间：2025-09-08

-- 添加business字段到test_case_set表
ALTER TABLE test_case_set 
ADD COLUMN business VARCHAR(50) NOT NULL DEFAULT 'VPN阻断业务';

-- 添加注释
COMMENT ON COLUMN test_case_set.business IS '业务类型，如：VPN阻断业务';

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_test_case_set_business ON test_case_set(business);

-- 验证表结构
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default
FROM information_schema.columns 
WHERE table_name = 'test_case_set' 
ORDER BY ordinal_position;

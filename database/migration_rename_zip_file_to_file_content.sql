-- 将zip_file字段重命名为file_content的数据库迁移脚本
-- 执行时间：2025-09-08

-- 重命名字段
ALTER TABLE test_case_set 
RENAME COLUMN zip_file TO file_content;

-- 更新字段注释
COMMENT ON COLUMN test_case_set.file_content IS '文件内容（二进制数据）';

-- 验证表结构
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default
FROM information_schema.columns 
WHERE table_name = 'test_case_set' 
ORDER BY ordinal_position;

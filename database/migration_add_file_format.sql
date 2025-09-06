-- 添加文件格式字段的数据库迁移脚本
-- 执行时间：2024年

-- 添加 file_format 字段到 test_case_set 表
ALTER TABLE test_case_set 
ADD COLUMN file_format VARCHAR(10) NOT NULL DEFAULT 'zip';

-- 更新现有记录的 file_format 字段
-- 由于现有记录都是 zip 格式，所以设置为 'zip'
UPDATE test_case_set 
SET file_format = 'zip' 
WHERE file_format IS NULL OR file_format = '';

-- 添加注释
COMMENT ON COLUMN test_case_set.file_format IS '文件格式：zip 或 tar.gz';

-- 创建索引以提高查询性能（可选）
CREATE INDEX idx_test_case_set_file_format ON test_case_set(file_format);

-- 验证数据
SELECT 
    id, 
    name, 
    version, 
    file_format, 
    creator, 
    file_size, 
    created_time 
FROM test_case_set 
ORDER BY created_time DESC 
LIMIT 10;

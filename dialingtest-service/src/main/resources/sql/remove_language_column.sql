-- 删除operation_logs表中的language字段
-- 创建时间: 2024-01-15

-- 1. 删除language相关的索引
DROP INDEX IF EXISTS idx_operation_logs_language;
DROP INDEX IF EXISTS idx_operation_logs_language_time;

-- 2. 删除language相关的检查约束
ALTER TABLE operation_logs DROP CONSTRAINT IF EXISTS chk_language_description;

-- 3. 删除language字段
ALTER TABLE operation_logs DROP COLUMN IF EXISTS language;

-- 4. 添加新的检查约束
ALTER TABLE operation_logs 
ADD CONSTRAINT chk_description_not_empty 
CHECK (
    operation_description_zh IS NOT NULL OR operation_description_en IS NOT NULL
);

-- 5. 验证修改
SELECT 
    ol.id,
    ol.username,
    ol.operation_description_zh,
    ol.operation_description_en,
    ol.operation_type,
    ol.operation_target,
    ol.operation_time
FROM operation_logs ol
ORDER BY ol.operation_time DESC;

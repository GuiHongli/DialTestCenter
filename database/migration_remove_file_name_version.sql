-- 移除file_name和version字段的数据库迁移脚本
-- 将software_name字段长度调整为255以容纳完整文件名

-- 1. 删除file_name字段
ALTER TABLE software_package DROP COLUMN IF EXISTS file_name;

-- 2. 删除version字段
ALTER TABLE software_package DROP COLUMN IF EXISTS version;

-- 3. 调整software_name字段长度从200到255
ALTER TABLE software_package ALTER COLUMN software_name TYPE VARCHAR(255);

-- 4. 删除相关的索引（如果存在）
DROP INDEX IF EXISTS idx_software_package_file_name;
DROP INDEX IF EXISTS idx_software_package_version;

-- 5. 删除相关的唯一约束（如果存在）
ALTER TABLE software_package DROP CONSTRAINT IF EXISTS uk_software_package_file_name;

-- 6. 添加新的索引（如果需要）
CREATE INDEX IF NOT EXISTS idx_software_package_software_name ON software_package(software_name);

-- 7. 添加新的唯一约束（如果需要）
ALTER TABLE software_package ADD CONSTRAINT uk_software_package_software_name UNIQUE (software_name);

-- 8. 更新注释
COMMENT ON COLUMN software_package.software_name IS '软件名称（完整文件名，带后缀）';

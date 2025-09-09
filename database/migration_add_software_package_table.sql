-- 添加软件包管理表的数据库迁移脚本
-- 执行时间：2025-01-15

-- 创建软件包表
CREATE TABLE IF NOT EXISTS software_package (
    id BIGSERIAL PRIMARY KEY,
    software_name VARCHAR(200) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_content BYTEA NOT NULL,
    file_format VARCHAR(10) NOT NULL,
    sha512 VARCHAR(128),
    platform VARCHAR(20) NOT NULL,
    creator VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    description VARCHAR(1000),
    version VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 添加注释
COMMENT ON TABLE software_package IS '软件包管理表';
COMMENT ON COLUMN software_package.id IS '主键ID';
COMMENT ON COLUMN software_package.software_name IS '软件名称（从文件名解析）';
COMMENT ON COLUMN software_package.file_name IS '原始文件名（带后缀）';
COMMENT ON COLUMN software_package.file_content IS '文件内容（二进制数据）';
COMMENT ON COLUMN software_package.file_format IS '文件格式：apk 或 ipa';
COMMENT ON COLUMN software_package.sha512 IS '文件内容的SHA512哈希值';
COMMENT ON COLUMN software_package.platform IS '平台：android 或 ios';
COMMENT ON COLUMN software_package.creator IS '创建者';
COMMENT ON COLUMN software_package.file_size IS '文件大小（字节）';
COMMENT ON COLUMN software_package.description IS '描述信息';
COMMENT ON COLUMN software_package.version IS '软件版本（从文件名解析）';
COMMENT ON COLUMN software_package.created_time IS '创建时间';
COMMENT ON COLUMN software_package.updated_time IS '更新时间';

-- 创建唯一约束
ALTER TABLE software_package ADD CONSTRAINT uk_software_package_file_name UNIQUE (file_name);
ALTER TABLE software_package ADD CONSTRAINT uk_software_package_sha512 UNIQUE (sha512);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_software_package_software_name ON software_package (software_name);
CREATE INDEX IF NOT EXISTS idx_software_package_platform ON software_package (platform);
CREATE INDEX IF NOT EXISTS idx_software_package_creator ON software_package (creator);
CREATE INDEX IF NOT EXISTS idx_software_package_file_format ON software_package (file_format);
CREATE INDEX IF NOT EXISTS idx_software_package_created_time ON software_package (created_time);
CREATE INDEX IF NOT EXISTS idx_software_package_sha512 ON software_package (sha512);

-- 创建触发器
DROP TRIGGER IF EXISTS update_software_package_updated_time ON software_package;
CREATE TRIGGER update_software_package_updated_time
    BEFORE UPDATE ON software_package
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 验证表结构
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default
FROM information_schema.columns 
WHERE table_name = 'software_package' 
ORDER BY ordinal_position;

-- 插入示例软件包数据（可选）
-- 注意：由于file_content字段存储二进制数据，示例数据需要实际的文件内容
-- INSERT INTO software_package (software_name, file_name, file_content, file_format, platform, creator, file_size, sha512, version, description) VALUES 
-- ('微信', 'wechat_8.0.0.apk', '\x504b0304...', 'apk', 'android', 'admin', 102400000, 'abc123...', '8.0.0', '微信Android版本'),
-- ('抖音', 'douyin_20.0.0.ipa', '\x504b0304...', 'ipa', 'ios', 'admin', 150000000, 'def456...', '20.0.0', '抖音iOS版本')
-- ON CONFLICT (file_name) DO UPDATE SET updated_time = CURRENT_TIMESTAMP;

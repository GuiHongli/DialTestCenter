-- Dial Test Center 数据库初始化脚本
-- PostgreSQL

-- 创建数据库（如果不存在）
-- 注意：需要在PostgreSQL中手动执行：createdb dialtestcenter

-- 连接到数据库
\c dialingtest;

-- 创建用户角色关系表
CREATE TABLE IF NOT EXISTS user_role (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 添加注释
COMMENT ON TABLE user_role IS '用户角色关系表';
COMMENT ON COLUMN user_role.id IS '主键ID';
COMMENT ON COLUMN user_role.username IS '用户名';
COMMENT ON COLUMN user_role.role IS '角色：ADMIN/OPERATOR/BROWSER/EXECUTOR';
COMMENT ON COLUMN user_role.created_time IS '创建时间';
COMMENT ON COLUMN user_role.updated_time IS '更新时间';

-- 创建唯一约束
ALTER TABLE user_role ADD CONSTRAINT uk_username_role UNIQUE (username, role);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_username ON user_role (username);
CREATE INDEX IF NOT EXISTS idx_role ON user_role (role);

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_updated_time_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 创建触发器
DROP TRIGGER IF EXISTS update_user_role_updated_time ON user_role;
CREATE TRIGGER update_user_role_updated_time
    BEFORE UPDATE ON user_role
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 插入内置管理员账号
INSERT INTO user_role (username, role) VALUES 
('admin', 'ADMIN'),
('system', 'ADMIN')
ON CONFLICT (username, role) DO UPDATE SET updated_time = CURRENT_TIMESTAMP;

-- 插入示例用户角色数据（可选）
INSERT INTO user_role (username, role) VALUES 
('operator1', 'OPERATOR'),
('browser1', 'BROWSER'),
('executor1', 'EXECUTOR'),
('multi_role_user', 'ADMIN'),
('multi_role_user', 'OPERATOR')
ON CONFLICT (username, role) DO UPDATE SET updated_time = CURRENT_TIMESTAMP;

-- 创建用例集表
CREATE TABLE IF NOT EXISTS test_case_set (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    version VARCHAR(50) NOT NULL,
    zip_file BYTEA NOT NULL,
    creator VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    description VARCHAR(1000),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 添加注释
COMMENT ON TABLE test_case_set IS '用例集表';
COMMENT ON COLUMN test_case_set.id IS '主键ID';
COMMENT ON COLUMN test_case_set.name IS '用例集名称';
COMMENT ON COLUMN test_case_set.version IS '用例集版本';
COMMENT ON COLUMN test_case_set.zip_file IS 'ZIP文件内容';
COMMENT ON COLUMN test_case_set.creator IS '创建人';
COMMENT ON COLUMN test_case_set.file_size IS '文件大小（字节）';
COMMENT ON COLUMN test_case_set.description IS '描述';
COMMENT ON COLUMN test_case_set.created_time IS '创建时间';
COMMENT ON COLUMN test_case_set.updated_time IS '更新时间';

-- 创建唯一约束
ALTER TABLE test_case_set ADD CONSTRAINT uk_name_version UNIQUE (name, version);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_test_case_set_name ON test_case_set (name);
CREATE INDEX IF NOT EXISTS idx_test_case_set_creator ON test_case_set (creator);
CREATE INDEX IF NOT EXISTS idx_test_case_set_created_time ON test_case_set (created_time);

-- 创建触发器
DROP TRIGGER IF EXISTS update_test_case_set_updated_time ON test_case_set;
CREATE TRIGGER update_test_case_set_updated_time
    BEFORE UPDATE ON test_case_set
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 插入示例用例集数据（可选）
-- 注意：由于zip_file字段存储二进制数据，示例数据需要实际的文件内容
-- INSERT INTO test_case_set (name, version, zip_file, creator, file_size, description) VALUES 
-- ('短视频采集', 'v0.1', '\x504b0304...', 'admin', 1024000, '短视频采集测试用例集'),
-- ('直播推流', 'v1.0', '\x504b0304...', 'admin', 2048000, '直播推流测试用例集'),
-- ('音视频通话', 'v2.1', '\x504b0304...', 'operator1', 1536000, '音视频通话测试用例集')
-- ON CONFLICT (name, version) DO UPDATE SET updated_time = CURRENT_TIMESTAMP;

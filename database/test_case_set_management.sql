-- 拨测用例集管理模块数据库初始化脚本
-- 创建时间: 2025-01-27
-- 描述: 拨测用例集管理模块相关表结构

-- 删除已存在的表（按依赖关系顺序删除）
DROP TABLE IF EXISTS test_case CASCADE;
DROP TABLE IF EXISTS app_type CASCADE;
DROP TABLE IF EXISTS test_case_set CASCADE;

-- 1. 用例集表（test_case_set）
CREATE TABLE test_case_set (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    version VARCHAR(50) NOT NULL,
    file_content BYTEA NOT NULL,
    file_size BIGINT NOT NULL,
    description VARCHAR(1000),
    sha256 VARCHAR(64),
    business VARCHAR(50) DEFAULT 'VPN阻断',
    business_en VARCHAR(50) DEFAULT 'VPN_BLOCK',
    CONSTRAINT uk_name_version UNIQUE (name, version)
);

-- 2. 测试用例表（test_case）
CREATE TABLE test_case (
    id BIGSERIAL PRIMARY KEY,
    test_case_set_id BIGINT NOT NULL,
    case_name VARCHAR(200) NOT NULL,
    case_number VARCHAR(100) NOT NULL,
    test_steps TEXT,
    expected_result TEXT,
    business_category VARCHAR(200),
    app_name VARCHAR(200),
    dependencies_package TEXT,
    dependencies_rule TEXT,
    environment_config TEXT,
    script_exists BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_test_case_set_id FOREIGN KEY (test_case_set_id) REFERENCES test_case_set(id) ON DELETE CASCADE,
    CONSTRAINT uk_test_case_set_case_number UNIQUE (test_case_set_id, case_number)
);

-- 3. 应用类型表（app_type）
CREATE TABLE app_type (
    id BIGSERIAL PRIMARY KEY,
    business_category VARCHAR(200) NOT NULL,
    app_name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    CONSTRAINT uk_business_app UNIQUE (business_category, app_name)
);

-- 4. 创建索引
CREATE INDEX idx_test_case_set_name ON test_case_set (name);
CREATE INDEX idx_test_case_set_id ON test_case (test_case_set_id);
CREATE INDEX idx_case_number ON test_case (case_number);
CREATE INDEX idx_script_exists ON test_case (script_exists);
CREATE INDEX idx_app_type_category ON app_type (business_category);
CREATE INDEX idx_app_type_app ON app_type (app_name);

-- 5. 添加注释
COMMENT ON TABLE test_case_set IS 'Test case set table';
COMMENT ON TABLE test_case IS 'Test case table';
COMMENT ON TABLE app_type IS 'Application type table';

COMMENT ON COLUMN test_case_set.id IS 'Primary key ID, auto increment';
COMMENT ON COLUMN test_case_set.name IS 'Test case set name';
COMMENT ON COLUMN test_case_set.version IS 'Test case set version';
COMMENT ON COLUMN test_case_set.file_content IS 'File content (binary)';
COMMENT ON COLUMN test_case_set.file_size IS 'File size (bytes)';
COMMENT ON COLUMN test_case_set.description IS 'Description';
COMMENT ON COLUMN test_case_set.sha256 IS 'File SHA256 hash value';
COMMENT ON COLUMN test_case_set.business IS 'Business type (Chinese)';
COMMENT ON COLUMN test_case_set.business_en IS 'Business type (English)';

COMMENT ON COLUMN test_case.id IS 'Primary key ID, auto increment';
COMMENT ON COLUMN test_case.test_case_set_id IS 'Associated test case set ID';
COMMENT ON COLUMN test_case.case_name IS 'Test case name';
COMMENT ON COLUMN test_case.case_number IS 'Test case number';
COMMENT ON COLUMN test_case.test_steps IS 'Test steps';
COMMENT ON COLUMN test_case.expected_result IS 'Expected result';
COMMENT ON COLUMN test_case.business_category IS 'Business category';
COMMENT ON COLUMN test_case.app_name IS 'Application name';
COMMENT ON COLUMN test_case.dependencies_package IS 'Dependencies package';
COMMENT ON COLUMN test_case.dependencies_rule IS 'Dependencies rule';
COMMENT ON COLUMN test_case.environment_config IS 'Environment configuration (JSON)';
COMMENT ON COLUMN test_case.script_exists IS 'Script exists flag';

COMMENT ON COLUMN app_type.id IS 'Primary key ID, auto increment';
COMMENT ON COLUMN app_type.business_category IS 'Business category';
COMMENT ON COLUMN app_type.app_name IS 'Application name';
COMMENT ON COLUMN app_type.description IS 'Description';

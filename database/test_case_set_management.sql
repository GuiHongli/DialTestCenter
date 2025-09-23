-- 拨测用例集管理模块数据库初始化脚本
-- 创建时间: 2025-01-27
-- 描述: 拨测用例集管理模块相关表结构

-- 1. 用例集表（test_case_set）
CREATE TABLE IF NOT EXISTS test_case_set (
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
CREATE TABLE IF NOT EXISTS test_case (
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
    environment_config JSONB,
    script_exists BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_test_case_set_id FOREIGN KEY (test_case_set_id) REFERENCES test_case_set(id) ON DELETE CASCADE,
    CONSTRAINT uk_test_case_set_case_number UNIQUE (test_case_set_id, case_number)
);

-- 3. 应用类型表（app_type）
CREATE TABLE IF NOT EXISTS app_type (
    id BIGSERIAL PRIMARY KEY,
    business_category VARCHAR(200) NOT NULL,
    app_name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    CONSTRAINT uk_business_app UNIQUE (business_category, app_name)
);

-- 4. 创建索引
CREATE INDEX IF NOT EXISTS idx_test_case_set_name ON test_case_set (name);
CREATE INDEX IF NOT EXISTS idx_test_case_set_id ON test_case (test_case_set_id);
CREATE INDEX IF NOT EXISTS idx_case_number ON test_case (case_number);
CREATE INDEX IF NOT EXISTS idx_script_exists ON test_case (script_exists);
CREATE INDEX IF NOT EXISTS idx_app_type_category ON app_type (business_category);
CREATE INDEX IF NOT EXISTS idx_app_type_app ON app_type (app_name);

-- 5. 添加注释
COMMENT ON TABLE test_case_set IS '用例集表';
COMMENT ON TABLE test_case IS '测试用例表';
COMMENT ON TABLE app_type IS '应用类型表';

COMMENT ON COLUMN test_case_set.id IS '主键ID，自增';
COMMENT ON COLUMN test_case_set.name IS '用例集名称';
COMMENT ON COLUMN test_case_set.version IS '用例集版本';
COMMENT ON COLUMN test_case_set.file_content IS '文件内容（二进制）';
COMMENT ON COLUMN test_case_set.file_size IS '文件大小（字节）';
COMMENT ON COLUMN test_case_set.description IS '描述信息';
COMMENT ON COLUMN test_case_set.sha256 IS '文件SHA256哈希值';
COMMENT ON COLUMN test_case_set.business IS '业务类型（中文）';
COMMENT ON COLUMN test_case_set.business_en IS '业务类型（英文）';

COMMENT ON COLUMN test_case.id IS '主键ID，自增';
COMMENT ON COLUMN test_case.test_case_set_id IS '关联的用例集ID';
COMMENT ON COLUMN test_case.case_name IS '用例名称';
COMMENT ON COLUMN test_case.case_number IS '用例编号';
COMMENT ON COLUMN test_case.test_steps IS '测试步骤';
COMMENT ON COLUMN test_case.expected_result IS '预期结果';
COMMENT ON COLUMN test_case.business_category IS '业务大类';
COMMENT ON COLUMN test_case.app_name IS '应用名称';
COMMENT ON COLUMN test_case.dependencies_package IS '依赖的软件包';
COMMENT ON COLUMN test_case.dependencies_rule IS '依赖的规则';
COMMENT ON COLUMN test_case.environment_config IS '运行环境配置（JSON）';
COMMENT ON COLUMN test_case.script_exists IS '脚本是否存在';

COMMENT ON COLUMN app_type.id IS '主键ID，自增';
COMMENT ON COLUMN app_type.business_category IS '业务大类';
COMMENT ON COLUMN app_type.app_name IS '应用名称';
COMMENT ON COLUMN app_type.description IS '描述信息';

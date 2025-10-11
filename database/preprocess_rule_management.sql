-- 预处理规则管理模块数据库变更脚本
-- 创建时间：2025-01-27

-- 1. 创建预处理规则ZIP包表
CREATE TABLE preprocess_rule_packages (
    id BIGSERIAL PRIMARY KEY,
    package_name VARCHAR(200) NOT NULL,
    business_zh VARCHAR(100) NOT NULL,
    business_en VARCHAR(100) NOT NULL,
    file_content BYTEA NOT NULL,
    file_size BIGINT NOT NULL,
    description VARCHAR(1000),
    CONSTRAINT uk_package_name_business UNIQUE (package_name, business_zh)
);

-- 2. 创建预处理规则表
CREATE TABLE preprocess_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_name VARCHAR(200) NOT NULL,
    business_zh VARCHAR(100) NOT NULL,
    business_en VARCHAR(100) NOT NULL,
    category VARCHAR(100) NOT NULL,
    app_name VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_custom BOOLEAN NOT NULL DEFAULT FALSE,
    package_id BIGINT,
    CONSTRAINT uk_rule_name_business UNIQUE (rule_name, business_zh),
    CONSTRAINT fk_package_id FOREIGN KEY (package_id) REFERENCES preprocess_rule_packages(id) ON DELETE CASCADE
);

-- 3. 创建索引以提升查询性能
CREATE INDEX idx_preprocess_rule_packages_business ON preprocess_rule_packages(business_zh);
CREATE INDEX idx_preprocess_rule_packages_name ON preprocess_rule_packages(package_name);

CREATE INDEX idx_preprocess_rules_business ON preprocess_rules(business_zh);
CREATE INDEX idx_preprocess_rules_category ON preprocess_rules(category);
CREATE INDEX idx_preprocess_rules_app_name ON preprocess_rules(app_name);
CREATE INDEX idx_preprocess_rules_package_id ON preprocess_rules(package_id);

-- 4. 添加表注释
COMMENT ON TABLE preprocess_rule_packages IS '预处理规则ZIP包表';
COMMENT ON TABLE preprocess_rules IS '预处理规则表';

COMMENT ON COLUMN preprocess_rule_packages.id IS '主键ID';
COMMENT ON COLUMN preprocess_rule_packages.package_name IS 'ZIP包名称';
COMMENT ON COLUMN preprocess_rule_packages.business_zh IS '业务类型中文名称';
COMMENT ON COLUMN preprocess_rule_packages.business_en IS '业务类型英文名称';
COMMENT ON COLUMN preprocess_rule_packages.file_content IS 'ZIP包文件内容';
COMMENT ON COLUMN preprocess_rule_packages.file_size IS '文件大小';
COMMENT ON COLUMN preprocess_rule_packages.description IS '描述信息';

COMMENT ON COLUMN preprocess_rules.id IS '主键ID';
COMMENT ON COLUMN preprocess_rules.rule_name IS '规则名称';
COMMENT ON COLUMN preprocess_rules.business_zh IS '业务类型中文名称';
COMMENT ON COLUMN preprocess_rules.business_en IS '业务类型英文名称';
COMMENT ON COLUMN preprocess_rules.category IS '规则分类';
COMMENT ON COLUMN preprocess_rules.app_name IS '应用名称';
COMMENT ON COLUMN preprocess_rules.content IS '规则内容';
COMMENT ON COLUMN preprocess_rules.is_custom IS '是否自定义规则';
COMMENT ON COLUMN preprocess_rules.package_id IS '关联的ZIP包ID';


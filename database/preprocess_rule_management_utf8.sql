-- Preprocess Rule Management Module Database Schema
-- Created: 2025-01-27

-- 1. Create preprocess rule packages table
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

-- 2. Create preprocess rules table
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

-- 3. Create indexes for better query performance
CREATE INDEX idx_preprocess_rule_packages_business ON preprocess_rule_packages(business_zh);
CREATE INDEX idx_preprocess_rule_packages_name ON preprocess_rule_packages(package_name);

CREATE INDEX idx_preprocess_rules_business ON preprocess_rules(business_zh);
CREATE INDEX idx_preprocess_rules_category ON preprocess_rules(category);
CREATE INDEX idx_preprocess_rules_app_name ON preprocess_rules(app_name);
CREATE INDEX idx_preprocess_rules_package_id ON preprocess_rules(package_id);

-- 4. Add table comments
COMMENT ON TABLE preprocess_rule_packages IS 'Preprocess rule packages table';
COMMENT ON TABLE preprocess_rules IS 'Preprocess rules table';

COMMENT ON COLUMN preprocess_rule_packages.id IS 'Primary key ID';
COMMENT ON COLUMN preprocess_rule_packages.package_name IS 'Package name';
COMMENT ON COLUMN preprocess_rule_packages.business_zh IS 'Business type Chinese name';
COMMENT ON COLUMN preprocess_rule_packages.business_en IS 'Business type English name';
COMMENT ON COLUMN preprocess_rule_packages.file_content IS 'ZIP package file content';
COMMENT ON COLUMN preprocess_rule_packages.file_size IS 'File size';
COMMENT ON COLUMN preprocess_rule_packages.description IS 'Description';

COMMENT ON COLUMN preprocess_rules.id IS 'Primary key ID';
COMMENT ON COLUMN preprocess_rules.rule_name IS 'Rule name';
COMMENT ON COLUMN preprocess_rules.business_zh IS 'Business type Chinese name';
COMMENT ON COLUMN preprocess_rules.business_en IS 'Business type English name';
COMMENT ON COLUMN preprocess_rules.category IS 'Rule category';
COMMENT ON COLUMN preprocess_rules.app_name IS 'Application name';
COMMENT ON COLUMN preprocess_rules.content IS 'Rule content';
COMMENT ON COLUMN preprocess_rules.is_custom IS 'Is custom rule';
COMMENT ON COLUMN preprocess_rules.package_id IS 'Associated package ID';


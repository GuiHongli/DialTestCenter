-- 添加测试用例表的数据库迁移脚本
-- 执行时间：2025-01-27

-- 创建测试用例表
CREATE TABLE IF NOT EXISTS test_case (
    id BIGSERIAL PRIMARY KEY,
    test_case_set_id BIGINT NOT NULL,
    case_name VARCHAR(200) NOT NULL,
    case_number VARCHAR(100) NOT NULL,
    network_topology VARCHAR(500),
    business_category VARCHAR(200),
    app_name VARCHAR(200),
    test_steps TEXT,
    expected_result TEXT,
    script_exists BOOLEAN NOT NULL DEFAULT FALSE,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 添加注释
COMMENT ON TABLE test_case IS '测试用例表';
COMMENT ON COLUMN test_case.id IS '主键ID';
COMMENT ON COLUMN test_case.test_case_set_id IS '关联的用例集ID';
COMMENT ON COLUMN test_case.case_name IS '用例名称';
COMMENT ON COLUMN test_case.case_number IS '用例编号';
COMMENT ON COLUMN test_case.network_topology IS '逻辑组网';
COMMENT ON COLUMN test_case.business_category IS '业务大类';
COMMENT ON COLUMN test_case.app_name IS '用例App';
COMMENT ON COLUMN test_case.test_steps IS '测试步骤';
COMMENT ON COLUMN test_case.expected_result IS '预期结果';
COMMENT ON COLUMN test_case.script_exists IS '脚本是否存在';
COMMENT ON COLUMN test_case.created_time IS '创建时间';
COMMENT ON COLUMN test_case.updated_time IS '更新时间';

-- 创建外键约束
ALTER TABLE test_case 
ADD CONSTRAINT fk_test_case_set_id 
FOREIGN KEY (test_case_set_id) 
REFERENCES test_case_set(id) 
ON DELETE CASCADE;

-- 创建唯一约束
ALTER TABLE test_case 
ADD CONSTRAINT uk_test_case_set_case_number 
UNIQUE (test_case_set_id, case_number);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_test_case_set_id ON test_case (test_case_set_id);
CREATE INDEX IF NOT EXISTS idx_case_number ON test_case (case_number);
CREATE INDEX IF NOT EXISTS idx_case_name ON test_case (case_name);
CREATE INDEX IF NOT EXISTS idx_script_exists ON test_case (script_exists);
CREATE INDEX IF NOT EXISTS idx_test_case_created_time ON test_case (created_time);

-- 创建触发器
DROP TRIGGER IF EXISTS update_test_case_updated_time ON test_case;
CREATE TRIGGER update_test_case_updated_time
    BEFORE UPDATE ON test_case
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 验证表结构
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default
FROM information_schema.columns 
WHERE table_name = 'test_case' 
ORDER BY ordinal_position;

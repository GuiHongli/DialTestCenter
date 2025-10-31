-- =====================================================
-- 用例集校验功能数据库表脚本
-- =====================================================
-- 创建时间: 2025-10-30
-- 描述: 用例集校验任务和结果存储表
-- 数据库: PostgreSQL 16+
-- =====================================================

-- 删除已存在的表（如果存在）
DROP TABLE IF EXISTS test_case_set_validation_result CASCADE;
DROP TABLE IF EXISTS test_case_set_validation_task CASCADE;

-- =====================================================
-- 1. 校验任务表
-- =====================================================
CREATE TABLE test_case_set_validation_task (
    id BIGSERIAL PRIMARY KEY,
    test_case_set_id BIGINT NOT NULL,
    task_id VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    progress INTEGER DEFAULT 0,
    started_time TIMESTAMP,
    completed_time TIMESTAMP,
    error_message TEXT,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_test_case_set_validation_task FOREIGN KEY (test_case_set_id) 
        REFERENCES test_case_set(id) ON DELETE CASCADE,
    CONSTRAINT chk_task_status CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED'))
);

-- 创建索引
CREATE INDEX idx_validation_task_test_case_set_id ON test_case_set_validation_task(test_case_set_id);
CREATE INDEX idx_validation_task_task_id ON test_case_set_validation_task(task_id);
CREATE INDEX idx_validation_task_status ON test_case_set_validation_task(status);
CREATE INDEX idx_validation_task_created_time ON test_case_set_validation_task(created_time);

-- 添加表注释
COMMENT ON TABLE test_case_set_validation_task IS '用例集校验任务表';
COMMENT ON COLUMN test_case_set_validation_task.id IS '主键ID';
COMMENT ON COLUMN test_case_set_validation_task.test_case_set_id IS '用例集ID';
COMMENT ON COLUMN test_case_set_validation_task.task_id IS '任务ID（UUID）';
COMMENT ON COLUMN test_case_set_validation_task.status IS '任务状态：PENDING/RUNNING/COMPLETED/FAILED/CANCELLED';
COMMENT ON COLUMN test_case_set_validation_task.progress IS '任务进度（0-100）';
COMMENT ON COLUMN test_case_set_validation_task.started_time IS '任务开始执行时间';
COMMENT ON COLUMN test_case_set_validation_task.completed_time IS '任务完成时间';
COMMENT ON COLUMN test_case_set_validation_task.error_message IS '错误信息（任务失败时）';
COMMENT ON COLUMN test_case_set_validation_task.created_time IS '任务创建时间';

-- =====================================================
-- 2. 校验结果表（存储完整的校验结果JSON）
-- =====================================================
CREATE TABLE test_case_set_validation_result (
    id BIGSERIAL PRIMARY KEY,
    test_case_set_id BIGINT NOT NULL UNIQUE,
    task_id VARCHAR(64) NOT NULL,
    validation_result JSONB NOT NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_test_case_set_validation_result FOREIGN KEY (test_case_set_id) 
        REFERENCES test_case_set(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_validation_result_test_case_set_id ON test_case_set_validation_result(test_case_set_id);
CREATE INDEX idx_validation_result_task_id ON test_case_set_validation_result(task_id);
CREATE INDEX idx_validation_result_created_time ON test_case_set_validation_result(created_time);

-- 添加表注释
COMMENT ON TABLE test_case_set_validation_result IS '用例集校验结果表（JSON格式存储）';
COMMENT ON COLUMN test_case_set_validation_result.id IS '主键ID';
COMMENT ON COLUMN test_case_set_validation_result.test_case_set_id IS '用例集ID（唯一）';
COMMENT ON COLUMN test_case_set_validation_result.task_id IS '关联的任务ID';
COMMENT ON COLUMN test_case_set_validation_result.validation_result IS '校验结果JSON数据';
COMMENT ON COLUMN test_case_set_validation_result.created_time IS '创建时间';
COMMENT ON COLUMN test_case_set_validation_result.updated_time IS '更新时间';

-- =====================================================
-- 3. 创建更新updated_time的触发器函数（如果不存在）
-- =====================================================
CREATE OR REPLACE FUNCTION update_validation_result_updated_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
DROP TRIGGER IF EXISTS trigger_update_validation_result_updated_time ON test_case_set_validation_result;
CREATE TRIGGER trigger_update_validation_result_updated_time
    BEFORE UPDATE ON test_case_set_validation_result
    FOR EACH ROW
    EXECUTE FUNCTION update_validation_result_updated_time();

-- =====================================================
-- 4. 初始化操作类型枚举（如果不存在）
-- =====================================================
-- 检查并插入VALIDATE操作类型
INSERT INTO operation_types (code, name_zh, name_en, description_zh, description_en, is_active) 
SELECT 'VALIDATE', '校验', 'Validate', '执行校验操作', 'Perform validation operation', true
WHERE NOT EXISTS (
    SELECT 1 FROM operation_types WHERE code = 'VALIDATE'
);


-- Task Executor Mapping Table
-- Purpose: Store the mapping between tasks and executors/UEs for task assignment and cancellation

CREATE TABLE IF NOT EXISTS task_executor_mapping (
    id BIGSERIAL PRIMARY KEY,
    task_id VARCHAR(128) NOT NULL,
    executor_name VARCHAR(40),
    ue_serial VARCHAR(64),
    assign_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_task_executor_mapping_task_id ON task_executor_mapping (task_id);
CREATE INDEX IF NOT EXISTS idx_task_executor_mapping_executor_name ON task_executor_mapping (executor_name);

COMMENT ON TABLE task_executor_mapping IS '任务与执行机映射表';
COMMENT ON COLUMN task_executor_mapping.id IS '主键ID';
COMMENT ON COLUMN task_executor_mapping.task_id IS '任务ID';
COMMENT ON COLUMN task_executor_mapping.executor_name IS '执行机名称';
COMMENT ON COLUMN task_executor_mapping.ue_serial IS 'UE设备序列号';
COMMENT ON COLUMN task_executor_mapping.assign_time IS '分配时间';


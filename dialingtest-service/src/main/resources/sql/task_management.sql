-- 任务管理模块数据库初始化脚本（Task / TemplateTask）
-- 创建时间: 2025-10-24

-- 为避免外键/自引用冲突，先删表
DROP TABLE IF EXISTS task CASCADE;
DROP TABLE IF EXISTS template_task CASCADE;

-- 模板任务表（对应 TemplateDao）
CREATE TABLE template_task (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    cron VARCHAR(128) NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    input TEXT,
    creator VARCHAR(128) NOT NULL,
    description TEXT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_template_task_name ON template_task(name);
CREATE INDEX IF NOT EXISTS idx_template_task_enabled ON template_task(is_enabled);

COMMENT ON TABLE template_task IS 'Template task table';
COMMENT ON COLUMN template_task.cron IS 'Cron expression for scheduling';
COMMENT ON COLUMN template_task.is_enabled IS 'Whether the template is enabled';

-- 任务表（对应 TaskDao）
CREATE TABLE task (
    id BIGSERIAL PRIMARY KEY,
    main_task_id BIGINT,
    parent_task_id BIGINT,
    creator VARCHAR(128) NOT NULL,
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(16) NOT NULL,
    result VARCHAR(16),
    input TEXT,
    output TEXT,
    context TEXT,
    template_task_id BIGINT,
    CONSTRAINT fk_task_parent FOREIGN KEY (parent_task_id) REFERENCES task(id) ON DELETE SET NULL,
    CONSTRAINT fk_task_template FOREIGN KEY (template_task_id) REFERENCES template_task(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_task_main_task_id ON task(main_task_id);
CREATE INDEX IF NOT EXISTS idx_task_parent_task_id ON task(parent_task_id);
CREATE INDEX IF NOT EXISTS idx_task_status ON task(status);
CREATE INDEX IF NOT EXISTS idx_task_start_time ON task(start_time);

COMMENT ON TABLE task IS 'Dialing test task table (main/sub tasks)';
COMMENT ON COLUMN task.main_task_id IS 'Main task ID (self id for main tasks)';
COMMENT ON COLUMN task.parent_task_id IS 'Parent task ID (for sub tasks)';
COMMENT ON COLUMN task.template_task_id IS 'Related template task ID';

-- 可选：初始化一条示例模板，便于本地验证
-- INSERT INTO template_task(name, cron, is_enabled, input, creator, description)
-- VALUES('Weekly VPN Validation', '0 0 1 ? * 1', TRUE, '{"scenario":"VALIDATION"}', 'admin', 'Run weekly validation');



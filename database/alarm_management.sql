-- 告警管理模块数据库表
-- 创建告警表

-- 告警表
CREATE TABLE IF NOT EXISTS alarms (
    id BIGSERIAL PRIMARY KEY,                                    -- 主键ID，自增长
    alarm_summary VARCHAR(200) NOT NULL,                        -- 告警概述，最大长度200字符
    alarm_description TEXT,                                      -- 告警详细描述
    alarm_level VARCHAR(20) NOT NULL,                           -- 告警等级：Urgent/Important/Minor
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- 告警发生时间，精确到秒
    end_time TIMESTAMP                                          -- 告警结束时间，为空表示告警未结束
);

-- 添加检查约束：确保告警等级为有效值
ALTER TABLE alarms 
ADD CONSTRAINT chk_alarm_level 
CHECK (alarm_level IN ('Urgent', 'Important', 'Minor'));

-- 添加检查约束：确保结束时间不早于开始时间
ALTER TABLE alarms 
ADD CONSTRAINT chk_time_order 
CHECK (end_time IS NULL OR end_time >= start_time);

-- 创建索引：优化查询性能
CREATE INDEX IF NOT EXISTS idx_alarms_start_time ON alarms(start_time DESC);
CREATE INDEX IF NOT EXISTS idx_alarms_end_time ON alarms(end_time);
CREATE INDEX IF NOT EXISTS idx_alarms_level ON alarms(alarm_level);

-- 插入测试数据（可选）
-- INSERT INTO alarms (alarm_summary, alarm_description, alarm_level, start_time, end_time) VALUES
-- ('系统CPU使用率过高', '系统CPU使用率超过90%，持续5分钟', 'Urgent', '2024-01-15 10:00:00', NULL),
-- ('磁盘空间不足', '磁盘使用率超过85%', 'Important', '2024-01-15 11:00:00', NULL),
-- ('网络延迟异常', '网络延迟超过100ms', 'Minor', '2024-01-15 09:00:00', '2024-01-15 09:30:00'),
-- ('内存使用率过高', '内存使用率超过80%', 'Important', '2024-01-14 15:00:00', '2024-01-14 16:00:00');


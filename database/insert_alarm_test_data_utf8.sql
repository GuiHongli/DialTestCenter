-- 插入告警测试数据（UTF-8编码）
-- 包含不同级别（Urgent、Important、Minor）和不同状态（已结束、未结束）
-- 注意：执行此脚本时请确保客户端编码为UTF8

-- 设置客户端编码为UTF8
SET client_encoding = 'UTF8';

-- 清空现有测试数据（可选，如果需要重新插入）
-- DELETE FROM alarms;

-- 插入20条测试数据
INSERT INTO alarms (alarm_summary, alarm_description, alarm_level, start_time, end_time) VALUES
-- Urgent级别告警（7条）
('系统CPU使用率过高', '系统CPU使用率超过95%，持续10分钟，可能导致服务中断', 'Urgent', NOW() - INTERVAL '2 hours', NULL),
('数据库连接池耗尽', '数据库连接池使用率达到100%，无法建立新连接', 'Urgent', NOW() - INTERVAL '1 hour 30 minutes', NULL),
('主服务器宕机', '主服务器无响应，已切换到备用服务器', 'Urgent', NOW() - INTERVAL '5 hours', NOW() - INTERVAL '4 hours 30 minutes'),
('磁盘空间严重不足', '系统盘使用率超过98%，剩余空间不足1GB', 'Urgent', NOW() - INTERVAL '1 day', NOW() - INTERVAL '23 hours'),
('内存泄漏严重', '应用内存使用持续增长，已超过8GB限制', 'Urgent', NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days 12 hours'),
('关键服务异常', '支付服务无法正常处理请求，影响业务', 'Urgent', NOW() - INTERVAL '6 hours', NULL),
('网络中断', '核心网络链路中断，导致部分区域无法访问', 'Urgent', NOW() - INTERVAL '12 hours', NOW() - INTERVAL '11 hours 30 minutes'),

-- Important级别告警（8条）
('磁盘空间不足', '数据盘使用率超过85%，建议清理历史数据', 'Important', NOW() - INTERVAL '4 hours', NULL),
('API响应时间过长', '用户查询接口平均响应时间超过3秒', 'Important', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day 18 hours'),
('缓存命中率下降', 'Redis缓存命中率从95%降至75%，影响性能', 'Important', NOW() - INTERVAL '8 hours', NULL),
('数据库慢查询增多', '检测到10条执行时间超过5秒的慢查询', 'Important', NOW() - INTERVAL '1 day 6 hours', NOW() - INTERVAL '1 day 2 hours'),
('应用日志异常', '应用日志中出现大量ERROR级别日志', 'Important', NOW() - INTERVAL '3 hours', NULL),
('备份任务失败', '定时备份任务执行失败，需要手动检查', 'Important', NOW() - INTERVAL '2 days 12 hours', NOW() - INTERVAL '2 days 10 hours'),
('SSL证书即将过期', 'SSL证书将在30天后过期，需要及时更新', 'Important', NOW() - INTERVAL '5 days', NULL),
('数据库连接数接近上限', '数据库当前连接数达到最大连接数的90%', 'Important', NOW() - INTERVAL '6 hours', NOW() - INTERVAL '5 hours 30 minutes'),

-- Minor级别告警（5条）
('网络延迟轻微增加', '网络延迟从50ms增加到80ms，仍在可接受范围', 'Minor', NOW() - INTERVAL '1 hour', NULL),
('磁盘IO使用率偏高', '磁盘IO使用率达到70%，建议监控', 'Minor', NOW() - INTERVAL '1 day 3 hours', NOW() - INTERVAL '1 day 1 hour'),
('日志文件过大', '应用日志文件大小超过500MB，建议归档', 'Minor', NOW() - INTERVAL '4 days', NULL),
('定时任务执行时间延长', '数据同步任务执行时间从5分钟延长至8分钟', 'Minor', NOW() - INTERVAL '2 days 8 hours', NOW() - INTERVAL '2 days 6 hours'),
('监控指标波动', 'CPU使用率出现小幅波动，需要持续观察', 'Minor', NOW() - INTERVAL '3 hours', NULL);


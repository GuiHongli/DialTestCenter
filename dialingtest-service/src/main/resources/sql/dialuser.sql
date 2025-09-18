-- 删除已存在的dial_users表
DROP TABLE IF EXISTS dial_users;

-- 创建拨测用户表
CREATE TABLE dial_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    last_login_time TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_dial_users_username ON dial_users(username);
CREATE INDEX idx_dial_users_last_login_time ON dial_users(last_login_time);

-- 插入测试数据
INSERT INTO dial_users (username, password, last_login_time) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', NOW()),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', NOW());

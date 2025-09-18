-- 删除已存在的dialuser表
DROP TABLE IF EXISTS dialuser;

-- 创建拨测用户表
CREATE TABLE dialuser (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    last_login_time TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_dialuser_username ON dialuser(username);
CREATE INDEX idx_dialuser_last_login_time ON dialuser(last_login_time);

-- 插入测试数据
INSERT INTO dialuser (username, password, last_login_time) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', NOW()),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', NOW());

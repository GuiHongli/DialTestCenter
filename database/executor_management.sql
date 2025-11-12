-- Executor Management V2 schema

-- agent_user table: stores NTLM hash for CHAP auth
CREATE TABLE IF NOT EXISTS agent_user (
    id SERIAL PRIMARY KEY,
    username VARCHAR(40) UNIQUE NOT NULL,
    password VARCHAR(128) NOT NULL,
    last_login_time TIMESTAMP NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_agent_user_username ON agent_user (username);

-- executor table: stores registered executors
CREATE TABLE IF NOT EXISTS executor (
    name VARCHAR(40) PRIMARY KEY,
    ip VARCHAR(40),
    token VARCHAR(256),
    proxy VARCHAR(256),
    description TEXT,
    status SMALLINT,
    last_online_time TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_executor_status_last_online_time ON executor (status, last_online_time DESC);

-- ue table: stores UE devices bound to executors
CREATE TABLE IF NOT EXISTS ue (
    msisdn VARCHAR(15) PRIMARY KEY,
    executor_name VARCHAR(40) REFERENCES executor(name) ON UPDATE CASCADE ON DELETE SET NULL,
    vendor VARCHAR(128),
    os VARCHAR(128),
    info TEXT,
    task_info TEXT
);
CREATE INDEX IF NOT EXISTS idx_ue_executor_name ON ue (executor_name);
CREATE INDEX IF NOT EXISTS idx_ue_vendor ON ue (vendor);



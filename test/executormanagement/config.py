import os
from datetime import datetime
from common.config import (
    get_log_dir,
    build_logging_config,
    load_database_config,
    load_request_config,
    load_base_url,
)

# API 配置（统一从 common 读取，兼容 EXEC_API_BASE_URL 与 API_BASE_URL）
BASE_URL = load_base_url(preferred_env_vars=['EXEC_API_BASE_URL'], default_base='https://localhost:8087/dialingtest')
API_ENDPOINTS = {
    'LIST_EXECUTORS': '/api/executors',
    'REFRESH_EXECUTOR': '/api/executors/refresh',
}

# WebSocket 配置
WS_URL = os.getenv('EXEC_WS_URL', 'wss://localhost:8087/dialingtest/ws/executor')
WS_ENABLE = os.getenv('EXEC_WS_ENABLE', '1') == '1'  # 改为 '1' 默认启用

# Agent 凭据（用于 SC-01/02/07 的真实用例）
AGENT_NAME = os.getenv('EXEC_AGENT_NAME', 'Executor_PC_001')
AGENT_USERNAME = os.getenv('EXEC_AGENT_USERNAME', 'test_agent')
# Agent NTLM Hash（数据库中 agent_user.password 字段的值，十六进制字符串）
# 默认值对应数据库中的测试用户：username=test_agent, password(NTLM Hash)=cc03e747a6afbbcbf8be7668acfebee5
# 对应的明文密码是：test123
AGENT_NTLM_HASH = os.getenv('EXEC_AGENT_NTLM_HASH', 'cc03e747a6afbbcbf8be7668acfebee5')

# WS 超时与等待配置（秒）
WS_TIMEOUT = int(os.getenv('EXEC_WS_TIMEOUT', '10'))
WS_WAIT_OFFLINE_SEC = int(os.getenv('EXEC_WS_WAIT_OFFLINE_SEC', '5'))

# 数据库配置
DATABASE_CONFIG = load_database_config()

# 请求/日志配置（与 taskmanagement 对齐）
REQUEST_CONFIG = load_request_config(env_prefix='EXEC')

LOG_DIR = get_log_dir()
LOGGING_CONFIG = build_logging_config(prefix='exec_test', debug=False)

# 性能基准（毫秒）
PERFORMANCE_BASELINE = {
    'list_executors': 200,
    'refresh_executor': 300,
    'register_auth_handshake': 300,
    'heartbeat': 100,
}



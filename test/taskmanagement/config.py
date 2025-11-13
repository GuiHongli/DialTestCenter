# -*- coding: utf-8 -*-
"""
集成测试配置模块

定义全局配置、API端点、数据库连接等
"""

import os
from datetime import datetime
from common.config import (
    get_log_dir,
    build_logging_config,
    load_database_config,
    load_request_config,
    load_base_url,
)

# ==================== 环境配置 ====================
ENV = os.getenv('TEST_ENV', 'local')
DEBUG = os.getenv('DEBUG', 'False').lower() == 'true'

# ==================== API配置 ====================
BASE_URL = load_base_url()
API_PREFIX = '/api'

# ==================== API端点 ====================
API_ENDPOINTS = {
    'START_TASK': f'{API_PREFIX}/tasks/start',
    'GET_TASKS': f'{API_PREFIX}/tasks',
    'GET_TASK_BY_ID': f'{API_PREFIX}/tasks/{{task_id}}',
    'STOP_TASK': f'{API_PREFIX}/tasks/{{task_id}}/stop',
    'CREATE_TEMPLATE': f'{API_PREFIX}/templates',
    'GET_TEMPLATES': f'{API_PREFIX}/templates',
    'GET_TEMPLATE_BY_ID': f'{API_PREFIX}/templates/{{template_id}}',
    'UPDATE_TEMPLATE': f'{API_PREFIX}/templates/{{template_id}}',
    'DELETE_TEMPLATE': f'{API_PREFIX}/templates/{{template_id}}',
    'NOTIFY_CALLBACK': f'{API_PREFIX}/callbacks/notify',
}

# ==================== 数据库配置 ====================
DATABASE_CONFIG = load_database_config()

# ==================== 请求配置 ====================
REQUEST_CONFIG = load_request_config()

# ==================== 测试数据 ====================
TEST_DATA = {
    'business_types': ['VPN_BLOCK', 'APP_DETECT'],
    'scenarios': ['VALIDATION', 'TRAINING'],
    'test_user': 'test_user_001',
    'admin_user': 'admin',
}

# ==================== 超时配置 ====================
TIMEOUTS = {
    'api_response': 30,  # API响应超时（秒）
    'state_transition': 5,  # 状态转换等待超时（秒）
    'db_query': 10,  # 数据库查询超时（秒）
}

# ==================== 日志配置 ====================
LOG_DIR = get_log_dir()
LOGGING_CONFIG = build_logging_config(prefix='test', debug=DEBUG)

# ==================== 性能基准 ====================
PERFORMANCE_BASELINE = {
    'create_task': 700,  # 毫秒
    'query_task': 100,
    'query_task_list': 200,
    'create_template': 300,
    'callback_notify': 1000,
}

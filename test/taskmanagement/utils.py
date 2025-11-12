# -*- coding: utf-8 -*-
"""
集成测试工具函数模块

提供API请求、数据验证、日志等辅助函数
"""

import json
import time
import logging
from typing import Dict, Any, Optional, List
from datetime import datetime
import requests

from .config import (
    BASE_URL, API_ENDPOINTS, DATABASE_CONFIG, REQUEST_CONFIG,
    TIMEOUTS, LOGGING_CONFIG, PERFORMANCE_BASELINE
)
from common.http import APIClient as _CommonAPIClient
from common.db import BaseDatabaseHelper
from common.helpers import (
    measure_response_time_seconds as _measure_response_time_seconds,
    wait_for_condition as _wait_for_condition,
    json_to_string as _json_to_string,
)

# ==================== 日志配置 ====================
logging.basicConfig(
    level=LOGGING_CONFIG['level'],
    format=LOGGING_CONFIG['format'],
    handlers=[
        logging.FileHandler(LOGGING_CONFIG['file']),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)


# ==================== API请求函数 ====================
class APIClient(_CommonAPIClient):
    """REST API客户端（复用 common.http.APIClient，注入本模块请求配置）"""

    def __init__(self, base_url: str = BASE_URL):
        super().__init__(base_url=base_url, request_config=REQUEST_CONFIG)


# ==================== 断言函数 ====================
class Assertions:
    """测试断言辅助类"""

    @staticmethod
    def assert_status_code(response: requests.Response, expected_code: int):
        """验证状态码"""
        assert response.status_code == expected_code, \
            f"Expected status {expected_code}, got {response.status_code}. Response: {response.text}"
        logger.info(f"OK Status code verified: {expected_code}")

    @staticmethod
    def assert_json_field(response: requests.Response, field_path: str, expected_value: Any = None):
        """验证JSON字段"""
        data = response.json()
        keys = field_path.split('.')
        value = data
        
        for key in keys:
            assert key in value, f"Field '{key}' not found in response"
            value = value[key]
        
        if expected_value is not None:
            assert value == expected_value, \
                f"Field '{field_path}' expected {expected_value}, got {value}"
        
        logger.info(f"OK JSON field verified: {field_path} = {value}")

    @staticmethod
    def assert_json_field_not_null(response: requests.Response, field_path: str):
        """验证JSON字段不为空"""
        data = response.json()
        keys = field_path.split('.')
        value = data
        
        for key in keys:
            assert key in value, f"Field '{key}' not found in response"
            value = value[key]
        
        assert value is not None, f"Field '{field_path}' should not be null"
        logger.info(f"OK JSON field not null verified: {field_path}")

    @staticmethod
    def assert_response_time(response_time: float, baseline_ms: int):
        """验证响应时间"""
        response_time_ms = response_time * 1000
        assert response_time_ms < baseline_ms, \
            f"Response time {response_time_ms:.0f}ms exceeds baseline {baseline_ms}ms"
        logger.info(f"OK Response time verified: {response_time_ms:.0f}ms < {baseline_ms}ms")


# ==================== 数据库函数 ====================
class DatabaseHelper(BaseDatabaseHelper):
    """数据库操作辅助类（复用 common.db.BaseDatabaseHelper，添加领域查询）"""

    def __init__(self, config: Dict[str, Any] = DATABASE_CONFIG):
        super().__init__(config=config)

    def get_task_by_id(self, task_id: int) -> Optional[Dict[str, Any]]:
        results = self.execute_query(
            "SELECT * FROM task WHERE id = %s",
            (task_id,),
        )
        return results[0] if results else None

    def get_latest_task(self) -> Optional[Dict[str, Any]]:
        results = self.execute_query(
            "SELECT * FROM task ORDER BY id DESC LIMIT 1",
        )
        return results[0] if results else None


# ==================== 测试数据生成 ====================
class TestDataBuilder:
    """测试数据构建器"""

    @staticmethod
    def build_start_task_request(scenario: str = 'VALIDATION',
                                 business_type: str = 'VPN_BLOCK',
                                 failed_apps: Optional[List[str]] = None) -> Dict:
        """构建启动任务请求"""
        return {
            'business_type': business_type,
            'scenario': scenario,
            'script_names': ['vpn_app_001.py', 'vpn_app_002.py'],
            'target_ues': ['ue_serial_12345', 'ue_serial_12346'],
            'failed_apps': failed_apps
        }

    @staticmethod
    def build_create_template_request(name: str, cron: str = '0 0 1 ? * 1',
                                      enabled: bool = True) -> Dict:
        """构建创建模板请求"""
        return {
            'name': name,
            'cron': cron,
            'enabled': enabled,
            'input': json.dumps({
                'business_type': 'VPN_BLOCK',
                'scenario': 'VALIDATION'
            }),
            'creator': 'admin',
            'description': f'Test template: {name}'
        }

    @staticmethod
    def build_callback_request(task_id: int, status: str = 'SUCCESS',
                               result_data: Optional[Dict] = None) -> Dict:
        """构建异步回调请求"""
        if result_data is None:
            result_data = {'modelName': 'model-v1.2.3'}
        
        return {
            'mainTaskId': task_id,
            'status': status,
            'resultData': result_data
        }


# ==================== 辅助函数 ====================
def measure_response_time(func, *args, **kwargs) -> tuple:
    """测量函数执行时间（单位：秒）"""
    return _measure_response_time_seconds(func, *args, **kwargs)


def wait_for_condition(condition_func, timeout: int = 5, interval: float = 0.5) -> bool:
    """等待条件满足"""
    return _wait_for_condition(condition_func, timeout=timeout, interval=interval)


def json_to_string(data: Dict) -> str:
    """将字典转为JSON字符串（用于输出）"""
    return _json_to_string(data)

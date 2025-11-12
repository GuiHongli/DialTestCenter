# -*- coding: utf-8 -*-
"""
SC-01: 人工启动拨测任务完整流程

测试用例：
  TC-01-001: 成功启动VPN拨测任务（验证场景）
  TC-01-002: 启动训练场景拨测任务
  TC-01-003: 参数验证失败
"""

import json
from taskmanagement.base import BaseTestCase
from taskmanagement.utils import measure_response_time
from taskmanagement.config import PERFORMANCE_BASELINE


class TestStartTask(BaseTestCase):
    """任务启动测试套件"""

    def setUp(self):
        super().setUp()
        # 清空旧数据
        self.truncate_task_table()

    # ==================== TC-01-001 ====================
    def test_tc_01_001_start_validation_task_success(self):
        """
        TC-01-001: 成功启动VPN拨测任务（验证场景）
        
        场景：用户通过POST /api/tasks/start启动验证场景的拨测任务
        期望：返回202 Accepted，包含新建的TaskEntity
        """
        # Arrange - 构造请求
        request_data = self.data_builder.build_start_task_request(
            scenario='VALIDATION',
            failed_apps=None
        )

        # Act - 发送请求
        response, response_time = measure_response_time(
            self.start_task,
            scenario='VALIDATION'
        )

        # Assert - 验证HTTP状态码
        self.assertions.assert_status_code(response, 202)

        # Assert - 验证响应字段
        self.assertions.assert_json_field_not_null(response, 'id')
        self.assertions.assert_json_field_not_null(response, 'mainTaskId')
        self.assertions.assert_json_field(response, 'status', 'RUNNING')
        self.assertions.assert_json_field(response, 'templateTaskId', None)
        
        # Assert - 验证context字段
        response_data = response.json()
        context = json.loads(response_data['context'])
        self.assertEqual(context['step'], 'START_VALIDATION')

        # Assert - 验证startTime
        self.assertions.assert_json_field_not_null(response, 'startTime')

        # Assert - 验证响应时间
        self.assertions.assert_response_time(
            response_time,
            PERFORMANCE_BASELINE['create_task']
        )

        # 验证数据库
        task_id = response_data['id']
        task_from_db = self.get_task_from_db(task_id)
        self.assertIsNotNone(task_from_db)
        self.assertEqual(task_from_db['main_task_id'], task_id)  # 自引用验证
        self.assertEqual(task_from_db['status'], 'RUNNING')

    # ==================== TC-01-002 ====================
    def test_tc_01_002_start_training_task_with_failed_apps(self):
        """
        TC-01-002: 启动训练场景拨测任务
        
        场景：用户启动训练场景的拨测任务，并指定失败的应用
        期望：context.step初始化为START_TRAINING_DIALING，failedApps被序列化
        """
        # Arrange
        failed_apps = ['app_vpn_001', 'app_vpn_002']

        # Act
        response = self.start_task(
            scenario='TRAINING',
            failed_apps=failed_apps
        )

        # Assert
        self.assertions.assert_status_code(response, 202)
        self.assertions.assert_json_field(response, 'status', 'RUNNING')

        # Assert - 验证context中的step
        response_data = response.json()
        context = json.loads(response_data['context'])
        self.assertEqual(context['step'], 'START_TRAINING_DIALING')

        # Assert - 验证failedApps被保存
        self.assertIn('app_vpn_001', str(response_data['input']))
        self.assertIn('app_vpn_002', str(response_data['input']))

    # ==================== TC-01-003 ====================
    def test_tc_01_003_start_task_with_null_business_type(self):
        """
        TC-01-003: 参数验证失败
        
        场景：businessType字段为null
        期望：返回400 Bad Request，不创建任务
        """
        # Arrange
        invalid_data = {
            'business_type': None,  # 无效
            'scenario': 'VALIDATION',
            'script_names': ['vpn_app_001.py'],
            'target_ues': ['ue_serial_12345'],
            'failed_apps': None
        }

        # Act
        response = self.api.post('/api/tasks/start', invalid_data)

        # Assert
        self.assertions.assert_status_code(response, 400)

        # 验证未创建新任务
        latest_task = self.get_latest_task_from_db()
        self.assertIsNone(latest_task)

    # ==================== TC-01-004 (额外) ====================
    def test_tc_01_004_start_task_with_empty_script_names(self):
        """
        TC-01-004: scriptNames为空列表（额外覆盖）
        
        期望：返回400 Bad Request
        """
        # Arrange
        invalid_data = {
            'business_type': 'VPN_BLOCK',
            'scenario': 'VALIDATION',
            'script_names': [],  # 空列表
            'target_ues': ['ue_serial_12345'],
        }

        # Act
        response = self.api.post('/api/tasks/start', invalid_data)

        # Assert
        self.assertIn(response.status_code, [400, 422])

    # ==================== TC-01-005 (额外) ====================
    def test_tc_01_005_start_task_with_empty_target_ues(self):
        """
        TC-01-005: targetUes为空列表（额外覆盖）
        
        期望：返回400 Bad Request
        """
        # Arrange
        invalid_data = {
            'business_type': 'VPN_BLOCK',
            'scenario': 'VALIDATION',
            'script_names': ['vpn_app_001.py'],
            'target_ues': [],  # 空列表
        }

        # Act
        response = self.api.post('/api/tasks/start', invalid_data)

        # Assert
        self.assertIn(response.status_code, [400, 422])

    # ==================== TC-01-006 (额外) ====================
    def test_tc_01_006_start_task_scenario_case_sensitivity(self):
        """
        TC-01-006: scenario参数不同大小写（额外覆盖）
        
        期望：应该规范化处理或返回400
        """
        # Arrange
        data = {
            'business_type': 'VPN_BLOCK',
            'scenario': 'validation',  # 小写
            'script_names': ['vpn_app_001.py'],
            'target_ues': ['ue_serial_12345'],
        }

        # Act
        response = self.api.post('/api/tasks/start', data)

        # Assert - 可能返回400或自动转换为大写
        if response.status_code == 202:
            # 自动转换的情况
            response_data = response.json()
            context = json.loads(response_data['context'])
            # step应该正确初始化
            self.assertIn(context['step'], ['START_VALIDATION', 'START_TRAINING_DIALING'])
        else:
            # 严格验证的情况
            self.assertEqual(response.status_code, 400)


if __name__ == '__main__':
    import unittest
    unittest.main()

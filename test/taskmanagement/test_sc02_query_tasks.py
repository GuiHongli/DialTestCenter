# -*- coding: utf-8 -*-
"""
SC-02: 查询主任务列表和详情

测试用例：
  TC-02-001: 分页查询主任务列表
  TC-02-002: 查询具体任务详情
  TC-02-003: 查询不存在的任务
  TC-02-004: 分页参数边界测试
"""

import json
import unittest

from taskmanagement.base import BaseTestCase
from taskmanagement.utils import measure_response_time
from taskmanagement.config import PERFORMANCE_BASELINE


class TestQueryTasks(BaseTestCase):
    """任务查询测试套件"""

    def setUp(self):
        super().setUp()
        # 清空旧数据
        self.truncate_task_table()

    # ==================== TC-02-001 ====================
    def test_tc_02_001_query_tasks_pagination(self):
        """
        TC-02-001: 分页查询主任务列表

        前置：创建5条任务记录
        期望：200 OK，content按ID降序，totalElements>=5
        """
        # Arrange - 创建多条任务
        for _ in range(5):
            self.start_task(scenario='VALIDATION')

        # Act
        response, response_time = measure_response_time(
            self.get_tasks,
            page=0,
            size=20
        )

        # Assert - 基本结构与状态码
        self.assertions.assert_status_code(response, 200)
        self.assertions.assert_response_time(
            response_time,
            PERFORMANCE_BASELINE['query_task_list']
        )

        data = response.json()
        self.assertIn('content', data)
        self.assertIn('totalElements', data)
        self.assertGreaterEqual(data['totalElements'], 5)
        self.assertGreaterEqual(len(data['content']), 5)

        # Assert - 按ID降序
        ids = [item['id'] for item in data['content']]
        self.assertEqual(ids, sorted(ids, reverse=True))

        # 每个任务包含context（序列化JSON字符串）
        for item in data['content']:
            self.assertIn('context', item)
            # 可解析
            if item['context']:
                json.loads(item['context'])

    # ==================== TC-02-002 ====================
    def test_tc_02_002_query_task_detail(self):
        """
        TC-02-002: 查询具体任务详情
        """
        # Arrange
        create_resp = self.start_task(scenario='VALIDATION')
        self.assertions.assert_status_code(create_resp, 202)
        task_id = create_resp.json()['id']

        # Act
        response, response_time = measure_response_time(
            self.get_task_by_id,
            task_id
        )

        # Assert
        self.assertions.assert_status_code(response, 200)
        self.assertions.assert_response_time(
            response_time,
            PERFORMANCE_BASELINE['query_task']
        )

        data = response.json()
        self.assertEqual(data['id'], task_id)
        self.assertIn('mainTaskId', data)
        self.assertIn('status', data)
        self.assertIn('context', data)
        ctx = json.loads(data['context'])
        self.assertIn('step', ctx)
        # 验证场景：应初始化为验证场景的起始步骤
        self.assertEqual(ctx['step'], 'START_VALIDATION')

    # ==================== TC-02-003 ====================
    def test_tc_02_003_query_task_not_found(self):
        """
        TC-02-003: 查询不存在的任务
        """
        response = self.get_task_by_id(999999)
        self.assertions.assert_status_code(response, 404)

    # ==================== TC-02-004 ====================
    def test_tc_02_004_pagination_bounds(self):
        """
        TC-02-004: 分页参数边界测试（page<0, size<=0 应安全处理）
        """
        # Arrange
        for _ in range(2):
            self.start_task(scenario='VALIDATION')

        # Act
        response = self.api.get('/api/tasks', params={'page': -1, 'size': -5})

        # Assert
        self.assertions.assert_status_code(response, 200)
        data = response.json()
        self.assertIn('content', data)
        self.assertIn('size', data)
        self.assertIn('number', data)


if __name__ == '__main__':
    unittest.main()



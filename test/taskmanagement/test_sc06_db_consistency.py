# -*- coding: utf-8 -*-
"""
SC-06: 数据库一致性和约束验证

测试用例：
  TC-06-001: 主任务ID自引用验证
  TC-06-002: Context字段JSON格式验证
  TC-06-003: 时间戳字段自动设置
"""

import json
import unittest

from taskmanagement.base import BaseTestCase


class TestDbConsistency(BaseTestCase):
    """数据库一致性与约束测试套件"""

    def setUp(self):
        super().setUp()
        self.truncate_task_table()
        self.truncate_template_table()

    # ==================== TC-06-001 ====================
    def test_tc_06_001_main_task_id_self_reference(self):
        """
        TC-06-001: 主任务ID自引用验证
        """
        # Arrange
        resp = self.start_task(scenario='VALIDATION')
        self.assertions.assert_status_code(resp, 202)
        task_id = resp.json()['id']

        # Act
        row = self.get_task_from_db(task_id)

        # Assert
        self.assertIsNotNone(row)
        self.assertEqual(row['main_task_id'], task_id)

    # ==================== TC-06-002 ====================
    def test_tc_06_002_context_json_validity(self):
        """
        TC-06-002: Context字段JSON格式验证
        """
        # Arrange
        for _ in range(3):
            self.start_task(scenario='VALIDATION')

        # Act
        rows = self.db.execute_query("SELECT id, context FROM task ORDER BY id DESC LIMIT 3")

        # Assert
        self.assertEqual(len(rows), 3)
        for r in rows:
            self.assertIsNotNone(r.get('context'))
            ctx = json.loads(r['context'])
            self.assertIn('step', ctx)
            self.assertTrue(isinstance(ctx['step'], str) and len(ctx['step']) > 0)

    # ==================== TC-06-003 ====================
    def test_tc_06_003_timestamp_auto_set(self):
        """
        TC-06-003: 时间戳字段自动设置
        """
        # 验证 Task 表时间戳
        resp = self.start_task(scenario='VALIDATION')
        self.assertions.assert_status_code(resp, 202)
        created_id = resp.json()['id']
        latest = self.db.execute_query("SELECT id, start_time, end_time, status FROM task WHERE id = %s", (created_id,))
        self.assertEqual(len(latest), 1)
        r = latest[0]
        self.assertIsNotNone(r['start_time'])
        self.assertEqual(r['status'], 'RUNNING')
        self.assertIsNone(r['end_time'])

        # 验证 Template 表时间戳
        create_tpl_resp = self.create_template(name='时间戳模板')
        self.assertions.assert_status_code(create_tpl_resp, 201)
        tpl_id = create_tpl_resp.json()['id']
        tpl_rows = self.db.execute_query(
            "SELECT id, create_time, update_time FROM template_task WHERE id = %s",
            (tpl_id,)
        )
        self.assertEqual(len(tpl_rows), 1)
        tpl = tpl_rows[0]
        self.assertIsNotNone(tpl['create_time'])
        # 新建后未更新
        self.assertIsNone(tpl['update_time'])


if __name__ == '__main__':
    unittest.main()



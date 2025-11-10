# -*- coding: utf-8 -*-
"""
SC-04: 异步回调和状态机转换

测试用例：
  TC-04-001: 异步任务完成回调（SUCCESS）
  TC-04-002: 异步任务失败回调（FAILED）
  TC-04-003: 重复回调幂等性
  TC-04-004: 无效的主任务ID
"""

import json
import unittest

from taskmanagement.base import BaseTestCase
from taskmanagement.utils import wait_for_condition


class TestCallbacks(BaseTestCase):
    """回调与状态机转换测试套件"""

    def setUp(self):
        super().setUp()
        self.truncate_task_table()

    # 辅助：直接插入指定上下文的主任务
    def _insert_task_with_context(self, context_dict) -> int:
        ctx = json.dumps(context_dict, ensure_ascii=False)
        rows = self.db.execute_query(
            """
            INSERT INTO task (main_task_id, parent_task_id, creator, start_time, status, result, input, output, context, template_task_id)
            VALUES (NULL, NULL, 'system', now(), 'RUNNING', NULL, '{}', NULL, %s::text, NULL)
            RETURNING id
            """,
            (ctx,)
        )
        task_id = rows[0]['id']
        # 自引用
        self.db.execute_update("UPDATE task SET main_task_id = id WHERE id = %s", (task_id,))
        return task_id

    # ==================== TC-04-001 ====================
    def test_tc_04_001_callback_success_transitions_state(self):
        """
        TC-04-001: 异步任务完成回调（SUCCESS）
        期望：HTTP 200；状态机从 START_MODEL_TRAIN -> START_MODEL_REPLAY
        """
        # Arrange
        task_id = self._insert_task_with_context({
            'step': 'START_MODEL_TRAIN',
            'async_job_id': 'job-model-train-001'
        })

        # Act
        response = self.notify_callback(
            task_id=task_id,
            status='SUCCESS',
            result_data={'modelName': 'model-v1.2.3', 'accuracy': 0.96}
        )

        # Assert - 立即验证
        self.assertions.assert_status_code(response, 200)

        # 延迟验证：等待状态机持久化
        def _state_changed():
            row = self.get_task_from_db(task_id)
            if not row or not row.get('context'):
                return False
            ctx = json.loads(row['context'])
            return ctx.get('step') == 'START_MODEL_REPLAY'

        ok = wait_for_condition(_state_changed, timeout=5, interval=0.2)
        self.assertTrue(ok, '状态未转换到 START_MODEL_REPLAY')

    # ==================== TC-04-002 ====================
    def test_tc_04_002_callback_failed_to_final(self):
        """
        TC-04-002: 异步任务失败回调（FAILED）
        期望：HTTP 200；context.step = FINAL；status 更新为 COMPLETED/FAILED；output含错误
        """
        # Arrange
        task_id = self._insert_task_with_context({
            'step': 'START_MODEL_TRAIN',
            'async_job_id': 'job-model-train-002'
        })

        # Act
        response = self.notify_callback(
            task_id=task_id,
            status='FAILED',
            result_data={'errorMessage': '模型训练失败：内存不足', 'errorCode': 'OUT_OF_MEMORY'}
        )

        # Assert
        self.assertions.assert_status_code(response, 200)

        def _finalized():
            row = self.get_task_from_db(task_id)
            if not row:
                return False
            ctx = json.loads(row['context']) if row.get('context') else {}
            status_ok = row.get('status') in ('COMPLETED', 'FAILED')
            step_ok = ctx.get('step') == 'FINAL'
            output_ok = (row.get('output') or '').find('内存不足') != -1 or (row.get('result') in ('FAILED',))
            return status_ok and step_ok and output_ok

        ok = wait_for_condition(_finalized, timeout=5, interval=0.2)
        self.assertTrue(ok, '失败回调后未进入 FINAL 或未写入错误信息')

    # ==================== TC-04-003 ====================
    def test_tc_04_003_callback_idempotency(self):
        """
        TC-04-003: 重复回调幂等性
        期望：第二次相同回调返回200，数据库状态不变
        """
        # Arrange：准备处于 START_MODEL_REPLAY 的任务
        task_id = self._insert_task_with_context({
            'step': 'START_MODEL_REPLAY',
            'async_job_id': None
        })

        # Act：重复回调 SUCCESS
        resp1 = self.notify_callback(task_id=task_id, status='SUCCESS', result_data={'modelName': 'model-v1.2.3'})
        self.assertions.assert_status_code(resp1, 200)

        # 记录当前上下文
        before = self.get_task_from_db(task_id)
        ctx_before = json.loads(before['context']) if before and before.get('context') else {}

        resp2 = self.notify_callback(task_id=task_id, status='SUCCESS', result_data={'modelName': 'model-v1.2.3'})
        self.assertions.assert_status_code(resp2, 200)

        after = self.get_task_from_db(task_id)
        ctx_after = json.loads(after['context']) if after and after.get('context') else {}
        self.assertEqual(ctx_after.get('step'), ctx_before.get('step'))

    # ==================== TC-04-004 ====================
    def test_tc_04_004_callback_invalid_main_task_id(self):
        """
        TC-04-004: 无效的主任务ID
        期望：404 Not Found
        """
        response = self.notify_callback(task_id=99999, status='SUCCESS', result_data={})
        self.assertions.assert_status_code(response, 404)


if __name__ == '__main__':
    unittest.main()



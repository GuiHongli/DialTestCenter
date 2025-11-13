import unittest
import time

from .base import BaseTestCase
from .config import WS_ENABLE, AGENT_NTLM_HASH
from .tlv_codec import MessageType, encode_task_stop_response


@unittest.skipUnless(WS_ENABLE, "WS测试默认关闭，设置 EXEC_WS_ENABLE=1 以启用")
class TestTaskStopIT09(BaseTestCase):
    """IT-09: 任务停止流程"""

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_09_001_task_stop_request(self):
        """IT-09-001: 任务停止响应（Agent发送TaskStop-Response）"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 注意：TaskStop-Request是CloudUDN主动发送给Agent的，这里只测试Agent的响应
            # 模拟Agent响应任务停止成功
            task_id = "TASK_STOP_TEST_001"
            stop_msg = encode_task_stop_response(task_id, 0)  # 0=成功停止
            self._ws_send_tlv(ws, stop_msg)
            
            # 验证消息发送成功（没有异常）
            time.sleep(0.2)

        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_09_002_task_stop_nonexistent(self):
        """IT-09-002: 停止不存在的任务"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 模拟Agent响应停止一个不存在的任务（返回失败）
            stop_msg = encode_task_stop_response("NON_EXISTENT_TASK", 1)  # 1=失败
            self._ws_send_tlv(ws, stop_msg)
            time.sleep(0.2)

            # 系统应该妥善处理，不应该崩溃

        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_09_003_task_stop_after_completion(self):
        """IT-09-003: 任务完成后停止"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 模拟任务已完成的情况
            task_id = "COMPLETED_TASK_001"

            # 模拟Agent响应停止请求（任务已完成，返回失败）
            stop_msg = encode_task_stop_response(task_id, 1)  # 1=失败（任务已完成）
            self._ws_send_tlv(ws, stop_msg)
            time.sleep(0.2)

            # 系统应该妥善处理

        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_09_004_multiple_task_stop(self):
        """IT-09-004: 多次停止同一任务"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            task_id = "MULTI_STOP_TASK"

            # 多次发送停止响应
            for i in range(3):
                stop_msg = encode_task_stop_response(task_id, 0)  # 0=成功
                self._ws_send_tlv(ws, stop_msg)
                time.sleep(0.1)  # 小延迟

        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_09_005_task_stop_during_execution(self):
        """IT-09-005: 执行期间停止任务"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 模拟任务正在执行
            task_id = "RUNNING_TASK_001"

            # 模拟Agent响应停止正在执行的任务（成功）
            stop_msg = encode_task_stop_response(task_id, 0)  # 0=成功
            self._ws_send_tlv(ws, stop_msg)
            time.sleep(0.2)

        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_09_006_task_stop_multiple_tasks(self):
        """IT-09-006: 停止多个任务"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 停止多个不同的任务
            task_ids = ["TASK_A", "TASK_B", "TASK_C"]

            for task_id in task_ids:
                stop_msg = encode_task_stop_response(task_id, 0)  # 0=成功
                self._ws_send_tlv(ws, stop_msg)
                time.sleep(0.1)

        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_09_007_task_stop_with_deregister(self):
        """IT-09-007: 注销时停止所有任务"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 模拟有多个任务在运行
            running_tasks = ["TASK_RUNNING_1", "TASK_RUNNING_2"]

            # 发送注销请求（应该停止所有任务）
            from .tlv_codec import encode_deregister_request
            deregister_msg = encode_deregister_request("TestExecutor")
            self._ws_send_tlv(ws, deregister_msg)

        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_09_008_task_stop_error_handling(self):
        """IT-09-008: 任务停止错误处理"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 测试各种边界情况
            invalid_task_ids = ["", "   ", None]

            for task_id in invalid_task_ids:
                if task_id is not None:
                    try:
                        stop_msg = encode_task_stop_response(task_id, 1)  # 1=失败
                        self._ws_send_tlv(ws, stop_msg)
                    except Exception:
                        # 预期的错误情况
                        pass

        finally:
            ws.close()

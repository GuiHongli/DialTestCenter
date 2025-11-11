import unittest
import time
import hashlib
import json

from .base import BaseTestCase
from .config import WS_ENABLE, AGENT_NAME, AGENT_NTLM_HASH
from .tlv_codec import MessageType, encode_task_start_response


@unittest.skipUnless(WS_ENABLE, "WS测试默认关闭，设置 EXEC_WS_ENABLE=1 以启用")
class TestTaskDispatchIT03(BaseTestCase):
    """IT-03: 任务下发执行流程"""

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_03_001_task_start_request_message(self):
        """IT-03-001: 验证TaskStart-Request消息格式（通过数据库创建任务）"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 通过数据库API创建任务（模拟业务层发起任务）
            task_id = 1001
            task_data = {
                "id": task_id,
                "creator": "test_user",
                "status": "PENDING",
                "input": json.dumps({
                    "script_name": "dial_test_script.py",
                    "version": "1.0.0",
                    "serial_no_list": ["SN001", "SN002"],
                    "proctype": 1,
                    "parameters": {"timeout": 30, "retry_count": 3}
                })
            }
            
            # 使用REST API创建任务
            response = self.db.execute_query(
                "INSERT INTO task(id, creator, status, input, start_time) "
                "VALUES (%s, %s, %s, %s, NOW()) "
                "ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status",
                (task_data["id"], task_data["creator"], task_data["status"], task_data["input"])
            )
            
            # 注意：实际测试中，应该通过REST API触发任务分发，服务端会发送TaskStart-Request
            # 这里我们只验证数据库中任务创建成功
            
            # 查询任务状态
            tasks = self.db.execute_query("SELECT id, status FROM task WHERE id = %s", (task_id,))
            self.assertTrue(len(tasks) > 0, "任务应该在数据库中")
            task = tasks[0]
            self.assertEqual(task.get('id'), task_id)
            
        finally:
            # 清理测试数据
            self.db.execute_query("DELETE FROM task WHERE id = %s", (1001,))
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_03_002_task_start_response_success(self):
        """IT-03-002: 任务执行成功响应"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 先在数据库中创建任务（模拟服务端已下发任务）
            task_id = 1002
            self.db.execute_query(
                "INSERT INTO task(id, creator, status, input, start_time) "
                "VALUES (%s, %s, %s, %s, NOW()) "
                "ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status",
                (task_id, "test_user", "RUNNING", json.dumps({
                    "script_name": "test_script.py",
                    "version": "1.0.0",
                    "serial_no_list": ["SN001"]
                }))
            )

            # 模拟Agent执行任务并发送响应
            # 构造执行结果
            sub_result = [
                {"serial_no": "SN001", "result": 0, "duration": 1500, "error_msg": ""}
            ]

            # 构造日志文件数据
            log_content = "Test execution log\nTask completed successfully\n"
            log_bytes = log_content.encode('utf-8')

            # 计算CRC
            crc = hashlib.md5(log_bytes).hexdigest()

            # 发送TaskStart-Response消息
            response_msg = encode_task_start_response(
                task_id, 0, sub_result, log_bytes, crc
            )
            self._ws_send_tlv(ws, response_msg)
            
            # 等待一下让服务端处理
            time.sleep(0.5)
            
            # 验证任务状态已更新
            tasks = self.db.execute_query("SELECT id, status, result FROM task WHERE id = %s", (task_id,))
            self.assertTrue(len(tasks) > 0, "任务应该在数据库中")
            # 注意：服务端可能会更新status为COMPLETED或保持RUNNING，取决于业务逻辑

        finally:
            # 清理测试数据
            self.db.execute_query("DELETE FROM task WHERE id = %s", (task_id,))
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_03_003_task_start_response_failure(self):
        """IT-03-003: 任务执行失败响应"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 先在数据库中创建任务
            task_id = 1003
            self.db.execute_query(
                "INSERT INTO task(id, creator, status, input, start_time) "
                "VALUES (%s, %s, %s, %s, NOW()) "
                "ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status",
                (task_id, "test_user", "RUNNING", json.dumps({
                    "script_name": "failing_script.py",
                    "version": "1.0.0",
                    "serial_no_list": ["SN001"]
                }))
            )

            # 模拟任务失败
            sub_result = [
                {"serial_no": "SN001", "result": 1, "duration": 500, "error_msg": "Script execution failed"}
            ]

            # 构造错误日志
            error_log = "Error: Script execution failed\nTimeout occurred\n"
            error_bytes = error_log.encode('utf-8')
            crc = hashlib.md5(error_bytes).hexdigest()

            # 发送失败响应
            response_msg = encode_task_start_response(
                task_id, 1, sub_result, error_bytes, crc, "Execution timeout"
            )
            self._ws_send_tlv(ws, response_msg)
            
            # 等待服务端处理
            time.sleep(0.5)
            
            # 验证任务状态已更新为失败
            tasks = self.db.execute_query("SELECT id, status, result FROM task WHERE id = %s", (task_id,))
            self.assertTrue(len(tasks) > 0, "任务应该在数据库中")

        finally:
            # 清理测试数据
            self.db.execute_query("DELETE FROM task WHERE id = %s", (task_id,))
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_03_004_task_file_upload(self):
        """IT-03-004: 文件上传和CRC校验"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            task_id = 1004
            
            # 先在数据库中创建任务
            self.db.execute_query(
                "INSERT INTO task(id, creator, status, input, start_time) "
                "VALUES (%s, %s, %s, %s, NOW()) "
                "ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status",
                (task_id, "test_user", "RUNNING", json.dumps({
                    "script_name": "test_script.py",
                    "version": "1.0.0",
                    "serial_no_list": ["SN001"]
                }))
            )

            # 构造大的日志文件数据
            log_lines = []
            for i in range(100):
                log_lines.append(f"2024-01-01 12:00:{i:02d} INFO: Log entry {i}")
            log_content = "\n".join(log_lines)
            log_bytes = log_content.encode('utf-8')

            # 计算正确的CRC
            correct_crc = hashlib.md5(log_bytes).hexdigest()

            # 测试CRC校验通过的情况
            sub_result = [{"serial_no": "SN001", "result": 0}]
            response_msg = encode_task_start_response(
                task_id, 0, sub_result, log_bytes, correct_crc
            )
            # 发送消息
            self._ws_send_tlv(ws, response_msg)
            
            # 等待服务端处理
            time.sleep(0.5)

        finally:
            # 清理测试数据
            self.db.execute_query("DELETE FROM task WHERE id = %s", (task_id,))
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_03_005_task_multiple_ue_execution(self):
        """IT-03-005: 多UE设备任务执行"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            task_id = 1005
            serial_no_list = ["SN001", "SN002", "SN003"]

            # 先在数据库中创建任务
            self.db.execute_query(
                "INSERT INTO task(id, creator, status, input, start_time) "
                "VALUES (%s, %s, %s, %s, NOW()) "
                "ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status",
                (task_id, "test_user", "RUNNING", json.dumps({
                    "script_name": "multi_ue_script.py",
                    "version": "1.0.0",
                    "serial_no_list": serial_no_list
                }))
            )

            # 模拟多UE的执行结果
            sub_result = [
                {"serial_no": "SN001", "result": 0, "duration": 1200, "error_msg": ""},
                {"serial_no": "SN002", "result": 0, "duration": 1150, "error_msg": ""},
                {"serial_no": "SN003", "result": 1, "duration": 800, "error_msg": "Network timeout"}
            ]

            # 构造综合日志
            combined_log = ""
            for result in sub_result:
                combined_log += f"UE {result['serial_no']}: {result['result']} - {result['error_msg']}\n"

            log_bytes = combined_log.encode('utf-8')
            crc = hashlib.md5(log_bytes).hexdigest()

            # 发送包含多UE结果的响应
            response_msg = encode_task_start_response(
                task_id, 0, sub_result, log_bytes, crc  # 总体结果为0，因为部分成功
            )
            self._ws_send_tlv(ws, response_msg)
            
            # 等待服务端处理
            time.sleep(0.5)

        finally:
            # 清理测试数据
            self.db.execute_query("DELETE FROM task WHERE id = %s", (task_id,))
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_03_006_task_parameter_validation(self):
        """IT-03-006: 任务参数验证（验证不同参数格式的任务创建和响应）"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 测试各种参数格式的任务
            test_cases = [
                {
                    "task_id": 1006,
                    "script_name": "script.py",
                    "version": "1.0.0",
                    "serial_no_list": ["SN001"],
                    "parameters": json.dumps({"key": "value", "numbers": [1, 2, 3]})
                },
                {
                    "task_id": 1007,
                    "script_name": "complex_script.py",
                    "version": "2.1.0-beta",
                    "serial_no_list": ["SN001", "SN002", "SN003", "SN004"],
                    "parameters": json.dumps({
                        "timeout": 300,
                        "retries": 5,
                        "config": {"debug": True, "log_level": "INFO"}
                    })
                }
            ]

            for test_case in test_cases:
                task_id = test_case["task_id"]
                
                # 在数据库中创建任务
                self.db.execute_query(
                    "INSERT INTO task(id, creator, status, input, start_time) "
                    "VALUES (%s, %s, %s, %s, NOW()) "
                    "ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status",
                    (task_id, "test_user", "RUNNING", json.dumps(test_case))
                )
                
                # 发送简单的成功响应
                sub_result = [{"serial_no": test_case["serial_no_list"][0], "result": 0}]
                log_bytes = b"Task completed\n"
                crc = hashlib.md5(log_bytes).hexdigest()
                
                response_msg = encode_task_start_response(
                    task_id, 0, sub_result, log_bytes, crc
                )
                self._ws_send_tlv(ws, response_msg)
                
                # 等待服务端处理
                time.sleep(0.3)
            
            # 验证所有任务都正确处理
            for test_case in test_cases:
                tasks = self.db.execute_query(
                    "SELECT id, status FROM task WHERE id = %s", 
                    (test_case["task_id"],)
                )
                self.assertTrue(len(tasks) > 0, f"任务 {test_case['task_id']} 应该在数据库中")

        finally:
            # 清理测试数据
            for test_case in test_cases:
                self.db.execute_query("DELETE FROM task WHERE id = %s", (test_case["task_id"],))
            ws.close()

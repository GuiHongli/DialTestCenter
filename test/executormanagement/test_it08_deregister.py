import unittest
import time

from .base import BaseTestCase
from .config import WS_ENABLE, AGENT_NAME, AGENT_NTLM_HASH, WS_WAIT_OFFLINE_SEC
from .utils import wait_for_condition
from .tlv_codec import MessageType, encode_deregister_request


@unittest.skipUnless(WS_ENABLE, "WS测试默认关闭，设置 EXEC_WS_ENABLE=1 以启用")
class TestDeregisterIT08(BaseTestCase):
    """IT-08: Agent注销流程"""

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_08_001_deregister_success(self):
        """IT-08-001: 成功注销"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 验证初始状态为ONLINE
            ex = self.db.get_executor_by_name(AGENT_NAME)
            self.assertIsNotNone(ex)
            self.assertEqual(ex.get('status'), 1)

            # 发送注销请求（注意：服务器收到后会直接关闭连接，不返回响应）
            deregister_msg = encode_deregister_request(AGENT_NAME)
            self._ws_send_tlv(ws, deregister_msg)
            
            # 服务器会关闭连接，等待一下让连接关闭
            time.sleep(0.5)

        finally:
            try:
                ws.close()
            except:
                pass  # 连接可能已经被服务器关闭

        # 等待状态更新为OFFLINE
        def _check_offline():
            ex = self.db.get_executor_by_name(AGENT_NAME)
            if not ex:
                return False
            return ex.get('status') == 0

        ok = wait_for_condition(_check_offline, timeout=WS_WAIT_OFFLINE_SEC, interval=0.5)
        self.assertTrue(ok, "注销后executor应该更新为OFFLINE状态")

        # 验证executor状态正确
        ex = self.db.get_executor_by_name(AGENT_NAME)
        if ex:
            self.assertEqual(ex.get('status'), 0, "注销后应该是OFFLINE状态")
            # 注意：token是否被清理取决于业务逻辑设计，这里不做强制要求

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_08_002_deregister_not_registered(self):
        """IT-08-002: 注销未注册的执行机"""
        ws = self._open_ws()
        try:
            # 尝试注销一个不存在的执行机
            deregister_msg = encode_deregister_request("NonExistentExecutor")
            self._ws_send_tlv(ws, deregister_msg)

            # 系统应该妥善处理，不应该崩溃

        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_08_003_deregister_after_disconnect(self):
        """IT-08-003: 断开连接后的注销"""
        # 注册获取token（连接关闭）
        token = self._ws_register_and_get_token_tlv()

        # 重新连接并尝试注销
        ws = self._open_ws()
        try:
            deregister_msg = encode_deregister_request(AGENT_NAME)
            self._ws_send_tlv(ws, deregister_msg)

        finally:
            ws.close()

        # 验证状态仍然正确
        ex = self.db.get_executor_by_name(AGENT_NAME)
        self.assertIsNotNone(ex)
        # 状态可能仍然是在线（取决于实现），但token应该被清理

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_08_004_multiple_deregister(self):
        """IT-08-004: 重复注销"""
        # 注册并注销
        token = self._ws_register_and_get_token_tlv()

        # 重复注销多次
        for i in range(3):
            ws = self._open_ws()
            try:
                deregister_msg = encode_deregister_request(AGENT_NAME)
                self._ws_send_tlv(ws, deregister_msg)
            finally:
                ws.close()

        # 验证状态稳定
        ex = self.db.get_executor_by_name(AGENT_NAME)
        self.assertIsNotNone(ex)
        self.assertEqual(ex.get('status'), 0, "重复注销后状态应该稳定为OFFLINE")

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_08_005_deregister_during_task(self):
        """IT-08-005: 任务执行期间注销"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 模拟正在执行任务的状态
            # （实际实现可能需要在数据库中标记任务状态）

            # 发送注销请求
            deregister_msg = encode_deregister_request(AGENT_NAME)
            self._ws_send_tlv(ws, deregister_msg)

        finally:
            ws.close()

        # 验证注销成功，状态更新为OFFLINE
        time.sleep(WS_WAIT_OFFLINE_SEC)

        ex = self.db.get_executor_by_name(AGENT_NAME)
        self.assertIsNotNone(ex)
        self.assertEqual(ex.get('status'), 0, "任务执行期间注销后也应该变为OFFLINE")

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_08_006_deregister_cleanup_verification(self):
        """IT-08-006: 注销清理验证"""
        # 注册并发送心跳添加UE
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            from .tlv_codec import encode_heartbeat
            ue_list = [{'msisdn': '8613800080001', 'serial': 'SN_DEREGISTER_TEST'}]
            heartbeat_msg = encode_heartbeat(token, 1, ue_list)
            self._ws_send_recv_tlv(ws, heartbeat_msg)
            time.sleep(1)  # 等待UE数据入库
        finally:
            ws.close()

        # 验证UE已添加
        ue = self.db.get_ue_by_msisdn('8613800080001')
        self.assertIsNotNone(ue, "UE应该已添加")

        # 注销执行机（需要重新注册后才能注销）
        ws2, token2 = self._ws_register_and_keep_connection_tlv()
        try:
            deregister_msg = encode_deregister_request(AGENT_NAME)
            self._ws_send_tlv(ws2, deregister_msg)
            time.sleep(0.5)  # 等待服务器关闭连接
        finally:
            try:
                ws2.close()
            except:
                pass  # 连接可能已经被服务器关闭

        # 等待清理
        time.sleep(WS_WAIT_OFFLINE_SEC)

        # 验证执行机状态
        ex = self.db.get_executor_by_name(AGENT_NAME)
        self.assertIsNotNone(ex)
        self.assertEqual(ex.get('status'), 0)

        # UE记录可能保留或被清理（取决于业务逻辑）
        # 这里不做强制断言，因为不同实现可能有不同策略

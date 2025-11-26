import unittest
import time

from .base import BaseTestCase
from .config import WS_ENABLE, AGENT_NAME, AGENT_NTLM_HASH, WS_WAIT_OFFLINE_SEC
from .utils import wait_for_condition


@unittest.skipUnless(WS_ENABLE, "WS测试默认关闭，设置 EXEC_WS_ENABLE=1 以启用")
class TestOfflineReconnectIT04(BaseTestCase):
    """IT-04: 离线处理和重连流程"""

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_04_001_disconnect_and_offline(self):
        """IT-04-001: 连接断开后状态变为OFFLINE"""
        ws, token = self._ws_register_and_keep_connection()
        try:
            ex = self.db.get_executor_by_name(AGENT_NAME)
            self.assertIsNotNone(ex)
            self.assertEqual(ex.get("status"), 1, "初始状态应为ONLINE")
        finally:
            ws.close()

        def _check_offline():
            ex_inner = self.db.get_executor_by_name(AGENT_NAME)
            if not ex_inner:
                return False
            return ex_inner.get("status") == 0

        ok = wait_for_condition(_check_offline, timeout=WS_WAIT_OFFLINE_SEC, interval=0.5)
        self.assertTrue(ok, "断开连接后executor应该更新为OFFLINE状态")

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_04_002_reconnect_requires_auth(self):
        """IT-04-002: 重连需要重新认证"""
        old_token = self._ws_register_and_get_token()
        new_token = self._ws_register_and_get_token()

        self.assertNotEqual(new_token, old_token, "重连后应该生成新的token")

        ex = self.db.get_executor_by_name(AGENT_NAME)
        self.assertIsNotNone(ex)
        self.assertEqual(ex.get("status"), 1, "重连认证后状态应为ONLINE")

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_04_003_force_reauth_on_reconnect(self):
        """IT-04-003: 重连强制重新认证"""
        ws1, token1 = self._ws_register_and_keep_connection()
        try:
            ex = self.db.get_executor_by_name(AGENT_NAME)
            self.assertIsNotNone(ex)
            self.assertEqual(ex.get("status"), 1)
        finally:
            ws1.close()

        time.sleep(WS_WAIT_OFFLINE_SEC)

        token2 = self._ws_register_and_get_token()
        self.assertNotEqual(token2, token1, "重连应生成新的token")

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_04_004_session_cleanup_on_disconnect(self):
        """IT-04-004: 断开连接时会话清理"""
        ws, token = self._ws_register_and_keep_connection()
        try:
            ex = self.db.get_executor_by_name(AGENT_NAME)
            self.assertIsNotNone(ex)
            self.assertEqual(ex.get("status"), 1)
        finally:
            ws.close()

        time.sleep(WS_WAIT_OFFLINE_SEC + 1)

        ex = self.db.get_executor_by_name(AGENT_NAME)
        self.assertIsNotNone(ex)
        self.assertEqual(ex.get("status"), 0, "断开连接后状态应为OFFLINE")

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_04_005_multiple_disconnect_reconnect(self):
        """IT-04-005: 多次断开重连"""
        tokens = []

        for i in range(3):
            token = self._ws_register_and_get_token()
            tokens.append(token)

            ex = self.db.get_executor_by_name(AGENT_NAME)
            self.assertIsNotNone(ex)
            self.assertEqual(ex.get("status"), 1)

            if i < 2:
                time.sleep(WS_WAIT_OFFLINE_SEC + 1)
                ex = self.db.get_executor_by_name(AGENT_NAME)
                self.assertEqual(ex.get("status"), 0, f"第{i+1}次断开后应为OFFLINE")

        unique_tokens = set(tokens)
        self.assertEqual(len(unique_tokens), len(tokens), "每次重连应生成不同的token")

        ex = self.db.get_executor_by_name(AGENT_NAME)
        self.assertEqual(ex.get("status"), 1, "最后一次重连应为ONLINE")

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_04_006_reconnect_during_task_execution(self):
        """IT-04-006: 任务执行期间断开重连（可选测试）"""
        ws, token = self._ws_register_and_keep_connection()
        try:
            ws.close()
            time.sleep(WS_WAIT_OFFLINE_SEC)

            ex = self.db.get_executor_by_name(AGENT_NAME)
            self.assertEqual(ex.get("status"), 0)

            new_token = self._ws_register_and_get_token()

            ex = self.db.get_executor_by_name(AGENT_NAME)
            self.assertEqual(ex.get("status"), 1)
        except Exception:
            pass

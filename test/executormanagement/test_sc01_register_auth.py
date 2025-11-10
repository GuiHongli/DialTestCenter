import unittest
import time

from .base import BaseTestCase
from .config import WS_ENABLE, AGENT_NTLM_HASH, AGENT_NAME, AGENT_USERNAME
from .utils import compute_chap_response, wait_for_condition


@unittest.skipUnless(WS_ENABLE, "WS测试默认关闭，设置 EXEC_WS_ENABLE=1 以启用")
class TestRegisterAuth(BaseTestCase):
    """SC-01: WSS 注册与认证（CHAP 四阶段）"""

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，跳过成功注册用例")
    def test_tc_01_001_register_success(self):
        """TC-01-001: 成功注册并获取token"""
        token = self._ws_register_and_get_token()
        self.assertTrue(token, "Token 应该非空")
        
        # 验证数据库状态：executor.status=1（ONLINE），token已更新
        def _check_online():
            ex = self.db.get_executor_by_name(AGENT_NAME)
            if not ex:
                return False
            return (ex.get('status') == 1 and 
                    ex.get('token') is not None and
                    ex.get('last_online_time') is not None)
        
        ok = wait_for_condition(_check_online, timeout=5, interval=0.5)
        self.assertTrue(ok, "executor 应该更新为 ONLINE 状态，并设置 token 和 last_online_time")
        
        # 验证 Challenge 长度（Base64编码后应为24字符，对应16字节）
        # 这个验证已在 base.py 的 _ws_register_and_get_token 中隐式完成

    def test_tc_01_002_register_user_not_found(self):
        """TC-01-002: 用户名不存在"""
        ws = self._open_ws()
        try:
            req = {
                "message_type": "register_request",
                "data": {"name": AGENT_NAME, "username": "__not_exists__", "ne_name": "NE_AUTO"},
            }
            res = self._ws_send_recv(ws, req)
            # 服务端仍应返回 Challenge（避免用户名枚举）
            self.assertEqual(res.get("message_type"), "register_challenge", 
                           "即使用户名不存在，也应返回 challenge")
            challenge = res.get("data", {}).get("challenge")
            self.assertIsNotNone(challenge, "Challenge 不应为空")
            
            # 发送任意 response
            auth = {"message_type": "register_auth", "data": {"response": "00"*16}}
            res2 = self._ws_send_recv(ws, auth)
            self.assertEqual(res2.get("message_type"), "register_ack")
            data = res2.get("data", {})
            self.assertEqual(data.get("status"), "failed", "认证应该失败")
            self.assertNotIn("token", data, "失败时不应有 token 字段")
            self.assertIn("not found", data.get("error_message", "").lower(), 
                         "错误消息应包含 'not found'")
            
            # 验证数据库未更新为 ONLINE
            ex = self.db.get_executor_by_name(AGENT_NAME)
            if ex:
                self.assertNotEqual(ex.get('status'), 1, 
                                  "认证失败时不应更新为 ONLINE 状态")
        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，跳过")
    def test_tc_01_003_register_auth_response_mismatch(self):
        """TC-01-003: 响应摘要不匹配"""
        ws = self._open_ws()
        try:
            req = {
                "message_type": "register_request",
                "data": {"name": AGENT_NAME, "username": AGENT_USERNAME, "ne_name": "NE_AUTO"},
            }
            res = self._ws_send_recv(ws, req)
            self.assertEqual(res.get("message_type"), "register_challenge")
            challenge = res.get("data", {}).get("challenge")
            self.assertIsNotNone(challenge)
            
            # 发送错误的 response（不使用正确的 CHAP 计算）
            wrong_response = "0" * 32  # 错误的摘要
            auth = {"message_type": "register_auth", "data": {"response": wrong_response}}
            res2 = self._ws_send_recv(ws, auth)
            
            self.assertEqual(res2.get("message_type"), "register_ack")
            data = res2.get("data", {})
            self.assertEqual(data.get("status"), "failed", "认证应该失败")
            self.assertNotIn("token", data, "失败时不应有 token 字段")
            self.assertIn("invalid", data.get("error_message", "").lower(), 
                         "错误消息应包含 'invalid'")
            
            # 验证不绑定会话
            ex = self.db.get_executor_by_name(AGENT_NAME)
            if ex:
                self.assertNotEqual(ex.get('status'), 1, 
                                  "认证失败时不应更新为 ONLINE 状态")
        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，跳过")
    def test_tc_01_004_deregister(self):
        """TC-01-004: 注销（deregister）"""
        # 先注册
        token = self._ws_register_and_get_token()
        self.assertTrue(token)
        
        # 等待状态更新为 ONLINE
        time.sleep(1)
        
        # 发送注销请求
        ws = self._open_ws()
        try:
            dereg = {
                "message_type": "deregister",
                "data": {"name": AGENT_NAME}
            }
            ws.send(self._json_dumps(dereg))
            
            # 可能有回执，尝试接收（超时不报错）
            try:
                resp = ws.recv()
            except:
                pass
        finally:
            ws.close()
        
        # 验证数据库状态：executor.status=0（OFFLINE）
        def _check_offline():
            ex = self.db.get_executor_by_name(AGENT_NAME)
            if not ex:
                return False
            return ex.get('status') == 0
        
        ok = wait_for_condition(_check_offline, timeout=5, interval=0.5)
        self.assertTrue(ok, "注销后 executor 应该更新为 OFFLINE 状态")
        
        # 会话解除绑定后，后续心跳需要重新注册
        # （这个可以在 SC-02 中进一步测试）



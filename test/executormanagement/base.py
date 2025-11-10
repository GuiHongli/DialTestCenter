import unittest
import logging
import json

from .utils import APIClient, Assertions, DatabaseHelper, compute_chap_response, wait_for_condition
from .config import API_ENDPOINTS, WS_URL, WS_ENABLE, AGENT_NAME, AGENT_USERNAME, AGENT_NTLM_HASH, WS_TIMEOUT


class BaseTestCase(unittest.TestCase):
    """执行机管理 - 集成测试基类"""

    @classmethod
    def setUpClass(cls) -> None:
        logging.getLogger(__name__).info("== Exec tests start: %s ==", cls.__name__)
        cls.api = APIClient()
        cls.assertions = Assertions()
        cls.db = DatabaseHelper()
        # 确保测试所需表结构存在
        try:
            cls.db.ensure_schema()
            # 准备刷新接口所需的基础数据
            cls.db.ensure_executor_exists("Executor_PC_001")
        except Exception:
            # 不中断收集；具体用例执行时再报错便于定位
            pass

        # 预热一次接口，避免首个请求包含数据源初始化等冷启动开销影响性能断言
        try:
            cls.api.get(API_ENDPOINTS['LIST_EXECUTORS'])
        except Exception:
            # 预热失败不影响后续用例执行，由具体用例来报告错误
            pass

    @classmethod
    def tearDownClass(cls) -> None:
        if getattr(cls, 'db', None):
            try:
                cls.db.close()
            except Exception:
                pass
        logging.getLogger(__name__).info("== Exec tests finished: %s ==", cls.__name__)

    def setUp(self) -> None:
        # 可按需清理或准备数据
        pass

    # ---------- WS helpers ----------
    def _open_ws(self):
        import websocket  # type: ignore
        import ssl
        # 禁用 SSL 证书验证（用于测试环境的自签名证书）
        sslopt = {"cert_reqs": ssl.CERT_NONE}
        ws = websocket.create_connection(WS_URL, timeout=WS_TIMEOUT, sslopt=sslopt)
        return ws

    def _json_dumps(self, payload: dict):
        return json.dumps(payload)

    def _ws_send_recv(self, ws, payload: dict):
        ws.send(json.dumps(payload))
        text = ws.recv()
        return json.loads(text)

    def _ws_register_and_get_token(self) -> str:
        """注册并返回 token（连接会关闭）"""
        if not WS_ENABLE:
            self.skipTest('WS 未启用')
        if not AGENT_NTLM_HASH:
            self.skipTest('未提供 EXEC_AGENT_NTLM_HASH，无法计算CHAP摘要')
        ws = self._open_ws()
        try:
            req = {
                "message_type": "register_request",
                "data": {"name": AGENT_NAME, "username": AGENT_USERNAME, "ne_name": "NE_AUTO"},
            }
            res = self._ws_send_recv(ws, req)
            if res.get("message_type") != "register_challenge":
                self.fail(f"unexpected first response: {res}")
            challenge = res.get("data", {}).get("challenge")
            self.assertIsNotNone(challenge)
            response = compute_chap_response(AGENT_NTLM_HASH, challenge)
            auth = {"message_type": "register_auth", "data": {"response": response}}
            res2 = self._ws_send_recv(ws, auth)
            self.assertEqual(res2.get("message_type"), "register_ack")
            data = res2.get("data", {})
            self.assertEqual(data.get("status"), "success")
            token = data.get("token")
            self.assertTrue(token)
            return token
        finally:
            ws.close()

    def _ws_register_and_keep_connection(self):
        """注册并返回保持打开的 WebSocket 连接和 token（调用者负责关闭）"""
        if not WS_ENABLE:
            self.skipTest('WS 未启用')
        if not AGENT_NTLM_HASH:
            self.skipTest('未提供 EXEC_AGENT_NTLM_HASH，无法计算CHAP摘要')
        ws = self._open_ws()
        req = {
            "message_type": "register_request",
            "data": {"name": AGENT_NAME, "username": AGENT_USERNAME, "ne_name": "NE_AUTO"},
        }
        res = self._ws_send_recv(ws, req)
        if res.get("message_type") != "register_challenge":
            self.fail(f"unexpected first response: {res}")
        challenge = res.get("data", {}).get("challenge")
        self.assertIsNotNone(challenge)
        response = compute_chap_response(AGENT_NTLM_HASH, challenge)
        auth = {"message_type": "register_auth", "data": {"response": response}}
        res2 = self._ws_send_recv(ws, auth)
        self.assertEqual(res2.get("message_type"), "register_ack")
        data = res2.get("data", {})
        self.assertEqual(data.get("status"), "success")
        token = data.get("token")
        self.assertTrue(token)
        return ws, token



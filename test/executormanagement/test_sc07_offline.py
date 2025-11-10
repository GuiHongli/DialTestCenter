import unittest

from .base import BaseTestCase
from .config import WS_ENABLE, WS_TIMEOUT, WS_WAIT_OFFLINE_SEC
from .config import AGENT_NTLM_HASH
from .utils import wait_for_condition


@unittest.skipUnless(WS_ENABLE, "WS测试默认关闭，设置 EXEC_WS_ENABLE=1 以启用")
class TestOfflineHandling(BaseTestCase):
    """SC-07: 离线处理（连接断开）"""

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，跳过")
    def test_tc_07_001_on_close_sets_offline(self):
        # 注册确保建立绑定
        token = self._ws_register_and_get_token()
        self.assertTrue(token)

        # 重新建立连接后立即关闭，触发 onClose
        ws = self._open_ws()
        ws.close()

        # 等待 DB executor.status=OFFLINE
        def _offline():
            ex = self.db.get_executor_by_name("Executor_PC_001")
            if not ex:
                return False
            return ex.get('status') == 0

        ok = wait_for_condition(_offline, timeout=WS_WAIT_OFFLINE_SEC, interval=0.5)
        self.assertTrue(ok, "executor 未更新为 OFFLINE")



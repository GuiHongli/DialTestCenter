import unittest

from .base import BaseTestCase
from .config import WS_ENABLE


@unittest.skipUnless(WS_ENABLE, "WS测试默认关闭，设置 EXEC_WS_ENABLE=1 以启用")
class TestSecurity(BaseTestCase):
    """SC-10: 安全性（占位）"""

    @unittest.skip('抓包/协议校验需联调：NTLM Hash 不经网络传输')
    def test_tc_10_001_ntlm_hash_never_transmitted(self):
        self.assertTrue(True)

    @unittest.skip('无效/过期 Token 拦截需后端实现消息校验逻辑')
    def test_tc_10_002_invalid_or_expired_token_rejected(self):
        self.assertTrue(True)



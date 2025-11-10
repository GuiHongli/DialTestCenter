import unittest

from .base import BaseTestCase
from .config import WS_ENABLE


@unittest.skipUnless(WS_ENABLE, "WS测试默认关闭，设置 EXEC_WS_ENABLE=1 以启用")
class TestUeScreencap(BaseTestCase):
    """SC-06: UE 截屏请求与回执（占位用例）"""

    @unittest.skip("等待后端实现 query_ue_screencap/ue_screencap_response")
    def test_tc_06_001_query_screencap_then_response(self):
        self.assertTrue(True)



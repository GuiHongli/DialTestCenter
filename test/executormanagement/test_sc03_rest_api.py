from .base import BaseTestCase
from .config import API_ENDPOINTS, PERFORMANCE_BASELINE
from .utils import measure_response_time


class TestExecutorRestApi(BaseTestCase):
    """SC-03: 北向API - 执行机列表与刷新"""

    def test_tc_03_001_list_executors_200(self):
        response, cost = measure_response_time(self.api.get, API_ENDPOINTS['LIST_EXECUTORS'])
        self.assertions.assert_status_code(response, 200)
        self.assertions.assert_response_time(cost, PERFORMANCE_BASELINE['list_executors'])

    def test_tc_03_002_refresh_executor_accept(self):
        body = {"name": "Executor_PC_001"}
        response, cost = measure_response_time(self.api.post, API_ENDPOINTS['REFRESH_EXECUTOR'], body)
        # 后端实现：在线返回200/202，离线返回404；均视为接口语义正确
        assert response.status_code in (200, 202, 404), f"unexpected status {response.status_code}"
        # 性能阈值按照202路径设定
        self.assertions.assert_response_time(cost, PERFORMANCE_BASELINE['refresh_executor'])



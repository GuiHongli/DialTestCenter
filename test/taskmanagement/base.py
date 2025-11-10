# -*- coding: utf-8 -*-
"""
集成测试基类

提供测试前后的准备和清理工作
"""

import unittest
import logging
from .utils import APIClient, DatabaseHelper, Assertions, TestDataBuilder, logger
from .config import API_ENDPOINTS, BASE_URL


class BaseTestCase(unittest.TestCase):
    """集成测试基类"""

    @classmethod
    def setUpClass(cls):
        """测试类级别的初始化"""
        logger.info("=" * 60)
        logger.info(f"Starting test class: {cls.__name__}")
        logger.info("=" * 60)
        
        cls.api = APIClient(base_url=BASE_URL)
        cls.assertions = Assertions()
        cls.data_builder = TestDataBuilder()
        cls.db = DatabaseHelper()

    @classmethod
    def tearDownClass(cls):
        """测试类级别的清理"""
        logger.info("=" * 60)
        logger.info(f"Finished test class: {cls.__name__}")
        logger.info("=" * 60)
        
        if cls.db and cls.db.conn:
            cls.db.close()

    def setUp(self):
        """测试用例级别的初始化"""
        logger.info(f"\n>>> Running: {self._testMethodName}")

    def tearDown(self):
        """测试用例级别的清理"""
        logger.info(f"<<< Finished: {self._testMethodName}\n")

    # ==================== 便利方法 ====================
    def start_task(self, scenario='VALIDATION', failed_apps=None):
        """启动任务的便利方法"""
        data = self.data_builder.build_start_task_request(
            scenario=scenario,
            failed_apps=failed_apps
        )
        url = f"{BASE_URL}{API_ENDPOINTS['START_TASK']}"
        response = self.api.post(API_ENDPOINTS['START_TASK'], data)
        return response

    def get_tasks(self, page=0, size=20):
        """查询任务列表的便利方法"""
        response = self.api.get(
            API_ENDPOINTS['GET_TASKS'],
            params={'page': page, 'size': size}
        )
        return response

    def get_task_by_id(self, task_id):
        """查询任务详情的便利方法"""
        url = API_ENDPOINTS['GET_TASK_BY_ID'].format(task_id=task_id)
        response = self.api.get(url)
        return response

    def create_template(self, name, cron='0 0 1 ? * 1', enabled=True):
        """创建模板的便利方法"""
        data = self.data_builder.build_create_template_request(
            name=name,
            cron=cron,
            enabled=enabled
        )
        response = self.api.post(API_ENDPOINTS['CREATE_TEMPLATE'], data)
        return response

    def get_templates(self):
        """查询模板列表的便利方法"""
        response = self.api.get(API_ENDPOINTS['GET_TEMPLATES'])
        return response

    def get_template_by_id(self, template_id):
        """查询模板详情的便利方法"""
        url = API_ENDPOINTS['GET_TEMPLATE_BY_ID'].format(template_id=template_id)
        response = self.api.get(url)
        return response

    def update_template(self, template_id, **kwargs):
        """更新模板的便利方法"""
        url = API_ENDPOINTS['UPDATE_TEMPLATE'].format(template_id=template_id)
        response = self.api.put(url, kwargs)
        return response

    def delete_template(self, template_id):
        """删除模板的便利方法"""
        url = API_ENDPOINTS['DELETE_TEMPLATE'].format(template_id=template_id)
        response = self.api.delete(url)
        return response

    def notify_callback(self, task_id, status='SUCCESS', result_data=None):
        """异步回调的便利方法"""
        data = self.data_builder.build_callback_request(
            task_id=task_id,
            status=status,
            result_data=result_data
        )
        response = self.api.post(API_ENDPOINTS['NOTIFY_CALLBACK'], data)
        return response

    # ==================== 数据库便利方法 ====================
    def get_task_from_db(self, task_id):
        """从数据库查询任务"""
        return self.db.get_task_by_id(task_id)

    def get_latest_task_from_db(self):
        """获取数据库中最新的任务"""
        return self.db.get_latest_task()

    def truncate_task_table(self):
        """清空task表"""
        self.db.truncate_table('task')

    def truncate_template_table(self):
        """清空template_task表"""
        self.db.truncate_table('template_task')

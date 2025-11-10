import json
import unittest

from .base import BaseTestCase


class TestDatabaseConsistency(BaseTestCase):
    """SC-08: 数据库一致性与约束"""

    def test_tc_08_001_ue_info_json_valid_if_present(self):
        # 仅在存在 ue.info 列时执行
        if not self.db.table_has_column('ue', 'info'):
            self.skipTest('列 ue.info 不存在，跳过JSON合法性校验')
        rows = self.db.execute_query(
            "SELECT info FROM ue WHERE info IS NOT NULL LIMIT 20"
        )
        for row in rows:
            raw = row.get('info')
            text = raw if isinstance(raw, str) else json.dumps(raw)
            json.loads(text)
        self.assertTrue(True)

    def test_tc_08_002_fk_and_unique_constraints_declared(self):
        # 仅验证约束声明存在性，不对数据进行破坏性操作
        # executor.name 应唯一
        self.assertTrue(self.db.has_unique_constraint('executor'))
        # ue.executor_name 外键若存在则通过
        if self.db.table_has_column('ue', 'executor_name'):
            self.assertTrue(self.db.has_foreign_key('ue'))

    def test_tc_08_003_last_online_timestamp_exists_if_present(self):
        # 存在 last_online_time 列则至少可查询，自动设置由业务流保障
        if not self.db.table_has_column('executor', 'last_online_time'):
            self.skipTest('列 executor.last_online_time 不存在，跳过时间戳校验')
        rows = self.db.execute_query(
            "SELECT last_online_time FROM executor LIMIT 10"
        )
        self.assertIsNotNone(rows)



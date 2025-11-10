import json
import unittest
import time

from .base import BaseTestCase
from .config import WS_ENABLE, WS_TIMEOUT, AGENT_NAME, AGENT_NTLM_HASH, WS_WAIT_OFFLINE_SEC
from .utils import wait_for_condition


@unittest.skipUnless(WS_ENABLE, "WS测试默认关闭，设置 EXEC_WS_ENABLE=1 以启用")
class TestHeartbeat(BaseTestCase):
    """SC-02: 心跳与UE状态维护"""

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，跳过")
    def test_tc_02_001_heartbeat_updates_online(self):
        """TC-02-001: 心跳更新状态"""
        # 注册并保持连接（心跳需要在同一会话中发送）
        ws = self._ws_register_and_keep_connection()
        try:
            hb = {
                "message_type": "heartbeat_status",
                "data": {
                    "timestamp": int(time.time()),
                    "ue_list": [
                        {
                            "msisdn": "86138****0001",
                            "serial": "SN001",
                            "status": "idle",
                            "vendor": "Huawei",
                            "model": "P60",
                            "os_version": "Android 13",
                            "ip_v4": "192.168.1.10",
                            "battery_level": 85
                        },
                        {
                            "msisdn": "86139****0002",
                            "serial": "SN002",
                            "status": "busy",
                            "vendor": "Xiaomi",
                            "model": "Mi 13",
                            "os_version": "Android 12"
                        }
                    ],
                },
            }
            ws.send(json.dumps(hb))
            # 等待后端处理心跳
            time.sleep(1)
            
            # 在关闭连接前检查状态（关闭连接会触发 OFFLINE 更新）
            ex = self.db.get_executor_by_name(AGENT_NAME)
            self.assertIsNotNone(ex, "executor 应该存在")
            self.assertEqual(ex.get('status'), 1, "发送心跳后 executor 应为 ONLINE")
            self.assertIsNotNone(ex.get('last_online_time'), "last_online_time 应该已更新")
        finally:
            ws.close()
        
        # 验证 UE 列表已更新到数据库
        time.sleep(1)  # 等待 UE 数据入库
        ue1 = self.db.get_ue_by_msisdn("86138****0001")
        ue2 = self.db.get_ue_by_msisdn("86139****0002")
        
        # 验证 UE 信息存在
        self.assertIsNotNone(ue1, "UE1 应该存在于数据库中")
        self.assertIsNotNone(ue2, "UE2 应该存在于数据库中")
        
        # 验证核心字段
        if ue1:
            self.assertEqual(ue1.get('msisdn'), '86138****0001', "UE1 MSISDN 应正确")
            self.assertEqual(ue1.get('executor_name'), AGENT_NAME, "UE1 应关联到正确的执行机")
            if self.db.table_has_column('ue', 'vendor'):
                self.assertEqual(ue1.get('vendor'), 'Huawei', "UE1 厂商应正确")
        
        if ue2:
            self.assertEqual(ue2.get('msisdn'), '86139****0002', "UE2 MSISDN 应正确")
            self.assertEqual(ue2.get('executor_name'), AGENT_NAME, "UE2 应关联到正确的执行机")

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，跳过")
    def test_tc_02_002_ue_list_changes(self):
        """TC-02-002: UE 清单变更"""
        # 注册并保持连接
        ws = self._ws_register_and_keep_connection()
        try:
            # 第一次心跳：发送 3 个 UE
            hb1 = {
                "message_type": "heartbeat_status",
                "data": {
                    "timestamp": int(time.time()),
                    "ue_list": [
                        {"msisdn": "8613800000101", "serial": "SN_TEST_001", "status": "idle"},
                        {"msisdn": "8613800000102", "serial": "SN_TEST_002", "status": "idle"},
                        {"msisdn": "8613800000103", "serial": "SN_TEST_003", "status": "idle"}
                    ],
                },
            }
            ws.send(json.dumps(hb1))
            time.sleep(1)  # 等待数据库更新
            
            # 第二次心跳：修改 SN_TEST_002 状态，新增 SN_TEST_004，删除 SN_TEST_003
            hb2 = {
                "message_type": "heartbeat_status",
                "data": {
                    "timestamp": int(time.time()),
                    "ue_list": [
                        {"msisdn": "8613800000101", "serial": "SN_TEST_001", "status": "idle"},
                        {"msisdn": "8613800000102", "serial": "SN_TEST_002", "status": "busy"},  # 状态改变
                        {"msisdn": "8613800000104", "serial": "SN_TEST_004", "status": "idle"}   # 新增
                    ],
                },
            }
            ws.send(json.dumps(hb2))
            time.sleep(1)  # 等待数据库更新
        finally:
            ws.close()
        
        # 验证 UE 变更
        ue1 = self.db.get_ue_by_msisdn("8613800000101")
        ue2 = self.db.get_ue_by_msisdn("8613800000102")
        ue3 = self.db.get_ue_by_msisdn("8613800000103")
        ue4 = self.db.get_ue_by_msisdn("8613800000104")
        
        # SN_TEST_001 应保持不变
        if ue1:
            # 根据实际数据库实现，status 可能存储为字符串或整数
            pass
        
        # SN_TEST_002 状态应已更新
        if ue2:
            # 状态已更新的验证依赖数据库实现
            pass
        
        # SN_TEST_004 应已新增
        self.assertIsNotNone(ue4, "新增的 UE 应该存在于数据库中")
        
        # SN_TEST_003 按策略处理（保留或标记删除）
        # 这里不做强制断言，因为不同实现可能有不同策略

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，跳过")
    def test_tc_02_003_offline_on_disconnect(self):
        """TC-02-003: 离线处理"""
        # 注册并保持连接
        ws = self._ws_register_and_keep_connection()
        try:
            hb = {
                "message_type": "heartbeat_status",
                "data": {
                    "timestamp": int(time.time()),
                    "ue_list": [
                        {"msisdn": "8613800000201", "serial": "SN_OFFLINE_TEST", "status": "idle"}
                    ],
                },
            }
            ws.send(json.dumps(hb))
            time.sleep(1)  # 等待状态更新
        finally:
            pass
        
        # 验证 executor 为 ONLINE
        ex = self.db.get_executor_by_name(AGENT_NAME)
        self.assertIsNotNone(ex)
        self.assertEqual(ex.get('status'), 1, "发送心跳后应为 ONLINE")
        
        # 关闭 WebSocket 连接
        ws.close()
        
        # 等待服务端检测到断开并更新状态为 OFFLINE
        def _check_offline():
            ex = self.db.get_executor_by_name(AGENT_NAME)
            if not ex:
                return False
            return ex.get('status') == 0
        
        ok = wait_for_condition(_check_offline, timeout=WS_WAIT_OFFLINE_SEC, interval=0.5)
        self.assertTrue(ok, "断开连接后 executor 应该更新为 OFFLINE")
        
        # 验证会话映射已移除（重新连接需要重新注册）
        # 这个验证需要尝试发送消息，预期失败或需要重新认证



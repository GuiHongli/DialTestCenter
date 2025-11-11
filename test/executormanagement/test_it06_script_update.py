import unittest
import hashlib

from .base import BaseTestCase
from .config import WS_ENABLE, AGENT_NTLM_HASH
from .tlv_codec import (MessageType, encode_script_update_notify, encode_script_update_ack)


@unittest.skipUnless(WS_ENABLE, "WS测试默认关闭，设置 EXEC_WS_ENABLE=1 以启用")
class TestScriptUpdateIT06(BaseTestCase):
    """IT-06: 脚本更新流程"""

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_06_001_script_update_success(self):
        """IT-06-001: 脚本更新确认（Agent发送ScriptUpdate-Ack）"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 注意：ScriptUpdate-Notify是CloudUDN主动发送给Agent的，这里只测试Agent的确认
            # 模拟Agent确认更新成功
            script_name = "dial_test.py"
            version = "1.2.0"
            ack_msg = encode_script_update_ack(script_name, version, 0)  # 0=成功
            self._ws_send_tlv(ws, ack_msg)
            
            # 验证消息发送成功（没有异常）
            import time
            time.sleep(0.2)

        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_06_002_script_update_crc_mismatch(self):
        """IT-06-002: 脚本更新CRC校验失败"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 准备脚本数据但使用错误的CRC
            script_name = "test_script.py"
            version = "1.0.1"
            script_bytes = b"print('test script')"
            wrong_crc = "invalid_crc_" + "0" * 26  # 错误的CRC

            # 发送脚本更新通知
            update_msg = encode_script_update_notify(script_name, version, script_bytes, wrong_crc)
            self._ws_send_tlv(ws, update_msg)

            # 模拟Agent检测到CRC错误
            ack_msg = encode_script_update_ack(script_name, version, 1, "CRC verification failed")
            self._ws_send_tlv(ws, ack_msg)

        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_06_003_script_update_version_conflict(self):
        """IT-06-003: 脚本版本冲突"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 尝试更新到较低版本
            script_name = "existing_script.py"
            version = "0.9.0"  # 假设当前版本是1.0.0
            script_bytes = b"# older version script"
            crc = hashlib.md5(script_bytes).hexdigest()

            # 发送脚本更新通知
            update_msg = encode_script_update_notify(script_name, version, script_bytes, crc)
            self._ws_send_tlv(ws, update_msg)

            # 模拟Agent拒绝版本降级
            ack_msg = encode_script_update_ack(script_name, version, 1, "Version downgrade not allowed")
            self._ws_send_tlv(ws, ack_msg)

        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_06_004_script_update_large_file(self):
        """IT-06-004: 大文件脚本更新"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 准备大的脚本文件（模拟）
            script_name = "large_script.py"
            version = "2.0.0"
            # 创建一个较大的脚本内容
            script_lines = []
            for i in range(1000):
                script_lines.append(f"# Line {i}")
                script_lines.append(f"print('Processing item {i}')")
            script_content = "\n".join(script_lines)
            script_bytes = script_content.encode('utf-8')
            crc = hashlib.md5(script_bytes).hexdigest()

            # 发送大文件脚本更新
            update_msg = encode_script_update_notify(script_name, version, script_bytes, crc)
            self._ws_send_tlv(ws, update_msg)

            # 模拟处理大文件
            ack_msg = encode_script_update_ack(script_name, version, 0)
            self._ws_send_tlv(ws, ack_msg)

        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_06_005_script_update_multiple_scripts(self):
        """IT-06-005: 批量脚本更新"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 更新多个脚本
            scripts = [
                ("script1.py", "1.1.0", b"def script1(): pass"),
                ("script2.py", "2.0.0", b"def script2(): return True"),
                ("script3.py", "1.5.0", b"class Script3: pass")
            ]

            for script_name, version, script_bytes in scripts:
                crc = hashlib.md5(script_bytes).hexdigest()

                # 发送脚本更新
                update_msg = encode_script_update_notify(script_name, version, script_bytes, crc)
                self._ws_send_tlv(ws, update_msg)

                # 模拟确认
                ack_msg = encode_script_update_ack(script_name, version, 0)
                self._ws_send_tlv(ws, ack_msg)

        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_06_006_script_update_rollback(self):
        """IT-06-006: 脚本更新回滚"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 先更新到一个损坏的版本
            script_name = "rollback_test.py"
            version_bad = "1.0.0"
            bad_script = b"import nonexistent_module"  # 会导致导入错误
            crc_bad = hashlib.md5(bad_script).hexdigest()

            # 发送损坏脚本
            update_msg = encode_script_update_notify(script_name, version_bad, bad_script, crc_bad)
            self._ws_send_tlv(ws, update_msg)

            # 模拟Agent检测到脚本错误
            ack_msg = encode_script_update_ack(script_name, version_bad, 1, "Script validation failed")
            self._ws_send_tlv(ws, ack_msg)

            # 然后发送正确版本进行回滚
            version_good = "0.9.0"
            good_script = b"print('Working script')"
            crc_good = hashlib.md5(good_script).hexdigest()

            update_msg2 = encode_script_update_notify(script_name, version_good, good_script, crc_good)
            self._ws_send_tlv(ws, update_msg2)

            # 模拟成功回滚
            ack_msg2 = encode_script_update_ack(script_name, version_good, 0)
            self._ws_send_tlv(ws, ack_msg2)

        finally:
            ws.close()

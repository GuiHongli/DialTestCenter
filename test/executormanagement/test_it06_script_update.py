import unittest
import hashlib
import time

from .base import BaseTestCase
from .config import WS_ENABLE, AGENT_NTLM_HASH
from .json_message import JsonMessageHelper
from .binary_codec import BinaryCodec


@unittest.skipUnless(WS_ENABLE, "WS测试默认关闭，设置 EXEC_WS_ENABLE=1 以启用")
class TestScriptUpdateIT06(BaseTestCase):
    """IT-06: 脚本更新流程"""

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_06_001_script_update_success(self):
        """IT-06-001: 脚本更新确认（Agent发送ScriptUpdate-Ack）"""
        client, token = self._ws_register_and_keep_connection()
        helper = JsonMessageHelper()
        try:
            script_name = "dial_test.py"
            version = "1.2.0"
            ack_env = helper.build(
                "ScriptUpdateAck",
                {"scriptName": script_name, "version": version, "result": 0},
                token=int(token) if str(token).isdigit() else None,
            )
            client.send_json(ack_env)
            time.sleep(0.2)
        finally:
            client.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_06_002_script_update_crc_mismatch(self):
        """IT-06-002: 脚本更新CRC校验失败"""
        client, token = self._ws_register_and_keep_connection()
        helper = JsonMessageHelper()
        try:
            script_name = "test_script.py"
            version = "1.0.1"
            script_bytes = b"print('test script')"
            wrong_crc = "invalid_crc_" + "0" * 26
            notify_env = helper.build(
                "ScriptUpdateNotify",
                {
                    "scriptName": script_name,
                    "version": version,
                    "filelen": len(script_bytes),
                    "crc": wrong_crc,
                },
                token=int(token) if str(token).isdigit() else None,
            )
            client.send_json(notify_env)

            ack_env = helper.build(
                "ScriptUpdateAck",
                {
                    "scriptName": script_name,
                    "version": version,
                    "result": 1,
                    "errorMsg": "CRC verification failed",
                },
                token=int(token) if str(token).isdigit() else None,
            )
            client.send_json(ack_env)
        finally:
            client.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_06_003_script_update_version_conflict(self):
        """IT-06-003: 脚本版本冲突"""
        client, token = self._ws_register_and_keep_connection()
        helper = JsonMessageHelper()
        try:
            script_name = "existing_script.py"
            version = "0.9.0"
            script_bytes = b"# older version script"
            crc = BinaryCodec.crc32_hex(script_bytes)

            notify_env = helper.build(
                "ScriptUpdateNotify",
                {
                    "scriptName": script_name,
                    "version": version,
                    "filelen": len(script_bytes),
                    "crc": crc,
                },
                token=int(token) if str(token).isdigit() else None,
            )
            client.send_json(notify_env)

            ack_env = helper.build(
                "ScriptUpdateAck",
                {
                    "scriptName": script_name,
                    "version": version,
                    "result": 1,
                    "errorMsg": "Version downgrade not allowed",
                },
                token=int(token) if str(token).isdigit() else None,
            )
            client.send_json(ack_env)
        finally:
            client.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_06_004_script_update_large_file(self):
        """IT-06-004: 大文件脚本更新"""
        client, token = self._ws_register_and_keep_connection()
        helper = JsonMessageHelper()
        try:
            script_name = "large_script.py"
            version = "2.0.0"
            script_lines = []
            for i in range(1000):
                script_lines.append(f"# Line {i}")
                script_lines.append(f"print('Processing item {i}')")
            script_content = "\n".join(script_lines)
            script_bytes = script_content.encode("utf-8")
            crc = BinaryCodec.crc32_hex(script_bytes)

            notify_env = helper.build(
                "ScriptUpdateNotify",
                {
                    "scriptName": script_name,
                    "version": version,
                    "filelen": len(script_bytes),
                    "crc": crc,
                },
                token=int(token) if str(token).isdigit() else None,
            )
            client.send_json(notify_env)

            ack_env = helper.build(
                "ScriptUpdateAck",
                {"scriptName": script_name, "version": version, "result": 0},
                token=int(token) if str(token).isdigit() else None,
            )
            client.send_json(ack_env)
        finally:
            client.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_06_005_script_update_multiple_scripts(self):
        """IT-06-005: 批量脚本更新"""
        client, token = self._ws_register_and_keep_connection()
        helper = JsonMessageHelper()
        try:
            scripts = [
                ("script1.py", "1.1.0", b"def script1(): pass"),
                ("script2.py", "2.0.0", b"def script2(): return True"),
                ("script3.py", "1.5.0", b"class Script3: pass"),
            ]

            for script_name, version, script_bytes in scripts:
                crc = BinaryCodec.crc32_hex(script_bytes)
                notify_env = helper.build(
                    "ScriptUpdateNotify",
                    {
                        "scriptName": script_name,
                        "version": version,
                        "filelen": len(script_bytes),
                        "crc": crc,
                    },
                    token=int(token) if str(token).isdigit() else None,
                )
                client.send_json(notify_env)

                ack_env = helper.build(
                    "ScriptUpdateAck",
                    {"scriptName": script_name, "version": version, "result": 0},
                    token=int(token) if str(token).isdigit() else None,
                )
                client.send_json(ack_env)
        finally:
            client.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_06_006_script_update_rollback(self):
        """IT-06-006: 脚本更新回滚"""
        client, token = self._ws_register_and_keep_connection()
        helper = JsonMessageHelper()
        try:
            script_name = "rollback_test.py"
            version_bad = "1.0.0"
            bad_script = b"import nonexistent_module"
            crc_bad = BinaryCodec.crc32_hex(bad_script)

            bad_notify = helper.build(
                "ScriptUpdateNotify",
                {
                    "scriptName": script_name,
                    "version": version_bad,
                    "filelen": len(bad_script),
                    "crc": crc_bad,
                },
                token=int(token) if str(token).isdigit() else None,
            )
            client.send_json(bad_notify)

            bad_ack = helper.build(
                "ScriptUpdateAck",
                {
                    "scriptName": script_name,
                    "version": version_bad,
                    "result": 1,
                    "errorMsg": "Script validation failed",
                },
                token=int(token) if str(token).isdigit() else None,
            )
            client.send_json(bad_ack)

            version_good = "0.9.0"
            good_script = b"print('Working script')"
            crc_good = BinaryCodec.crc32_hex(good_script)

            good_notify = helper.build(
                "ScriptUpdateNotify",
                {
                    "scriptName": script_name,
                    "version": version_good,
                    "filelen": len(good_script),
                    "crc": crc_good,
                },
                token=int(token) if str(token).isdigit() else None,
            )
            client.send_json(good_notify)

            good_ack = helper.build(
                "ScriptUpdateAck",
                {"scriptName": script_name, "version": version_good, "result": 0},
                token=int(token) if str(token).isdigit() else None,
            )
            client.send_json(good_ack)
        finally:
            client.close()

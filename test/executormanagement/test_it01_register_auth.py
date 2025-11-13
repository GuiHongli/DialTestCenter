import unittest
import binascii

from .base import BaseTestCase
from .config import WS_ENABLE, AGENT_NTLM_HASH, AGENT_NAME, AGENT_USERNAME
from .utils import compute_chap_response, wait_for_condition
from .tlv_codec import MessageType, encode_register_request, encode_register_response


@unittest.skipUnless(WS_ENABLE, "WS测试默认关闭，设置 EXEC_WS_ENABLE=1 以启用")
class TestRegisterAuthIT01(BaseTestCase):
    """IT-01: Agent注册认证流程 - 四阶段CHAP认证"""

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算CHAP摘要")
    def test_it_01_001_register_success(self):
        """IT-01-001: 成功注册并获取token"""
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            self.assertTrue(token, "Token 应该非空")

            # 验证数据库状态：executor.status=1（ONLINE），token已更新
            def _check_online():
                ex = self.db.get_executor_by_name(AGENT_NAME)
                if not ex:
                    return False
                return (ex.get('status') == 1 and
                        ex.get('token') is not None and
                        ex.get('last_online_time') is not None)

            ok = wait_for_condition(_check_online, timeout=5, interval=0.5)
            self.assertTrue(ok, "executor 应该更新为 ONLINE 状态，并设置 token 和 last_online_time")

            # 验证token格式（8字节）
            self.assertEqual(len(token), 16, "Token 应该是16字符的十六进制字符串（8字节）")
            try:
                binascii.unhexlify(token)
            except:
                self.fail("Token 应该是有效的十六进制字符串")
        finally:
            ws.close()

    def test_it_01_002_register_user_not_found(self):
        """IT-01-002: 用户名不存在"""
        ws = self._open_ws()
        try:
            # 发送Register-Request消息（包含不存在的用户名）
            register_msg = encode_register_request(AGENT_NAME, "__not_exists__")
            msg_type, fields = self._ws_send_recv_tlv(ws, register_msg)

            # 验证收到Register-Challenge消息（即使用户名不存在，也应返回challenge）
            self.assertEqual(msg_type, MessageType.REGISTER_CHALLENGE,
                           "即使用户名不存在，也应返回 Register-Challenge 消息")
            challenge_hex = fields.get('challenge')
            self.assertIsNotNone(challenge_hex, "Challenge 不应为空")
            
            # 提取challenge_id
            challenge_id = fields.get('challenge_id', 0)

            # 发送任意 response
            fake_response = "0" * 32  # 错误的摘要
            response_msg = encode_register_response(challenge_id, "__not_exists__", fake_response)
            msg_type2, fields2 = self._ws_send_recv_tlv(ws, response_msg)

            # 验证收到Register-Result消息，结果为失败
            self.assertEqual(msg_type2, MessageType.REGISTER_RESULT)
            result = fields2.get('result', 0)
            self.assertNotEqual(result, 0, "认证应该失败")
            self.assertNotIn('token', fields2, "失败时不应有 token 字段")
            error_msg = fields2.get('error_message', '').lower()
            self.assertTrue('not found' in error_msg or 'invalid' in error_msg,
                          f"错误消息应包含失败原因，实际: {error_msg}")

            # 验证数据库未更新为 ONLINE
            ex = self.db.get_executor_by_name(AGENT_NAME)
            if ex:
                self.assertNotEqual(ex.get('status'), 1,
                                  "认证失败时不应更新为 ONLINE 状态")
        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_01_003_register_auth_response_mismatch(self):
        """IT-01-003: 响应摘要不匹配"""
        ws = self._open_ws()
        try:
            # 发送Register-Request消息
            register_msg = encode_register_request(AGENT_NAME, AGENT_USERNAME)
            msg_type, fields = self._ws_send_recv_tlv(ws, register_msg)

            # 验证收到Register-Challenge消息
            self.assertEqual(msg_type, MessageType.REGISTER_CHALLENGE)
            challenge_hex = fields.get('challenge')
            self.assertIsNotNone(challenge_hex)
            
            # 提取challenge_id
            challenge_id = fields.get('challenge_id', 0)

            # 发送错误的 response（不使用正确的 CHAP 计算）
            wrong_response = "0" * 32  # 错误的摘要
            response_msg = encode_register_response(challenge_id, AGENT_USERNAME, wrong_response)
            msg_type2, fields2 = self._ws_send_recv_tlv(ws, response_msg)

            # 验证收到Register-Result消息，结果为失败
            self.assertEqual(msg_type2, MessageType.REGISTER_RESULT)
            result = fields2.get('result', 0)
            self.assertNotEqual(result, 0, "认证应该失败")
            self.assertNotIn('token', fields2, "失败时不应有 token 字段")
            error_msg = fields2.get('error_message', '').lower()
            self.assertTrue('invalid' in error_msg or 'failed' in error_msg or 'incorrect' in error_msg,
                          f"错误消息应包含认证失败的提示，实际: {error_msg}")

            # 验证数据库未更新为 ONLINE
            ex = self.db.get_executor_by_name(AGENT_NAME)
            if ex:
                self.assertNotEqual(ex.get('status'), 1,
                                  "认证失败时不应更新为 ONLINE 状态")
        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_01_004_register_challenge_format(self):
        """IT-01-004: 验证Challenge格式（16字节随机数）"""
        ws = self._open_ws()
        try:
            # 发送Register-Request消息
            register_msg = encode_register_request(AGENT_NAME, AGENT_USERNAME)
            msg_type, fields = self._ws_send_recv_tlv(ws, register_msg)

            # 验证收到Register-Challenge消息
            self.assertEqual(msg_type, MessageType.REGISTER_CHALLENGE)
            challenge_data = fields.get('challenge')
            self.assertIsNotNone(challenge_data)

            # 验证Challenge格式：16字节的二进制数据
            if isinstance(challenge_data, bytes):
                self.assertEqual(len(challenge_data), 16, "Challenge 应该是16字节的二进制数据")
                challenge_hex = challenge_data.hex()
                self.assertEqual(len(challenge_hex), 32, "Challenge 转换为十六进制应该是32字符")
            elif isinstance(challenge_data, str):
                # 兼容性处理，如果是字符串则按十六进制验证
                self.assertEqual(len(challenge_data), 32, "Challenge 应该是32字符的十六进制字符串（16字节）")
                try:
                    challenge_bytes = bytes.fromhex(challenge_data)
                    self.assertEqual(len(challenge_bytes), 16, "Challenge 解码后应该是16字节")
                except ValueError:
                    self.fail("Challenge 应该是有效的十六进制字符串")
                challenge_hex = challenge_data
            else:
                self.fail(f"Unexpected challenge data type: {type(challenge_data)}")
        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_01_005_register_md5_calculation(self):
        """IT-01-005: 验证MD5摘要算法正确性"""
        ws = self._open_ws()
        try:
            # 发送Register-Request消息
            register_msg = encode_register_request(AGENT_NAME, AGENT_USERNAME)
            msg_type, fields = self._ws_send_recv_tlv(ws, register_msg)

            # 验证收到Register-Challenge消息
            self.assertEqual(msg_type, MessageType.REGISTER_CHALLENGE)
            challenge_data = fields.get('challenge')
            self.assertIsNotNone(challenge_data)

            # 使用正确的CHAP计算 - challenge_data可能是bytes或str
            if isinstance(challenge_data, bytes):
                # 服务器发送的是16字节二进制数据，直接使用
                challenge_hex = challenge_data.hex()
                challenge_bytes = challenge_data
            elif isinstance(challenge_data, str):
                # 兼容性处理，如果是字符串则按十六进制解析
                challenge_bytes = bytes.fromhex(challenge_data)
                challenge_hex = challenge_data
            else:
                self.fail(f"Unexpected challenge data type: {type(challenge_data)}")

            challenge_id_raw = fields.get('challenge_id', 0)
            if isinstance(challenge_id_raw, str):
                try:
                    challenge_id = int(challenge_id_raw.strip('\x00'))
                except ValueError:
                    challenge_id = 0
            elif isinstance(challenge_id_raw, int):
                challenge_id = challenge_id_raw
            elif isinstance(challenge_id_raw, bytes):
                try:
                    challenge_id = int.from_bytes(challenge_id_raw, byteorder='big')
                except ValueError:
                    challenge_id = 0
            else:
                challenge_id = 0

            correct_response = compute_chap_response(AGENT_NTLM_HASH, challenge_hex)
            response_msg = encode_register_response(challenge_id, AGENT_USERNAME, correct_response)
            msg_type2, fields2 = self._ws_send_recv_tlv(ws, response_msg)

            # 验证认证成功
            self.assertEqual(msg_type2, MessageType.REGISTER_RESULT)
            result = fields2.get('result', 1)
            self.assertEqual(result, 0, f"使用正确MD5摘要应该认证成功，错误: {fields2.get('error_message', '')}")
            token = fields2.get('token')
            self.assertTrue(token, "认证成功时应该返回token")
        finally:
            ws.close()

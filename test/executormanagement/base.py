import unittest
import logging
import json

from .utils import APIClient, Assertions, DatabaseHelper, compute_chap_response, wait_for_condition
from .config import API_ENDPOINTS, WS_URL, WS_ENABLE, AGENT_NAME, AGENT_USERNAME, AGENT_NTLM_HASH, WS_TIMEOUT
from .tlv_codec import TlvEncoder, TlvDecoder, MessageType, encode_register_request, encode_register_response, encode_heartbeat, encode_deregister_request, decode_message


class BaseTestCase(unittest.TestCase):
    """执行机管理 - 集成测试基类"""

    @staticmethod
    def _token_db_to_hex(db_token):
        """将数据库中的token（有符号long字符串）转换为16进制字符串
        
        数据库中存储的是Java long类型（有符号64位整数）的字符串表示，
        而客户端解码时转换为16进制字符串（8字节）
        """
        if db_token is None:
            return None
        
        # 转换为整数
        if isinstance(db_token, str):
            token_long = int(db_token)
        else:
            token_long = db_token
        
        # 如果是负数，转换为无符号表示（Java有符号long -> Python无符号）
        if token_long < 0:
            token_long = (1 << 64) + token_long
        
        # 转换为16字符的hex字符串（8字节，小写）
        return format(token_long, '016x')

    @classmethod
    def setUpClass(cls) -> None:
        logging.getLogger(__name__).info("== Exec tests start: %s ==", cls.__name__)
        cls.api = APIClient()
        cls.assertions = Assertions()
        cls.db = DatabaseHelper()
        # 确保测试所需表结构存在
        try:
            cls.db.ensure_schema()
            # 准备刷新接口所需的基础数据
            cls.db.ensure_executor_exists("Executor_PC_001")
            # 创建测试用户（用于CHAP认证）
            cls.db.ensure_agent_user_exists(AGENT_USERNAME, AGENT_NTLM_HASH)
        except Exception:
            # 不中断收集；具体用例执行时再报错便于定位
            pass

        # 预热一次接口，避免首个请求包含数据源初始化等冷启动开销影响性能断言
        try:
            cls.api.get(API_ENDPOINTS['LIST_EXECUTORS'])
        except Exception:
            # 预热失败不影响后续用例执行，由具体用例来报告错误
            pass

    @classmethod
    def tearDownClass(cls) -> None:
        if getattr(cls, 'db', None):
            try:
                cls.db.close()
            except Exception:
                pass
        logging.getLogger(__name__).info("== Exec tests finished: %s ==", cls.__name__)

    def setUp(self) -> None:
        # 可按需清理或准备数据
        pass

    # ---------- WS helpers ----------
    def _open_ws(self):
        import websocket  # type: ignore
        import ssl
        # 禁用 SSL 证书验证（用于测试环境的自签名证书）
        sslopt = {"cert_reqs": ssl.CERT_NONE}
        ws = websocket.create_connection(WS_URL, timeout=WS_TIMEOUT, sslopt=sslopt)
        return ws

    def _json_dumps(self, payload: dict):
        return json.dumps(payload)

    def _ws_send_recv(self, ws, payload: dict):
        ws.send(json.dumps(payload))
        text = ws.recv()
        return json.loads(text)

    # ---------- TLV WS helpers ----------

    def _ws_send_recv_tlv(self, ws, message_bytes: bytes):
        """发送TLV消息并接收响应"""
        import websocket
        print(f"DEBUG: Sending TLV message, length={len(message_bytes)}, hex={message_bytes.hex()}")
        ws.send(message_bytes, opcode=websocket.ABNF.OPCODE_BINARY)

        # 使用recv_data()而不是recv()来正确处理二进制消息
        try:
            # 尝试使用recv_data()获取帧信息
            frame = ws.recv_data()
            print(f"DEBUG: Received frame: opcode={frame[0]}, data_type={type(frame[1])}")

            if frame[0] == websocket.ABNF.OPCODE_BINARY:
                response_bytes = frame[1] if isinstance(frame[1], bytes) else frame[1].encode('latin1')
                print(f"DEBUG: Received binary frame: {response_bytes.hex()}")
            elif frame[0] == websocket.ABNF.OPCODE_TEXT:
                response_str = frame[1]
                print(f"DEBUG: Received text frame: {response_str}")
                # 如果收到文本消息，尝试按十六进制解析
                try:
                    response_bytes = bytes.fromhex(response_str)
                    print(f"DEBUG: Parsed text as hex: {response_str}")
                except ValueError:
                    raise ValueError(f"Expected binary message, got unexpected text: {response_str}")
            elif frame[0] == websocket.ABNF.OPCODE_CLOSE:
                print(f"DEBUG: Received CLOSE frame from server")
                raise ConnectionError("WebSocket connection closed by server")
            else:
                raise ValueError(f"Unexpected frame opcode: {frame[0]}")

        except AttributeError:
            # 如果没有recv_data()方法，回退到recv()
            print("DEBUG: recv_data() not available, using recv()")
            response = ws.recv()

            if isinstance(response, str):
                print(f"DEBUG: Received text message: {response}")
                try:
                    response_bytes = bytes.fromhex(response)
                    print(f"DEBUG: Parsed text as hex: {response}")
                except ValueError:
                    raise ValueError(f"Expected binary message, got unexpected text: {response}")
            elif isinstance(response, bytes):
                response_bytes = response
                print(f"DEBUG: Received binary message: {response_bytes.hex()}")
            else:
                raise ValueError(f"Unexpected response type: {type(response)}")

        try:
            message_type, fields = decode_message(response_bytes)
            # 添加详细调试信息
            print(f"DEBUG: Received message type: {message_type} (0x{message_type:02X})")
            print(f"DEBUG: Fields: {fields}")
            print(f"DEBUG: Response bytes length: {len(response_bytes)}")
            print(f"DEBUG: Response bytes (first 50): {response_bytes[:50].hex() if len(response_bytes) > 50 else response_bytes.hex()}")
            return message_type, fields
        except Exception as e:
            print(f"DEBUG: Failed to decode message: {e}")
            print(f"DEBUG: Raw response bytes length: {len(response_bytes)}")
            print(f"DEBUG: Raw response bytes: {response_bytes.hex()}")
            print(f"DEBUG: Response type: {type(response)}")
            raise

    def _ws_send_tlv(self, ws, message_bytes: bytes):
        """仅发送TLV消息，不等待响应"""
        import websocket
        ws.send(message_bytes, opcode=websocket.ABNF.OPCODE_BINARY)

    def _ws_register_and_get_token(self) -> str:
        """注册并返回 token（连接会关闭）"""
        if not WS_ENABLE:
            self.skipTest('WS 未启用')
        if not AGENT_NTLM_HASH:
            self.skipTest('未提供 EXEC_AGENT_NTLM_HASH，无法计算CHAP摘要')
        ws = self._open_ws()
        try:
            req = {
                "message_type": "register_request",
                "data": {"name": AGENT_NAME, "username": AGENT_USERNAME, "ne_name": "NE_AUTO"},
            }
            res = self._ws_send_recv(ws, req)
            if res.get("message_type") != "register_challenge":
                self.fail(f"unexpected first response: {res}")
            challenge = res.get("data", {}).get("challenge")
            self.assertIsNotNone(challenge)
            response = compute_chap_response(AGENT_NTLM_HASH, challenge)
            auth = {"message_type": "register_auth", "data": {"response": response}}
            res2 = self._ws_send_recv(ws, auth)
            self.assertEqual(res2.get("message_type"), "register_ack")
            data = res2.get("data", {})
            self.assertEqual(data.get("status"), "success")
            token = data.get("token")
            self.assertTrue(token)
            return token
        finally:
            ws.close()

    def _ws_register_and_keep_connection(self):
        """注册并返回保持打开的 WebSocket 连接和 token（调用者负责关闭）"""
        if not WS_ENABLE:
            self.skipTest('WS 未启用')
        if not AGENT_NTLM_HASH:
            self.skipTest('未提供 EXEC_AGENT_NTLM_HASH，无法计算CHAP摘要')
        ws = self._open_ws()
        req = {
            "message_type": "register_request",
            "data": {"name": AGENT_NAME, "username": AGENT_USERNAME, "ne_name": "NE_AUTO"},
        }
        res = self._ws_send_recv(ws, req)
        if res.get("message_type") != "register_challenge":
            self.fail(f"unexpected first response: {res}")
        challenge = res.get("data", {}).get("challenge")
        self.assertIsNotNone(challenge)
        response = compute_chap_response(AGENT_NTLM_HASH, challenge)
        auth = {"message_type": "register_auth", "data": {"response": response}}
        res2 = self._ws_send_recv(ws, auth)
        self.assertEqual(res2.get("message_type"), "register_ack")
        data = res2.get("data", {})
        self.assertEqual(data.get("status"), "success")
        token = data.get("token")
        self.assertTrue(token)
        return ws, token

    def _ws_register_and_get_token_tlv(self) -> str:
        """TLV版本：注册并返回 token（连接会关闭）"""
        if not WS_ENABLE:
            self.skipTest('WS 未启用')
        if not AGENT_NTLM_HASH:
            self.skipTest('未提供 EXEC_AGENT_NTLM_HASH，无法计算CHAP摘要')
        ws = self._open_ws()
        try:
            # 发送TLV格式的Register-Request消息
            register_msg = encode_register_request(AGENT_NAME, AGENT_USERNAME)
            msg_type, fields = self._ws_send_recv_tlv(ws, register_msg)

            # 验证收到Register-Challenge消息（0x02）
            self.assertEqual(msg_type, MessageType.REGISTER_CHALLENGE)
            challenge_data = fields.get('challenge')
            self.assertIsNotNone(challenge_data)

            # 计算MD5响应 - challenge_data可能是bytes或str
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

            response = compute_chap_response(AGENT_NTLM_HASH, challenge_hex)
            response_msg = encode_register_response(challenge_id, AGENT_USERNAME, response)
            msg_type2, fields2 = self._ws_send_recv_tlv(ws, response_msg)

            # 验证收到Register-Result消息（0x04）
            self.assertEqual(msg_type2, MessageType.REGISTER_RESULT)
            result = fields2.get('result', 1)
            self.assertEqual(result, 0, f"注册失败: {fields2.get('error_message', 'unknown error')}")

            token = fields2.get('token')
            self.assertTrue(token, "Token should be present")
            return token
        finally:
            ws.close()

    def _ws_register_and_keep_connection_tlv(self):
        """TLV版本：注册并返回保持打开的 WebSocket 连接和 token（调用者负责关闭）"""
        if not WS_ENABLE:
            self.skipTest('WS 未启用')
        if not AGENT_NTLM_HASH:
            self.skipTest('未提供 EXEC_AGENT_NTLM_HASH，无法计算CHAP摘要')
        ws = self._open_ws()

        # 发送TLV格式的Register-Request消息
        register_msg = encode_register_request(AGENT_NAME, AGENT_USERNAME)
        msg_type, fields = self._ws_send_recv_tlv(ws, register_msg)

        # 验证收到Register-Challenge消息（0x02）
        self.assertEqual(msg_type, MessageType.REGISTER_CHALLENGE)
        challenge_data = fields.get('challenge')
        self.assertIsNotNone(challenge_data)

        # 计算MD5响应 - challenge_data可能是bytes或str
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

        response = compute_chap_response(AGENT_NTLM_HASH, challenge_hex)

        # 从Register-Challenge消息中提取challenge_id
        challenge_id_raw = fields.get('challenge_id')
        if isinstance(challenge_id_raw, str):
            # 如果是字符串，尝试转换为int
            try:
                challenge_id = int(challenge_id_raw.strip('\x00'))
            except ValueError:
                challenge_id = 0  # 默认值
        elif isinstance(challenge_id_raw, int):
            challenge_id = challenge_id_raw
        elif isinstance(challenge_id_raw, bytes):
            # 如果是字节数据，尝试转换为int
            try:
                challenge_id = int.from_bytes(challenge_id_raw, byteorder='big')
            except ValueError:
                challenge_id = 0
        else:
            challenge_id = 0  # 默认值

        print(f"DEBUG: Using challenge_id={challenge_id} (raw: {challenge_id_raw}, type: {type(challenge_id_raw)})")
        response_msg = encode_register_response(challenge_id, AGENT_USERNAME, response)
        msg_type2, fields2 = self._ws_send_recv_tlv(ws, response_msg)

        # 验证收到Register-Result消息（0x04）
        self.assertEqual(msg_type2, MessageType.REGISTER_RESULT)
        result = fields2.get('result', 1)
        self.assertEqual(result, 0, f"注册失败: {fields2.get('error_message', 'unknown error')}")

        token = fields2.get('token')
        self.assertTrue(token, "Token should be present")
        return ws, token



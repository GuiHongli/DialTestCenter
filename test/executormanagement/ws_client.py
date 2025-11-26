import ssl
import json
from typing import Any, Dict, Optional

import websocket  # type: ignore

from .config import WS_URL, WS_TIMEOUT


class ExecutorWsClient:
    """
    执行机 WebSocket 客户端封装。

    仅关注 Text(JSON) + Binary 两种消息类型，底层细节对测试用例透明。
    """

    def __init__(self, url: str = WS_URL, timeout: int = WS_TIMEOUT) -> None:
        self._url = url
        self._timeout = timeout
        self._ws: Optional[websocket.WebSocket] = None

    def connect(self) -> None:
        if self._ws is not None:
            return
        sslopt = {"cert_reqs": ssl.CERT_NONE}
        self._ws = websocket.create_connection(self._url, timeout=self._timeout, sslopt=sslopt)

    def close(self) -> None:
        if self._ws is None:
            return
        try:
            self._ws.close()
        finally:
            self._ws = None

    def send_json(self, envelope: Dict[str, Any]) -> None:
        if self._ws is None:
            raise RuntimeError("WebSocket is not connected")
        self._ws.send(json.dumps(envelope))

    def recv_json(self) -> Dict[str, Any]:
        if self._ws is None:
            raise RuntimeError("WebSocket is not connected")
        raw = self._ws.recv()
        if isinstance(raw, bytes):
            raw = raw.decode("utf-8")
        return json.loads(raw)

    def send_binary(self, data: bytes) -> None:
        if self._ws is None:
            raise RuntimeError("WebSocket is not connected")
        self._ws.send(data, opcode=websocket.ABNF.OPCODE_BINARY)




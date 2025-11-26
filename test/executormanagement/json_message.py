import json
from typing import Any, Dict, Optional, Tuple


class JsonMessageHelper:
    """
    WebSocket JSON 信封辅助工具。

    统一构造/解析与执行机管理协议一致的信封格式：
    {
      "type": "RegisterRequest",
      "token": 1234567890123456,  # 可选
      "payload": { ... }
    }

    同时兼容历史格式：
    {
      "message_type": "register_request",
      "data": { ... }
    }
    """

    def build(self, msg_type: str, payload: Dict[str, Any], token: Optional[int] = None) -> Dict[str, Any]:
        envelope: Dict[str, Any] = {
            "type": msg_type,
            "payload": payload,
        }
        if token is not None:
            envelope["token"] = token
        return envelope

    def parse(self, data: Dict[str, Any]) -> Tuple[str, Optional[int], Dict[str, Any]]:
        # 新格式优先
        if "type" in data and "payload" in data:
            msg_type = data["type"]
            token = data.get("token")
            payload = data["payload"]
            return msg_type, token, payload

        # 兼容旧格式：message_type + data
        if "message_type" in data and "data" in data:
            msg_type = data["message_type"]
            token = data.get("token")
            payload = data["data"]
            return msg_type, token, payload

        # 兜底：尽量推断
        msg_type = data.get("type") or data.get("message_type") or ""
        token = data.get("token")
        payload = data.get("payload") or data.get("data") or {}
        return msg_type, token, payload


def dumps_envelope(envelope: Dict[str, Any]) -> str:
    """JSON 序列化封装，便于调试日志使用。"""
    return json.dumps(envelope, ensure_ascii=False)



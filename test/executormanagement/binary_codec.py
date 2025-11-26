import base64
import binascii
import hashlib
import zlib
from typing import Union


class BinaryCodec:
    """二进制编码/校验工具（Base64/Hex/CRC32）。"""

    @staticmethod
    def encode_base64(data: bytes) -> str:
        return base64.b64encode(data).decode("ascii")

    @staticmethod
    def decode_base64(text: str) -> bytes:
        return base64.b64decode(text)

    @staticmethod
    def encode_hex(data: bytes) -> str:
        return binascii.hexlify(data).decode("ascii")

    @staticmethod
    def decode_hex(text: str) -> bytes:
        return binascii.unhexlify(text)

    @staticmethod
    def crc32_hex(data: bytes) -> str:
        """返回 8 字符十六进制 CRC32（小写，不带 0x 前缀）。"""
        value = zlib.crc32(data) & 0xFFFFFFFF
        return f"{value:08x}"

    @staticmethod
    def compute_chap_response(ntlm_hash_hex: str, challenge: Union[str, bytes]) -> str:
        """
        CHAP 摘要：MD5(NTLM-Hash bytes + Challenge bytes) -> 32 字符十六进制。
        challenge 可以是十六进制字符串或原始 bytes。
        """
        ntlm_bytes = binascii.unhexlify(ntlm_hash_hex)
        if isinstance(challenge, bytes):
            challenge_bytes = challenge
        else:
            challenge_bytes = binascii.unhexlify(challenge)
        combined = ntlm_bytes + challenge_bytes
        md5 = hashlib.md5()
        md5.update(combined)
        return md5.hexdigest()



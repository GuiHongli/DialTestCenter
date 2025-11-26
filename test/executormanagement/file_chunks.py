from typing import List


class FileChunks:
    """文件分片/重组工具，用于脚本、APK、日志等大文件的 Binary 传输。"""

    DEFAULT_CHUNK_SIZE = 102400  # 100KB

    @staticmethod
    def split(path: str, chunk_size: int = None) -> List[bytes]:
        size = chunk_size or FileChunks.DEFAULT_CHUNK_SIZE
        chunks: List[bytes] = []
        with open(path, "rb") as f:
            while True:
                data = f.read(size)
                if not data:
                    break
                chunks.append(data)
        return chunks

    @staticmethod
    def join(chunks: List[bytes]) -> bytes:
        return b"".join(chunks)



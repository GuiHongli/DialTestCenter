from typing import Any, Dict, List, Mapping, Optional

from .utils import DatabaseHelper


class DbHelper(DatabaseHelper):
    """
    执行机管理场景专用的轻量 DB 助手。

    在现有 DatabaseHelper 的基础上，提供更语义化的方法名，便于测试场景调用。
    """

    def get_executor(self, name: str) -> Optional[Dict[str, Any]]:
        return self.get_executor_by_name(name)

    def get_ues(self, executor_name: str) -> List[Mapping[str, Any]]:
        return self.get_ues_by_executor(executor_name)



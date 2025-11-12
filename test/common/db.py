from typing import Any, Dict, List, Optional, Tuple
import logging

import psycopg2


logger = logging.getLogger(__name__)


class BaseDatabaseHelper:
    def __init__(self, config: Dict[str, Any]):
        self.config = config
        self.conn = None

    def connect(self) -> None:
        if self.conn:
            return
        try:
            self.conn = psycopg2.connect(
                host=self.config['host'],
                port=self.config['port'],
                database=self.config['database'],
                user=self.config['user'],
                password=self.config['password'],
            )
            logger.info("Database connected: %s", self.config['database'])
        except Exception as exc:
            logger.error("Failed to connect database: %s", exc)
            raise

    def close(self) -> None:
        if self.conn:
            self.conn.close()
            self.conn = None
            logger.info("Database connection closed")

    def execute_query(self, query: str, params: Tuple = ()) -> List[Dict[str, Any]]:
        if not self.conn:
            self.connect()
        cursor = self.conn.cursor()
        try:
            cursor.execute(query, params)
            columns = [desc[0] for desc in cursor.description] if cursor.description else []
            rows = cursor.fetchall() if cursor.description else []
            return [dict(zip(columns, row)) for row in rows]
        finally:
            cursor.close()

    def execute_update(self, query: str, params: Tuple = ()) -> int:
        if not self.conn:
            self.connect()
        cursor = self.conn.cursor()
        try:
            cursor.execute(query, params)
            self.conn.commit()
            logger.info("Updated %s rows", cursor.rowcount)
            return cursor.rowcount
        except Exception as exc:
            self.conn.rollback()
            logger.error("Update failed: %s", exc)
            raise
        finally:
            cursor.close()

    def truncate_table(self, table_name: str) -> None:
        self.execute_update(f"TRUNCATE TABLE {table_name} CASCADE")



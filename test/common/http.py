import json
import logging
from typing import Any, Dict, Optional

import requests


logger = logging.getLogger(__name__)


class APIClient:
    def __init__(self, base_url: str, request_config: Dict[str, Any]) -> None:
        self.base_url = base_url.rstrip('/')
        self.request_config = request_config
        self.session = requests.Session()
        self.session.headers.update(request_config.get('headers', {}))

    def _url(self, path: str) -> str:
        return f"{self.base_url}{path}" if not path.startswith('http') else path

    def _request(self, method: str, path: str, **kwargs) -> requests.Response:
        url = self._url(path)
        try:
            logger.debug("[%s] %s", method, url)
            if 'json' in kwargs:
                logger.debug("Request body: %s", json.dumps(kwargs['json'], ensure_ascii=False))
            response = self.session.request(
                method,
                url,
                timeout=self.request_config.get('timeout', 30),
                verify=self.request_config.get('verify_ssl', False),
                **kwargs,
            )
            logger.debug("Status Code: %s", response.status_code)
            if response.text:
                logger.debug("Response body: %s", response.text[:500])
            return response
        except Exception as exc:
            logger.error("Request failed: %s", exc)
            raise

    def get(self, path: str, params: Optional[Dict[str, Any]] = None) -> requests.Response:
        return self._request('GET', path, params=params or {})

    def post(self, path: str, data: Optional[Dict[str, Any]] = None) -> requests.Response:
        return self._request('POST', path, json=data or {})

    def put(self, path: str, data: Optional[Dict[str, Any]] = None) -> requests.Response:
        return self._request('PUT', path, json=data or {})

    def delete(self, path: str) -> requests.Response:
        return self._request('DELETE', path)



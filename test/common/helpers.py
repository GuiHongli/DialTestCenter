from typing import Any, Dict, Tuple
import time
import json


def measure_response_time_seconds(func, *args, **kwargs) -> Tuple[Any, float]:
    start_time = time.time()
    result = func(*args, **kwargs)
    end_time = time.time()
    return result, end_time - start_time


def measure_response_time_ms(func, *args, **kwargs) -> Tuple[Any, float]:
    result, seconds = measure_response_time_seconds(func, *args, **kwargs)
    return result, seconds * 1000.0


def wait_for_condition(condition_func, timeout: int = 5, interval: float = 0.5) -> bool:
    start_time = time.time()
    while time.time() - start_time < timeout:
        if condition_func():
            return True
        time.sleep(interval)
    return False


def json_to_string(data: Dict[str, Any]) -> str:
    return json.dumps(data, ensure_ascii=False, indent=2)


def pretty_json(data: Dict[str, Any]) -> str:
    return json_to_string(data)



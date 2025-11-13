#!/usr/bin/env python3
"""
Callback driver for DialingTestCenter taskmanagement module.

Features:
- Send SUCCESS/FAILED callbacks to the backend state machine via CallbackController
- Support scenario presets loaded from a JSON file
- Support ad-hoc plan via CLI (e.g., "SUCCESS:5,FAILED:3,SUCCESS:5")
- Per-step delay simulation and optional result_data
- HTTPS with optional certificate verification skip (self-signed dev cert)

Usage examples:
  # Happy path using presets (default 5s delay per step defined in scenarios.json)
  python test/taskmanagement/callback_driver.py \
    --main-task-id 101 --scenario happy_path --insecure

  # Custom plan: success->failed->success with delays 5s/3s/5s
  python test/taskmanagement/callback_driver.py \
    --main-task-id 101 --plan "SUCCESS:5,FAILED:3,SUCCESS:5" --insecure

  # Override base URL or provide result_data for all steps
  python test/taskmanagement/callback_driver.py \
    --main-task-id 101 --scenario model_train_failed \
    --base-url https://localhost:8087/dialingtest \
    --result-data '{"note":"stub run"}' --insecure

  # Backend-only flow: auto start a task then drive callbacks (no mainTaskId needed)
  python test/taskmanagement/callback_driver.py \
    --auto-start \
    --business-type VPN_BLOCK \
    --scenario TRAINING \
    --script-names vpn_app_001.py \
    --target-ues ue_serial_12345 \
    --plan "FAILED:5,SUCCESS:5,SUCCESS:5" \
    --insecure
"""

import argparse
import json
import sys
import time
from typing import Any, Dict, List, Optional

import requests

try:
    import urllib3
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
except Exception:
    # Not critical if urllib3 is unavailable
    pass


DEFAULT_BASE_URL = "https://localhost:8087/dialingtest"
DEFAULT_ENDPOINT = "/api/callbacks/notify"
DEFAULT_START_ENDPOINT = "/api/tasks/start"


class Step:
    def __init__(self, status: str, delay_seconds: int = 5, result_data: Optional[Dict[str, Any]] = None):
        if status not in ("SUCCESS", "FAILED"):
            raise ValueError("status must be SUCCESS or FAILED")
        if delay_seconds < 0:
            raise ValueError("delay_seconds must be >= 0")
        self.status = status
        self.delay_seconds = delay_seconds
        self.result_data = result_data or None

    def __repr__(self) -> str:
        return f"Step(status={self.status}, delay={self.delay_seconds}s, result_data={bool(self.result_data)})"


def load_scenarios(file_path: str) -> Dict[str, List[Step]]:
    with open(file_path, "r", encoding="utf-8") as f:
        raw = json.load(f)
    scenarios: Dict[str, List[Step]] = {}
    for name, steps in raw.items():
        parsed: List[Step] = []
        if not isinstance(steps, list):
            raise ValueError(f"Scenario '{name}' must be a list of steps")
        for s in steps:
            status = str(s.get("status", "")).upper()
            delay = int(s.get("delay", 5))
            result_data = s.get("result_data")
            parsed.append(Step(status=status, delay_seconds=delay, result_data=result_data))
        scenarios[name] = parsed
    return scenarios


def parse_plan(plan: str) -> List[Step]:
    """
    Parse a simple plan string like: "SUCCESS:5,FAILED:3,SUCCESS:5"
    where each token is STATUS:DELAY_SECONDS
    """
    steps: List[Step] = []
    if not plan:
        return steps
    parts = [p.strip() for p in plan.split(",") if p.strip()]
    for token in parts:
        if ":" in token:
            st, delay = token.split(":", 1)
            status = st.strip().upper()
            delay_seconds = int(delay.strip())
        else:
            status = token.strip().upper()
            delay_seconds = 5
        steps.append(Step(status=status, delay_seconds=delay_seconds))
    return steps


def send_callback(base_url: str, endpoint: str, main_task_id: int, status: str, result_data: Optional[Dict[str, Any]],
                  insecure: bool, timeout: int) -> requests.Response:
    url = base_url.rstrip("/") + endpoint
    body: Dict[str, Any] = {
        "mainTaskId": main_task_id,
        # The controller also accepts main_task_id; providing one key is sufficient
        "status": status,
    }
    if result_data is not None:
        body["result_data"] = result_data
    # No auth by default per SecurityConfig (permitAll)
    headers = {"Accept": "application/json"}
    resp = requests.post(url, json=body, headers=headers, verify=not insecure, timeout=timeout)
    return resp


def run_steps(steps: List[Step], base_url: str, endpoint: str, main_task_id: int,
              insecure: bool, timeout: int, global_result_data: Optional[Dict[str, Any]], dry_run: bool,
              show_status: bool = True) -> None:
    if not steps:
        print("No steps to run. Nothing to do.")
        return
    print(f"Target: {base_url}{endpoint} | mainTaskId={main_task_id} | steps={len(steps)}")
    for idx, step in enumerate(steps, start=1):
        print(f"[Step {idx}/{len(steps)}] Waiting {step.delay_seconds}s before sending {step.status} ...")
        time.sleep(step.delay_seconds)
        payload = step.result_data if step.result_data is not None else global_result_data
        if dry_run:
            print(f"DRY-RUN send: status={step.status}, result_data={(payload if payload else None)}")
            continue
        try:
            resp = send_callback(base_url, endpoint, main_task_id, step.status, payload, insecure, timeout)
            print(f"Sent {step.status}, HTTP {resp.status_code}")
            if show_status:
                try:
                    detail = fetch_task_detail(base_url, main_task_id, insecure, timeout)
                    if detail is not None:
                        status = detail.get("status")
                        result = detail.get("result")
                        ctx_text = detail.get("context")
                        step_name = None
                        if isinstance(ctx_text, str) and ctx_text.strip():
                            try:
                                ctx_obj = json.loads(ctx_text)
                                step_name = ctx_obj.get("step")
                            except Exception:
                                step_name = None
                        print(f"Task {main_task_id}: status={status} result={result} step={step_name}")
                except Exception:
                    pass
            if resp.status_code >= 400:
                # Print response body for easier diagnosis
                try:
                    print(f"Body: {resp.text}")
                except Exception:
                    pass
        except requests.RequestException as e:
            print(f"Request failed: {e}")
            # Continue to next step to allow long-running plan execution even if one step fails


def start_task(base_url: str, endpoint: str, insecure: bool, timeout: int, body: Dict[str, Any]) -> int:
    url = base_url.rstrip("/") + endpoint
    headers = {"Accept": "application/json", "Content-Type": "application/json"}
    resp = requests.post(url, json=body, headers=headers, verify=not insecure, timeout=timeout)
    if resp.status_code not in (200, 201, 202):
        # Fallback: Known server issue returning 500 after creating the task (serialization error).
        # Try to fetch latest task id from the list endpoint.
        if resp.status_code == 500:
            try:
                fallback_id = fetch_latest_main_task_id(base_url, insecure, timeout)
                if fallback_id is not None:
                    print(f"Server returned 500 but a new task likely created. Fallback to latest mainTaskId={fallback_id}")
                    return int(fallback_id)
            except Exception:
                pass
        try:
            detail = resp.text
        except Exception:
            detail = ""
        raise RuntimeError(f"Start task failed: HTTP {resp.status_code} {detail}")
    try:
        data = resp.json()
    except ValueError:
        raise RuntimeError("Start task response is not JSON")
    task_id = data.get("id")
    if task_id is None:
        # Fallback keys if response wrapper changes
        if isinstance(data, dict):
            for k in ("taskId", "task_id", "mainTaskId", "main_task_id"):
                if k in data:
                    task_id = data[k]
                    break
    if task_id is None:
        raise RuntimeError("Cannot find task id in start response")
    return int(task_id)


def fetch_latest_main_task_id(base_url: str, insecure: bool, timeout: int) -> Optional[int]:
    """
    Fetch latest main task id by querying first page and taking the max id.
    This is a best-effort fallback used only when start API returns 500 due to serialization error.
    """
    import math
    url = base_url.rstrip("/") + "/api/tasks?page=0&size=10"
    headers = {"Accept": "application/json"}
    resp = requests.get(url, headers=headers, verify=not insecure, timeout=timeout)
    if resp.status_code != 200:
        return None
    try:
        data = resp.json()
    except ValueError:
        return None
    content = data.get("content") or []
    ids = [item.get("id") for item in content if isinstance(item, dict) and item.get("id") is not None]
    if not ids:
        return None
    try:
        return max(int(i) for i in ids)
    except Exception:
        return None


def fetch_task_detail(base_url: str, task_id: int, insecure: bool, timeout: int) -> Optional[Dict[str, Any]]:
    url = base_url.rstrip("/") + f"/api/tasks/{task_id}"
    headers = {"Accept": "application/json"}
    resp = requests.get(url, headers=headers, verify=not insecure, timeout=timeout)
    if resp.status_code != 200:
        return None
    try:
        return resp.json()
    except ValueError:
        return None


def main(argv: Optional[List[str]] = None) -> int:
    parser = argparse.ArgumentParser(description="DialingTestCenter Callback Driver")
    parser.add_argument("--base-url", default=DEFAULT_BASE_URL, help="Base URL, e.g., https://localhost:8087/dialingtest")
    parser.add_argument("--endpoint", default=DEFAULT_ENDPOINT, help="Callback endpoint path (default: /api/callbacks/notify)")
    parser.add_argument("--main-task-id", type=int, required=False, help="Main task ID to attach callbacks to")
    # Auto-start options
    parser.add_argument("--auto-start", action="store_true", help="Create a main task via /api/tasks/start before callbacks")
    parser.add_argument("--start-endpoint", default=DEFAULT_START_ENDPOINT, help="Start endpoint path (default: /api/tasks/start)")
    parser.add_argument("--business-type", help="businessType for task start (required when --auto-start without --main-task-id)")
    parser.add_argument("--start-scenario", help="Scenario value for task start request (e.g., TRAINING)")
    parser.add_argument("--script-names", help="Comma-separated script names for task start (required when --auto-start without --main-task-id)")
    parser.add_argument("--target-ues", help="Comma-separated target UEs for task start (required when --auto-start without --main-task-id)")
    parser.add_argument("--failed-apps", help="Comma-separated failed apps for task start (optional)")
    parser.add_argument("--scenario", help="Scenario name defined in scenarios JSON file (for callbacks)")
    parser.add_argument("--scenarios-file", default="test/taskmanagement/scenarios.json", help="Path to scenarios JSON file")
    parser.add_argument("--plan", help="Ad-hoc plan like 'SUCCESS:5,FAILED:3,SUCCESS:5'")
    parser.add_argument("--result-data", help="JSON string applied to steps lacking per-step result_data")
    parser.add_argument("--insecure", action="store_true", help="Disable TLS verification (use for local self-signed cert)")
    parser.add_argument("--timeout", type=int, default=15, help="HTTP timeout seconds (default: 15)")
    parser.add_argument("--dry-run", action="store_true", help="Print steps but do not send requests")

    args = parser.parse_args(argv)

    # Resolve steps plan
    steps: List[Step] = []
    if args.plan:
        steps = parse_plan(args.plan)
    elif args.scenario:
        # If --auto-start is used, --scenario may mean two things:
        #  - a scenario name for callbacks (preferred)
        #  - when not found in file, we treat it as start request scenario value
        scenarios = load_scenarios(args.scenarios_file)
        if args.scenario in scenarios:
            steps = scenarios[args.scenario]
        else:
            # Not found in scenario file; allow using plan only
            if not args.plan:
                print(f"Scenario '{args.scenario}' not found in {args.scenarios_file}. Provide --plan or correct scenario name.")
                return 2
    else:
        print("Either --plan or --scenario must be provided.")
        return 2

    global_result_data: Optional[Dict[str, Any]] = None
    if args.result_data:
        try:
            global_result_data = json.loads(args.result_data)
        except json.JSONDecodeError as e:
            print(f"Invalid --result-data JSON: {e}")
            return 2

    # Determine main task id (auto-start if requested and not provided)
    main_task_id: Optional[int] = args.main_task_id
    if args.auto_start and main_task_id is None:
        # Validate required fields
        if not args.business_type or not args.script_names or not args.target_ues:
            print("When using --auto-start without --main-task-id, --business-type, --script-names, and --target-ues are required.")
            return 2
        start_body: Dict[str, Any] = {
            "business_type": args.business_type,
            "script_names": [s.strip() for s in args.script_names.split(",") if s.strip()],
            "target_ues": [u.strip() for u in args.target_ues.split(",") if u.strip()],
        }
        if args.failed_apps:
            start_body["failed_apps"] = [a.strip() for a in args.failed_apps.split(",") if a.strip()]
        # Determine start scenario value
        start_scn_value = args.start_scenario
        if not start_scn_value:
            start_scn_value = "TRAINING"
        start_body["scenario"] = start_scn_value
        try:
            main_task_id = start_task(args.base_url, args.start_endpoint, args.insecure, args.timeout, start_body)
            print(f"Auto-started task: mainTaskId={main_task_id}")
        except Exception as e:
            print(str(e))
            return 2

    if main_task_id is None:
        print("--main-task-id is required unless --auto-start is used with valid start parameters.")
        return 2

    try:
        run_steps(
            steps=steps,
            base_url=args.base_url,
            endpoint=args.endpoint,
            main_task_id=main_task_id,
            insecure=args.insecure,
            timeout=args.timeout,
            global_result_data=global_result_data,
            dry_run=args.dry_run,
            show_status=True,
        )
        return 0
    except KeyboardInterrupt:
        print("Interrupted by user.")
        return 130


if __name__ == "__main__":
    sys.exit(main())



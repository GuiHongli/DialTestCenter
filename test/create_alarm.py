#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
告警创建测试脚本

用于测试告警创建API接口
"""

import json
import sys
import argparse
import requests
from typing import Dict, Any, Optional

# 默认配置
DEFAULT_BASE_URL = 'https://localhost:8087'
DEFAULT_API_PREFIX = '/dialingtest/api'


def create_alarm(
    base_url: str,
    alarm_summary: str,
    alarm_level: str,
    alarm_description: Optional[str] = None,
    username: str = 'admin',
    csrf_token: str = 'test-csrf-token',
    verify_ssl: bool = False
) -> Dict[str, Any]:
    """
    创建告警
    
    Args:
        base_url: API基础URL
        alarm_summary: 告警概述
        alarm_level: 告警等级 (Urgent/Important/Minor)
        alarm_description: 告警详细描述（可选）
        username: 用户名
        csrf_token: CSRF令牌
        verify_ssl: 是否验证SSL证书
    
    Returns:
        响应数据字典
    """
    url = f"{base_url}{DEFAULT_API_PREFIX}/alarms"
    
    headers = {
        'Content-Type': 'application/json',
        'X-Csrf-Token': csrf_token,
        'X-Username': username,
    }
    
    data = {
        'alarmSummary': alarm_summary,
        'alarmLevel': alarm_level,
    }
    
    if alarm_description:
        data['alarmDescription'] = alarm_description
    
    try:
        print(f"Creating alarm: {alarm_summary} (Level: {alarm_level})")
        print(f"URL: {url}")
        print(f"Request data: {json.dumps(data, ensure_ascii=False, indent=2)}")
        
        response = requests.post(
            url,
            json=data,
            headers=headers,
            verify=verify_ssl,
            timeout=30
        )
        
        print(f"Status Code: {response.status_code}")
        
        if response.text:
            response_data = response.json()
            print(f"Response: {json.dumps(response_data, ensure_ascii=False, indent=2)}")
            
            if response.status_code == 201:
                print("✓ Alarm created successfully")
                if response_data.get('data'):
                    print(f"  Alarm ID: {response_data['data'].get('id')}")
            else:
                print(f"✗ Failed to create alarm: {response_data.get('message', 'Unknown error')}")
            
            return response_data
        else:
            print("✗ Empty response")
            return {}
            
    except requests.exceptions.RequestException as e:
        print(f"✗ Request failed: {e}")
        raise
    except json.JSONDecodeError as e:
        print(f"✗ Failed to parse response: {e}")
        print(f"Response text: {response.text}")
        raise


def main():
    """主函数"""
    parser = argparse.ArgumentParser(description='Create alarm test script')
    parser.add_argument(
        '--base-url',
        default=DEFAULT_BASE_URL,
        help=f'API base URL (default: {DEFAULT_BASE_URL})'
    )
    parser.add_argument(
        '--summary',
        required=True,
        help='Alarm summary (required)'
    )
    parser.add_argument(
        '--level',
        required=True,
        choices=['Urgent', 'Important', 'Minor'],
        help='Alarm level: Urgent, Important, or Minor (required)'
    )
    parser.add_argument(
        '--description',
        default=None,
        help='Alarm description (optional)'
    )
    parser.add_argument(
        '--username',
        default='admin',
        help='Username (default: admin)'
    )
    parser.add_argument(
        '--csrf-token',
        default='test-csrf-token',
        help='CSRF token (default: test-csrf-token)'
    )
    parser.add_argument(
        '--verify-ssl',
        action='store_true',
        help='Verify SSL certificate (default: False)'
    )
    
    args = parser.parse_args()
    
    try:
        result = create_alarm(
            base_url=args.base_url,
            alarm_summary=args.summary,
            alarm_level=args.level,
            alarm_description=args.description,
            username=args.username,
            csrf_token=args.csrf_token,
            verify_ssl=args.verify_ssl
        )
        
        if result.get('success'):
            sys.exit(0)
        else:
            sys.exit(1)
            
    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)


if __name__ == '__main__':
    main()


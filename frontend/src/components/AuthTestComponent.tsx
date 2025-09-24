import React, { useState, useEffect } from 'react';
import { Button, Input, message, Card, Space } from 'antd';
import { setXUsernameCookie, removeXUsernameCookie, hasXUsernameCookie } from '../utils/cookieUtils';
import { getXUsernameFromCookie } from '../utils/apiUtils';
import TestCaseSetService from '../services/testCaseSetService';

/**
 * 用户认证测试组件
 * 用于测试X-Username认证功能
 */
const AuthTestComponent: React.FC = () => {
  const [username, setUsername] = useState<string>('');
  const [currentUsername, setCurrentUsername] = useState<string>('');
  const [testResult, setTestResult] = useState<string>('');

  useEffect(() => {
    // 页面加载时检查当前用户名
    updateCurrentUsername();
  }, []);

  const updateCurrentUsername = () => {
    const current = getXUsernameFromCookie();
    setCurrentUsername(current);
  };

  const handleSetCookie = () => {
    if (!username.trim()) {
      message.warning('请输入用户名');
      return;
    }
    
    setXUsernameCookie(username.trim());
    message.success(`已设置用户名为: ${username.trim()}`);
    updateCurrentUsername();
    setUsername('');
  };

  const handleRemoveCookie = () => {
    removeXUsernameCookie();
    message.success('已清除用户名cookie');
    updateCurrentUsername();
  };

  const handleTestApi = async () => {
    try {
      setTestResult('正在测试API...');
      
      // 测试获取用例集列表
      const result = await TestCaseSetService.getTestCaseSets(1, 10);
      
      setTestResult(`API测试成功！获取到 ${result.data.length} 个用例集`);
      message.success('API调用成功');
    } catch (error) {
      const errorMsg = error instanceof Error ? error.message : '未知错误';
      setTestResult(`API测试失败: ${errorMsg}`);
      message.error('API调用失败');
    }
  };

  return (
    <Card title="X-Username 认证测试" style={{ margin: '20px' }}>
      <Space direction="vertical" style={{ width: '100%' }}>
        <div>
          <h4>当前用户名: {currentUsername}</h4>
          <p>Cookie状态: {hasXUsernameCookie() ? '已设置' : '未设置'}</p>
        </div>

        <div>
          <Space>
            <Input
              placeholder="输入用户名"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              onPressEnter={handleSetCookie}
            />
            <Button type="primary" onClick={handleSetCookie}>
              设置用户名
            </Button>
            <Button onClick={handleRemoveCookie}>
              清除Cookie
            </Button>
          </Space>
        </div>

        <div>
          <Button type="default" onClick={handleTestApi}>
            测试API调用
          </Button>
        </div>

        {testResult && (
          <div style={{ 
            padding: '10px', 
            backgroundColor: '#f5f5f5', 
            borderRadius: '4px',
            whiteSpace: 'pre-wrap'
          }}>
            {testResult}
          </div>
        )}

        <div style={{ fontSize: '12px', color: '#666' }}>
          <p>使用说明：</p>
          <ul>
            <li>设置用户名后，所有API请求都会自动在header中携带X-Username</li>
            <li>如果未设置cookie，将使用默认用户名 'admin'</li>
            <li>点击"测试API调用"可以验证认证是否正常工作</li>
          </ul>
        </div>
      </Space>
    </Card>
  );
};

export default AuthTestComponent;

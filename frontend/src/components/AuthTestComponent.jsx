import React, { useState, useEffect } from 'react';
import { Button, Input, message, Card, Space } from 'antd';
import { setXUsernameCookie, removeXUsernameCookie, hasXUsernameCookie } from '../utils/cookieUtils.js';
import { getXUsernameFromCookie } from '../utils/apiUtils.js';
import TestCaseSetService from '../services/testCaseSetService.js';

/**
 * 用户认证测试组件
 * 用于测试X-Username认证功能
 */
const AuthTestComponent = () => {
  const [username, setUsername] = useState('');
  const [currentUsername, setCurrentUsername] = useState('');
  const [testResult, setTestResult] = useState('');

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
      
      if (result && result.data) {
        setTestResult(`API测试成功！获取到 ${result.data.length} 个用例集`);
        message.success('API测试成功');
      } else {
        setTestResult('API测试失败：返回数据格式不正确');
        message.error('API测试失败');
      }
    } catch (error) {
      const errorMsg = `API测试失败：${error.message || '未知错误'}`;
      setTestResult(errorMsg);
      message.error('API测试失败');
      console.error('API测试错误:', error);
    }
  };

  const handleCheckCookie = () => {
    const hasCookie = hasXUsernameCookie();
    const current = getXUsernameFromCookie();
    
    if (hasCookie && current) {
      message.success(`Cookie存在，当前用户: ${current}`);
    } else {
      message.warning('Cookie不存在或为空');
    }
  };

  return (
    <Card title="用户认证测试" style={{ margin: '20px' }}>
      <Space direction="vertical" style={{ width: '100%' }}>
        {/* 当前状态显示 */}
        <div>
          <strong>当前用户名: </strong>
          <span style={{ color: currentUsername ? '#52c41a' : '#ff4d4f' }}>
            {currentUsername || '未设置'}
          </span>
        </div>

        {/* 设置用户名 */}
        <div>
          <Space>
            <Input
              placeholder="请输入用户名"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              style={{ width: 200 }}
            />
            <Button type="primary" onClick={handleSetCookie}>
              设置用户名
            </Button>
          </Space>
        </div>

        {/* 操作按钮 */}
        <Space>
          <Button onClick={handleCheckCookie}>
            检查Cookie
          </Button>
          <Button onClick={handleRemoveCookie}>
            清除Cookie
          </Button>
          <Button type="primary" onClick={handleTestApi}>
            测试API
          </Button>
        </Space>

        {/* 测试结果 */}
        {testResult && (
          <div style={{ 
            padding: '10px', 
            backgroundColor: '#f5f5f5', 
            borderRadius: '4px',
            wordBreak: 'break-all'
          }}>
            <strong>测试结果: </strong>
            {testResult}
          </div>
        )}
      </Space>
    </Card>
  );
};

export default AuthTestComponent;

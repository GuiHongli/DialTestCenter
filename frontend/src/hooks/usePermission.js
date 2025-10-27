import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { createApiRequestConfig, getXUsernameFromCookie } from '../utils/apiUtils.js';

// 创建权限上下文
const PermissionContext = createContext(undefined);

// 权限提供者组件
export const PermissionProvider = ({ children }) => {
  const [userPermission, setUserPermission] = useState(null);
  const [loading, setLoading] = useState(true);

  // 获取用户权限
  const fetchUserPermission = useCallback(async () => {
    try {
      setLoading(true);
      
      // 确保cookie中存在xUsername
      if (!document.cookie.includes('xUsername=')) {
        // 如果cookie中不存在xUsername，设置默认值
        document.cookie = `xUsername=${encodeURIComponent('admin')}; path=/`;
        console.log('设置默认xUsername cookie');
      }
      
      // 检查缓存是否过期（5分钟）或username是否变化
      const cachedPermission = sessionStorage.getItem('userPermission');
      const cachedTime = sessionStorage.getItem('userPermissionTime');
      const cachedUsername = sessionStorage.getItem('userPermissionUsername');
      const currentUsername = getXUsernameFromCookie();
      const now = new Date().getTime();
      const cacheExpiry = 5 * 60 * 1000; // 5分钟
      
      // 如果缓存存在且未过期，且username没有变化，则使用缓存
      if (cachedPermission && cachedTime && cachedUsername && 
          (now - parseInt(cachedTime)) < cacheExpiry && 
          cachedUsername === currentUsername) {
        const parsed = JSON.parse(cachedPermission);
        setUserPermission(parsed);
        setLoading(false);
        console.log('使用缓存的权限信息，username:', currentUsername);
        return;
      }
      
      // 如果username发生变化，清除旧缓存
      if (cachedUsername && cachedUsername !== currentUsername) {
        console.log('检测到username变化，清除旧缓存:', cachedUsername, '->', currentUsername);
        sessionStorage.removeItem('userPermission');
        sessionStorage.removeItem('userPermissionTime');
        sessionStorage.removeItem('userPermissionUsername');
      }

      // 从后端获取权限信息，添加时间戳防止缓存
      const timestamp = new Date().getTime();
      const config = createApiRequestConfig('GET', undefined, true);
      
      // 添加缓存控制头
      const headers = {
        ...config.headers,
        'Cache-Control': 'no-cache',
        'Pragma': 'no-cache'
      };
      
      // 调试信息
      console.log('权限API请求配置:', {
        url: `/dialingtest/api/user-roles/permission?t=${timestamp}`,
        headers: headers,
        xUsername: headers['X-Username'],
        cookies: document.cookie
      });
      
      const response = await fetch(`/dialingtest/api/user-roles/permission?t=${timestamp}`, {
        ...config,
        headers
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      // 检查响应内容类型
      const contentType = response.headers.get('content-type');
      if (!contentType || !contentType.includes('application/json')) {
        const text = await response.text();
        console.error('API返回非JSON数据:', text.substring(0, 200));
        throw new Error('API返回非JSON数据，可能是路径错误或服务未启动');
      }
      
      const result = await response.json();
      
      // 调试响应数据
      console.log('权限API响应数据:', result);
      
      if (result.success && result.data) {
        const permission = {
          username: result.data.username,
          roles: result.data.roles,
          pagePermissions: result.data.pagePermissions || {}
        };
        
        setUserPermission(permission);
        // 缓存权限信息到sessionStorage
        sessionStorage.setItem('userPermission', JSON.stringify(permission));
        // 记录缓存时间和username
        sessionStorage.setItem('userPermissionTime', timestamp.toString());
        sessionStorage.setItem('userPermissionUsername', currentUsername);
        console.log('权限信息已缓存，username:', currentUsername);
      } else {
        throw new Error(result.message || '获取权限信息失败');
      }
    } catch (error) {
      console.error('获取用户权限失败:', error);
      
      // 如果网络请求失败，尝试从缓存中获取
      const cachedPermission = sessionStorage.getItem('userPermission');
      if (cachedPermission) {
        const parsed = JSON.parse(cachedPermission);
        setUserPermission(parsed);
        console.log('使用缓存的权限信息');
      } else {
        // 设置默认权限（无权限）
        setUserPermission({
          username: '',
          roles: [],
          pagePermissions: {}
        });
      }
    } finally {
      setLoading(false);
    }
  }, []);

  // 组件挂载时获取权限
  useEffect(() => {
    fetchUserPermission();
  }, [fetchUserPermission]);

  // 添加调试信息
  useEffect(() => {
    if (userPermission) {
      console.log('权限信息已加载:', {
        username: userPermission.username,
        roles: userPermission.roles,
        pagePermissions: userPermission.pagePermissions
      });
    }
  }, [userPermission]);

  // 检查是否有指定角色
  const hasRole = useCallback((role) => {
    return (userPermission && userPermission.roles && userPermission.roles.includes(role)) || false;
  }, [userPermission]);

  // 检查是否有任意一个指定角色
  const hasAnyRole = useCallback((roles) => {
    return roles.some(role => (userPermission && userPermission.roles && userPermission.roles.includes(role))) || false;
  }, [userPermission]);

  // 检查是否有所有指定角色
  const hasAllRoles = useCallback((roles) => {
    return roles.every(role => (userPermission && userPermission.roles && userPermission.roles.includes(role))) || false;
  }, [userPermission]);

  // 检查页面权限
  const hasPagePermission = useCallback((pageId, operation) => {
    if (!userPermission) return false;
    
    const pagePermission = userPermission.pagePermissions[pageId];
    if (!pagePermission) return false;
    
    if (!operation) return pagePermission.hasAccess;
    
    return pagePermission.operations.includes(operation);
  }, [userPermission]);

  // 刷新权限
  const refreshPermission = useCallback(async () => {
    // 清除所有缓存，强制重新获取
    sessionStorage.removeItem('userPermission');
    sessionStorage.removeItem('userPermissionTime');
    sessionStorage.removeItem('userPermissionUsername');
    console.log('清除权限缓存，强制重新获取');
    await fetchUserPermission();
  }, [fetchUserPermission]);

  // 监听cookie变化，检测username变化
  useEffect(() => {
    const checkUsernameChange = () => {
      const currentUsername = getXUsernameFromCookie();
      const cachedUsername = sessionStorage.getItem('userPermissionUsername');
      
      if (cachedUsername && cachedUsername !== currentUsername) {
        console.log('检测到cookie中username变化，自动刷新权限:', cachedUsername, '->', currentUsername);
        refreshPermission();
      }
    };

    // 监听storage事件（跨标签页同步）
    const handleStorageChange = (e) => {
      if (e.key === 'userPermissionUsername' && e.newValue !== e.oldValue) {
        console.log('检测到其他标签页username变化，刷新权限');
        refreshPermission();
      }
    };

    // 定期检查cookie变化（每30秒）
    const interval = setInterval(checkUsernameChange, 30000);
    
    // 监听storage事件
    window.addEventListener('storage', handleStorageChange);

    return () => {
      clearInterval(interval);
      window.removeEventListener('storage', handleStorageChange);
    };
  }, [refreshPermission]);

  const contextValue = {
    userPermission,
    loading,
    hasRole,
    hasAnyRole,
    hasAllRoles,
    hasPagePermission,
    refreshPermission,
    username: (userPermission && userPermission.username) || '',
    roles: (userPermission && userPermission.roles) || [],
  };

  return React.createElement(PermissionContext.Provider, { value: contextValue }, children);
};

// 权限 Hook
export const usePermission = () => {
  const context = useContext(PermissionContext);
  if (context === undefined) {
    throw new Error('usePermission must be used within a PermissionProvider');
  }
  return context;
};

// 页面权限组件
export const PagePermission = ({ 
  pageId, 
  operation, 
  children, 
  fallback = null 
}) => {
  const { hasPagePermission } = usePermission();
  
  const hasPermission = hasPagePermission(pageId, operation);
  
  return hasPermission ? React.createElement(React.Fragment, null, children) : React.createElement(React.Fragment, null, fallback);
};

// 角色权限组件
export const RolePermission = ({ 
  roles, 
  children, 
  fallback = null, 
  requireAll = false 
}) => {
  const { hasAnyRole, hasAllRoles } = usePermission();
  
  const hasPermission = requireAll ? hasAllRoles(roles) : hasAnyRole(roles);
  
  return hasPermission ? React.createElement(React.Fragment, null, children) : React.createElement(React.Fragment, null, fallback);
};

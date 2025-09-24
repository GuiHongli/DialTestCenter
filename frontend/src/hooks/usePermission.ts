import React, { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react';
import { createApiRequestConfig, getXUsernameFromCookie } from '../utils/apiUtils';

// 权限数据接口
export interface UserPermission {
  username: string;
  roles: string[];
  pagePermissions: Record<string, {
    hasAccess: boolean;
    operations: string[];
  }>;
}

// 权限上下文类型
export interface PermissionContextType {
  userPermission: UserPermission | null;
  loading: boolean;
  hasRole: (role: string) => boolean;
  hasAnyRole: (roles: string[]) => boolean;
  hasAllRoles: (roles: string[]) => boolean;
  hasPagePermission: (pageId: string, operation?: string) => boolean;
  refreshPermission: () => Promise<void>;
  username: string;
  roles: string[];
}

// 创建权限上下文
const PermissionContext = createContext<PermissionContextType | undefined>(undefined);

// 权限提供者组件
export const PermissionProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [userPermission, setUserPermission] = useState<UserPermission | null>(null);
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
        const permission: UserPermission = {
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
  const hasRole = useCallback((role: string): boolean => {
    return userPermission?.roles.includes(role) || false;
  }, [userPermission]);

  // 检查是否有任意一个指定角色
  const hasAnyRole = useCallback((roles: string[]): boolean => {
    return roles.some(role => userPermission?.roles.includes(role)) || false;
  }, [userPermission]);

  // 检查是否有所有指定角色
  const hasAllRoles = useCallback((roles: string[]): boolean => {
    return roles.every(role => userPermission?.roles.includes(role)) || false;
  }, [userPermission]);

  // 检查页面权限
  const hasPagePermission = useCallback((pageId: string, operation?: string): boolean => {
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
    const handleStorageChange = (e: StorageEvent) => {
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

  const contextValue: PermissionContextType = {
    userPermission,
    loading,
    hasRole,
    hasAnyRole,
    hasAllRoles,
    hasPagePermission,
    refreshPermission,
    username: userPermission?.username || '',
    roles: userPermission?.roles || [],
  };

  return React.createElement(PermissionContext.Provider, { value: contextValue }, children);
};

// 权限 Hook
export const usePermission = (): PermissionContextType => {
  const context = useContext(PermissionContext);
  if (context === undefined) {
    throw new Error('usePermission must be used within a PermissionProvider');
  }
  return context;
};

// 页面权限组件
export interface PagePermissionProps {
  pageId: string;
  operation?: string;
  children: ReactNode;
  fallback?: ReactNode;
}

export const PagePermission: React.FC<PagePermissionProps> = ({ 
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
export interface RolePermissionProps {
  roles: string[];
  children: ReactNode;
  fallback?: ReactNode;
  requireAll?: boolean;
}

export const RolePermission: React.FC<RolePermissionProps> = ({ 
  roles, 
  children, 
  fallback = null, 
  requireAll = false 
}) => {
  const { hasAnyRole, hasAllRoles } = usePermission();
  
  const hasPermission = requireAll ? hasAllRoles(roles) : hasAnyRole(roles);
  
  return hasPermission ? React.createElement(React.Fragment, null, children) : React.createElement(React.Fragment, null, fallback);
};
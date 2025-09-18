/**
 * 用户服务类，提供用户相关的API调用
 */

import { 
  User, 
  UserCreateParams, 
  UserUpdateParams, 
  UserSearchParams,
  PasswordValidationParams,
  PasswordValidationResponse
} from '../types/user';
import { handleApiResponse, handlePagedApiResponse, createApiRequestConfig, PagedResponse } from '../utils/apiUtils';

const API_BASE_URL = '/dialingtest/api/dialusers';

/**
 * 获取用户列表（分页）
 */
export const getUsers = async (page: number = 0, pageSize: number = 10, username?: string): Promise<PagedResponse<User>> => {
  const params = new URLSearchParams({
    page: page.toString(),
    size: pageSize.toString(),
  });
  
  if (username && username.trim()) {
    params.append('username', username.trim());
  }
  
  const response = await fetch(`${API_BASE_URL}?${params}`);
  
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  
  const result = await response.json();
  
  if (!result.success) {
    throw new Error(result.message || 'Failed to fetch users');
  }
  
  // 转换后端响应格式为前端期望的格式
  return {
    data: result.data.content || [],
    total: result.data.totalElements || 0,
    page: result.data.number || 0,
    pageSize: result.data.size || 10,
    totalPages: result.data.totalPages || 0,
    hasNext: (result.data.number || 0) < (result.data.totalPages || 0) - 1,
    hasPrevious: (result.data.number || 0) > 0,
  };
};

/**
 * 根据用户名获取用户
 */
export const getUserByUsername = async (username: string): Promise<User> => {
  const response = await fetch(`${API_BASE_URL}/username/${encodeURIComponent(username)}`);
  return handleApiResponse<User>(response);
};

/**
 * 创建新用户
 */
export const createUser = async (params: UserCreateParams): Promise<User> => {
  const response = await fetch(API_BASE_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(params),
  });
  
  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
  }
  
  const result = await response.json();
  
  if (!result.success) {
    throw new Error(result.message || 'Failed to create user');
  }
  
  return result.data;
};

/**
 * 更新用户信息
 */
export const updateUser = async (id: number, params: UserUpdateParams): Promise<User> => {
  const response = await fetch(`${API_BASE_URL}/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(params),
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
  }

  const result = await response.json();
  
  if (!result.success) {
    throw new Error(result.message || 'Failed to update user');
  }
  
  return result.data;
};

/**
 * 删除用户
 */
export const deleteUser = async (id: number): Promise<void> => {
  const response = await fetch(`${API_BASE_URL}/${id}`, {
    method: 'DELETE',
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
  }
};

/**
 * 搜索用户
 */
export const searchUsers = async (params: UserSearchParams): Promise<User[]> => {
  try {
    const queryParams = new URLSearchParams();
    if (params.username) {
      queryParams.append('username', params.username);
    }

    const response = await fetch(`${API_BASE_URL}/search?${queryParams}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('Failed to search users:', error);
    throw error;
  }
};

/**
 * 验证用户密码
 */
export const validatePassword = async (params: PasswordValidationParams): Promise<PasswordValidationResponse> => {
  try {
    const response = await fetch(`${API_BASE_URL}/validate-password`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(params),
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('Failed to validate password:', error);
    throw error;
  }
};

/**
 * 更新用户最后登录时间
 */
export const updateLastLoginTime = async (username: string): Promise<void> => {
  try {
    const response = await fetch(`${API_BASE_URL}/update-login-time`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username }),
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }
  } catch (error) {
    console.error(`Failed to update last login time for user ${username}:`, error);
    throw error;
  }
};




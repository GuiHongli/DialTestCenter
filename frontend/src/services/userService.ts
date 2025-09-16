/**
 * 用户服务类，提供用户相关的API调用
 */

import { 
  User, 
  UserCreateParams, 
  UserUpdateParams, 
  UserSearchParams,
  PasswordValidationParams,
  PasswordValidationResponse,
  ApiResponse 
} from '../types/user';

const API_BASE_URL = '/dialingtest/api/users';

/**
 * 获取所有用户列表
 */
export const getAllUsers = async (): Promise<User[]> => {
  try {
    const response = await fetch(API_BASE_URL);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return await response.json();
  } catch (error) {
    console.error('Failed to get all users:', error);
    throw error;
  }
};

/**
 * 根据ID获取用户
 */
export const getUserById = async (id: number): Promise<User> => {
  try {
    const response = await fetch(`${API_BASE_URL}/${id}`);
    if (!response.ok) {
      if (response.status === 404) {
        throw new Error('User not found');
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return await response.json();
  } catch (error) {
    console.error(`Failed to get user by ID ${id}:`, error);
    throw error;
  }
};

/**
 * 根据用户名获取用户
 */
export const getUserByUsername = async (username: string): Promise<User> => {
  try {
    const response = await fetch(`${API_BASE_URL}/username/${encodeURIComponent(username)}`);
    if (!response.ok) {
      if (response.status === 404) {
        throw new Error('User not found');
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return await response.json();
  } catch (error) {
    console.error(`Failed to get user by username ${username}:`, error);
    throw error;
  }
};

/**
 * 创建新用户
 */
export const createUser = async (params: UserCreateParams): Promise<User> => {
  try {
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

    return await response.json();
  } catch (error) {
    console.error('Failed to create user:', error);
    throw error;
  }
};

/**
 * 更新用户信息
 */
export const updateUser = async (id: number, params: UserUpdateParams): Promise<User> => {
  try {
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

    return await response.json();
  } catch (error) {
    console.error(`Failed to update user ${id}:`, error);
    throw error;
  }
};

/**
 * 删除用户
 */
export const deleteUser = async (id: number): Promise<void> => {
  try {
    const response = await fetch(`${API_BASE_URL}/${id}`, {
      method: 'DELETE',
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }
  } catch (error) {
    console.error(`Failed to delete user ${id}:`, error);
    throw error;
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


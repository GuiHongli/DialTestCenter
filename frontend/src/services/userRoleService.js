import { createApiRequestConfig } from '../utils/apiUtils.js';

const API_BASE_URL = '/dialingtest/api';

/**
 * 用户角色服务类
 */
export class UserRoleService {
  
  /**
   * 获取用户角色列表（分页）
   * @param page 页码（从0开始）
   * @param size 每页大小
   * @param search 搜索关键词（可选）
   * @returns 分页的用户角色列表
   */
  static async getUserRolesWithPagination(page = 0, size = 10, search) {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    
    if (search && search.trim()) {
      params.append('search', search.trim());
    }
    
    const response = await fetch(`${API_BASE_URL}/user-roles?${params}`, createApiRequestConfig('GET', undefined, false));
    
    if (!response.ok) {
      throw new Error(`获取用户角色列表失败: ${response.statusText}`);
    }
    
    const result = await response.json();
    if (!result.success) {
      throw new Error(result.message || 'Failed to get user roles');
    }
    
    return result.data;
  }
  
  /**
   * 创建用户角色
   * @param userRoleData 用户角色数据
   * @returns 创建的用户角色
   */
  static async createUserRole(userRoleData) {
    const response = await fetch(
      `${API_BASE_URL}/user-roles`,
      createApiRequestConfig('POST', userRoleData, true)
    );
    
    if (!response.ok) {
      const errorResult = await response.json();
      throw new Error(errorResult.message || `创建用户角色失败: ${response.statusText}`);
    }
    
    const result = await response.json();
    if (!result.success) {
      throw new Error(result.message || 'Failed to create user role');
    }
    
    return result.data;
  }
  
  /**
   * 更新用户角色
   * @param id 用户角色ID
   * @param userRoleData 用户角色数据
   * @returns 更新后的用户角色
   */
  static async updateUserRole(id, userRoleData) {
    const response = await fetch(
      `${API_BASE_URL}/user-roles/${id}`,
      createApiRequestConfig('PUT', userRoleData, true)
    );
    
    if (!response.ok) {
      const errorResult = await response.json();
      throw new Error(errorResult.message || `更新用户角色失败: ${response.statusText}`);
    }
    
    const result = await response.json();
    if (!result.success) {
      throw new Error(result.message || 'Failed to update user role');
    }
    
    return result.data;
  }
  
  /**
   * 删除用户角色
   * @param id 用户角色ID
   */
  static async deleteUserRole(id) {
    const response = await fetch(
      `${API_BASE_URL}/user-roles/${id}`,
      createApiRequestConfig('DELETE', undefined, true)
    );
    
    if (!response.ok) {
      const errorResult = await response.json();
      throw new Error(errorResult.message || `删除用户角色失败: ${response.statusText}`);
    }
  }
  
  
  
  /**
   * 获取EXECUTOR角色数量
   * @returns EXECUTOR角色数量
   */
  static async getExecutorCount() {
    const response = await fetch(
      `${API_BASE_URL}/user-roles/executor-count`,
      createApiRequestConfig('GET', undefined, false)
    );
    
    if (!response.ok) {
      throw new Error(`获取执行机数量失败: ${response.statusText}`);
    }
    
    const result = await response.json();
    if (!result.success) {
      throw new Error(result.message || 'Failed to get executor count');
    }
    
    return result.data;
  }
}

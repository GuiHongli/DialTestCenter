import { PermissionCheckRequest, PermissionCheckResult, UserRole, UserRoleFormData } from '../types/userRole';

const API_BASE_URL = '/dialingtest/api';

/**
 * 用户角色服务类
 */
export class UserRoleService {
  
  /**
   * 获取用户角色列表
   * @param username 用户名（可选）
   * @returns 用户角色列表
   */
  static async getUserRoles(username?: string): Promise<UserRole[]> {
    const url = username 
      ? `${API_BASE_URL}/user-roles?username=${encodeURIComponent(username)}`
      : `${API_BASE_URL}/user-roles`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'X-Username': 'admin' // 这里需要根据实际认证机制调整
      }
    });
    
    if (!response.ok) {
      throw new Error(`获取用户角色列表失败: ${response.statusText}`);
    }
    
    return response.json();
  }

  /**
   * 获取用户角色列表（分页）
   * @param page 页码（从1开始）
   * @param pageSize 每页大小
   * @param search 搜索关键词（可选）
   * @returns 分页的用户角色列表
   */
  static async getUserRolesWithPagination(page: number = 1, pageSize: number = 10, search?: string): Promise<{
    data: UserRole[];
    total: number;
    page: number;
    pageSize: number;
    totalPages: number;
  }> {
    const params = new URLSearchParams({
      page: page.toString(),
      pageSize: pageSize.toString(),
    });
    
    if (search && search.trim()) {
      params.append('search', search.trim());
    }
    
    const response = await fetch(`${API_BASE_URL}/user-roles?${params}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'X-Username': 'admin' // 这里需要根据实际认证机制调整
      }
    });
    
    if (!response.ok) {
      throw new Error(`获取用户角色列表失败: ${response.statusText}`);
    }
    
    return response.json();
  }
  
  /**
   * 创建用户角色
   * @param userRoleData 用户角色数据
   * @returns 创建的用户角色
   */
  static async createUserRole(userRoleData: UserRoleFormData): Promise<UserRole> {
    const response = await fetch(`${API_BASE_URL}/user-roles`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Username': 'admin' // 这里需要根据实际认证机制调整
      },
      body: JSON.stringify(userRoleData)
    });
    
    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`创建用户角色失败: ${errorText}`);
    }
    
    return response.json();
  }
  
  /**
   * 更新用户角色
   * @param id 用户角色ID
   * @param userRoleData 用户角色数据
   * @returns 更新后的用户角色
   */
  static async updateUserRole(id: number, userRoleData: UserRoleFormData): Promise<UserRole> {
    const response = await fetch(`${API_BASE_URL}/user-roles/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'X-Username': 'admin' // 这里需要根据实际认证机制调整
      },
      body: JSON.stringify(userRoleData)
    });
    
    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`更新用户角色失败: ${errorText}`);
    }
    
    return response.json();
  }
  
  /**
   * 删除用户角色
   * @param id 用户角色ID
   */
  static async deleteUserRole(id: number): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/user-roles/${id}`, {
      method: 'DELETE',
      headers: {
        'X-Username': 'admin' // 这里需要根据实际认证机制调整
      }
    });
    
    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`删除用户角色失败: ${errorText}`);
    }
  }
  
  /**
   * 检查用户权限
   * @param request 权限检查请求
   * @returns 权限检查结果
   */
  static async checkPermission(request: PermissionCheckRequest): Promise<PermissionCheckResult> {
    const response = await fetch(`${API_BASE_URL}/user-roles/check-permission`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Username': 'admin' // 这里需要根据实际认证机制调整
      },
      body: JSON.stringify(request)
    });
    
    if (!response.ok) {
      throw new Error(`权限检查失败: ${response.statusText}`);
    }
    
    return response.json();
  }
  
  /**
   * 获取执行机用户数量
   * @returns 执行机用户数量
   */
  static async getExecutorCount(): Promise<number> {
    const response = await fetch(`${API_BASE_URL}/user-roles/executor-count`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'X-Username': 'admin' // 这里需要根据实际认证机制调整
      }
    });
    
    if (!response.ok) {
      throw new Error(`获取执行机数量失败: ${response.statusText}`);
    }
    
    return response.json();
  }
}

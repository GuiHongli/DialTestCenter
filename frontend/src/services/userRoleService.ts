import { PermissionCheckRequest, PermissionCheckResult, UserRole, UserRoleFormData, ApiResponse, PagedResponse, RoleDefinition } from '../types/userRole';

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
  static async getUserRolesWithPagination(page: number = 0, size: number = 10, search?: string): Promise<PagedResponse<UserRole>> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
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
    
    const result: ApiResponse<PagedResponse<UserRole>> = await response.json();
    if (!result.success) {
      throw new Error(result.message || 'Failed to get user roles');
    }
    
    return result.data!;
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
      const errorResult: ApiResponse = await response.json();
      throw new Error(errorResult.message || `创建用户角色失败: ${response.statusText}`);
    }
    
    const result: ApiResponse<UserRole> = await response.json();
    if (!result.success) {
      throw new Error(result.message || 'Failed to create user role');
    }
    
    return result.data!;
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
      const errorResult: ApiResponse = await response.json();
      throw new Error(errorResult.message || `更新用户角色失败: ${response.statusText}`);
    }
    
    const result: ApiResponse<UserRole> = await response.json();
    if (!result.success) {
      throw new Error(result.message || 'Failed to update user role');
    }
    
    return result.data!;
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
      const errorResult: ApiResponse = await response.json();
      throw new Error(errorResult.message || `删除用户角色失败: ${response.statusText}`);
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
    
    const result: ApiResponse<PermissionCheckResult> = await response.json();
    if (!result.success) {
      throw new Error(result.message || 'Failed to check permission');
    }
    
    return result.data!;
  }
  
  /**
   * 获取所有角色定义
   * @returns 角色列表
   */
  static async getAllRoles(): Promise<RoleDefinition[]> {
    const response = await fetch(`${API_BASE_URL}/user-roles/roles`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'X-Username': 'admin' // 这里需要根据实际认证机制调整
      }
    });
    
    if (!response.ok) {
      throw new Error(`获取角色列表失败: ${response.statusText}`);
    }
    
    const result: ApiResponse<RoleDefinition[]> = await response.json();
    if (!result.success) {
      throw new Error(result.message || 'Failed to get roles');
    }
    
    return result.data!;
  }
}

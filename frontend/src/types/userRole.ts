// 用户角色类型定义

export type Role = 'ADMIN' | 'OPERATOR' | 'BROWSER' | 'EXECUTOR';

export interface UserRole {
  id: number;
  username: string;
  role: Role;
}

export interface UserRoleFormData {
  username: string;
  role: Role;
}

export interface PermissionCheckRequest {
  username: string;
  roles: string[];
}

export interface PermissionCheckResult {
  hasPermission: boolean;
  userRoles: string[];
}

export interface RoleDefinition {
  id: number;
  code: string;
  nameZh: string;
  nameEn: string;
  descriptionZh?: string;
  descriptionEn?: string;
}

/**
 * API响应基础接口
 */
export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message?: string;
  errorCode?: string;
}

/**
 * 分页响应接口
 */
export interface PagedResponse<T = any> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// 角色描述映射
export const ROLE_DESCRIPTIONS: Record<Role, string> = {
  ADMIN: '管理员',
  OPERATOR: '操作员',
  BROWSER: '浏览者',
  EXECUTOR: '执行机'
};

// 角色权限描述映射
export const ROLE_PERMISSIONS: Record<Role, string> = {
  ADMIN: '拥有所有权限',
  OPERATOR: '可以执行拨测任务相关的所有操作',
  BROWSER: '仅查看',
  EXECUTOR: '执行机注册使用'
};

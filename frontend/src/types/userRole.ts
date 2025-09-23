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


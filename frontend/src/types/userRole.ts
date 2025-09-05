// 用户角色类型定义

export type Role = 'ADMIN' | 'OPERATOR' | 'BROWSER' | 'EXECUTOR';

export interface UserRole {
  id: number;
  username: string;
  role: Role;
  createdTime: string;
  updatedTime: string;
}

export interface UserRoleFormData {
  username: string;
  role: Role;
}

export interface PermissionCheckRequest {
  username: string;
  resource: string;
}

export interface PermissionCheckResult {
  hasPermission: boolean;
  userRoles: string[];
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

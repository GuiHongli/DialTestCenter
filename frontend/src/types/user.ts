/**
 * 用户相关类型定义
 */

/**
 * 用户信息接口
 */
export interface User {
  id: number;
  username: string;
  password?: string; // 密码字段通常不在前端显示
  lastLoginTime?: string;
}

/**
 * 用户创建请求参数
 */
export interface UserCreateParams {
  username: string;
  password: string;
}

/**
 * 用户更新请求参数
 */
export interface UserUpdateParams {
  username?: string;
  password?: string;
}

/**
 * 用户搜索参数
 */
export interface UserSearchParams {
  username?: string;
}

/**
 * 密码验证请求参数
 */
export interface PasswordValidationParams {
  username: string;
  password: string;
}

/**
 * 密码验证响应
 */
export interface PasswordValidationResponse {
  valid: boolean;
  message: string;
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
  data: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

/**
 * 用户统计信息
 */
export interface UserStatistics {
  totalUsers: number;
  activeUsers: number;
  newUsersToday: number;
  lastLoginUsers: number;
}

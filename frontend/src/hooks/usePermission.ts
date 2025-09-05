import { Role } from '../types/userRole';

/**
 * 权限检查Hook
 */
export const usePermission = () => {
  
  /**
   * 检查用户是否有管理员角色
   * @param userRoles 用户角色列表
   * @returns 是否有管理员角色
   */
  const hasAdminRole = (userRoles: string[]): boolean => {
    return userRoles.includes('ADMIN');
  };
  
  /**
   * 检查用户是否可以管理用户
   * @param userRoles 用户角色列表
   * @returns 是否可以管理用户
   */
  const canManageUsers = (userRoles: string[]): boolean => {
    return hasAdminRole(userRoles);
  };
  
  /**
   * 检查用户是否可以执行拨测任务
   * @param userRoles 用户角色列表
   * @returns 是否可以执行拨测任务
   */
  const canExecuteTasks = (userRoles: string[]): boolean => {
    return userRoles.includes('ADMIN') || userRoles.includes('OPERATOR');
  };
  
  /**
   * 检查用户是否可以注册执行机
   * @param userRoles 用户角色列表
   * @returns 是否可以注册执行机
   */
  const canRegisterExecutor = (userRoles: string[]): boolean => {
    return userRoles.includes('ADMIN') || userRoles.includes('EXECUTOR');
  };
  
  /**
   * 检查用户是否有浏览权限
   * @param userRoles 用户角色列表
   * @returns 是否有浏览权限
   */
  const canBrowse = (userRoles: string[]): boolean => {
    return userRoles.length > 0; // 任何角色都可以浏览
  };
  
  /**
   * 检查用户是否有指定角色
   * @param userRoles 用户角色列表
   * @param role 要检查的角色
   * @returns 是否有指定角色
   */
  const hasRole = (userRoles: string[], role: Role): boolean => {
    return userRoles.includes(role);
  };
  
  /**
   * 检查用户是否有任一指定角色
   * @param userRoles 用户角色列表
   * @param roles 要检查的角色列表
   * @returns 是否有任一指定角色
   */
  const hasAnyRole = (userRoles: string[], roles: Role[]): boolean => {
    return roles.some(role => userRoles.includes(role));
  };
  
  /**
   * 检查用户是否有所有指定角色
   * @param userRoles 用户角色列表
   * @param roles 要检查的角色列表
   * @returns 是否有所有指定角色
   */
  const hasAllRoles = (userRoles: string[], roles: Role[]): boolean => {
    return roles.every(role => userRoles.includes(role));
  };
  
  return {
    hasAdminRole,
    canManageUsers,
    canExecuteTasks,
    canRegisterExecutor,
    canBrowse,
    hasRole,
    hasAnyRole,
    hasAllRoles
  };
};

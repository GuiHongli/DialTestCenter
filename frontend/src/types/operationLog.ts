/**
 * 操作记录相关类型定义
 */

/**
 * 操作记录实体
 */
export interface OperationLog {
  id: number
  username: string
  operationTime: string
  operationType: string
  target: string
  description?: string
}

/**
 * 操作记录分页响应
 */
export interface OperationLogPageResponse {
  content: OperationLog[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  numberOfElements: number
}

/**
 * 操作记录查询参数
 */
export interface OperationLogQueryParams {
  page?: number
  size?: number
  username?: string
  operationType?: string
  target?: string
  description?: string
  startTime?: string
  endTime?: string
}

/**
 * 操作记录创建参数
 */
export interface OperationLogCreateParams {
  username: string
  operationType: string
  target: string
  description?: string
}

/**
 * 操作记录统计信息
 */
export interface OperationLogStatistics {
  totalOperations: number
  operationsByType: Record<string, number>
  operationsByUser: Record<string, number>
  operationsByTarget: Record<string, number>
  recentOperations: OperationLog[]
}


/**
 * API响应基础结构
 */
export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
}

/**
 * API错误响应
 */
export interface ApiErrorResponse {
  success: false
  error: string
  message: string
}

/**
 * 操作类型枚举
 */
export enum OperationType {
  CREATE = 'CREATE',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE',
  LOGIN = 'LOGIN',
  LOGOUT = 'LOGOUT',
  VIEW = 'VIEW',
  EXPORT = 'EXPORT',
  IMPORT = 'IMPORT',
  UPLOAD = 'UPLOAD',
  DOWNLOAD = 'DOWNLOAD'
}

/**
 * 操作对象类型枚举
 */
export enum OperationTarget {
  USER = '用户管理',
  USER_ROLE = '角色管理',
  TEST_CASE_SET = '测试用例集',
  SOFTWARE_PACKAGE = '软件包管理',
  SYSTEM = '系统',
  LOGIN = '系统登录',
  LOGOUT = '系统登出'
}

/**
 * 操作记录表格列配置
 */
export interface OperationLogTableColumn {
  key: string
  title: string
  dataIndex: string
  width?: number
  sorter?: boolean
  filters?: Array<{ text: string; value: string }>
  render?: (value: any, record: OperationLog) => React.ReactNode
}

/**
 * 操作记录筛选器
 */
export interface OperationLogFilter {
  username?: string
  operationType?: string
  target?: string
  description?: string
  startTime?: string
  endTime?: string
}

/**
 * 操作记录排序配置
 */
export interface OperationLogSort {
  field: string
  order: 'ascend' | 'descend'
}

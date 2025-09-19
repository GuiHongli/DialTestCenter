/**
 * 操作记录服务
 * 提供操作记录相关的API调用功能
 */

import { 
  OperationLog, 
  OperationLogPageResponse, 
  OperationLogQueryParams, 
  OperationLogCreateParams,
  OperationLogStatistics,
  ApiResponse 
} from '../types/operationLog'

const API_BASE_URL = 'https://localhost:8087/dialingtest/api'

/**
 * 操作记录服务类
 */
export class OperationLogService {
  /**
   * 获取操作记录列表（分页，支持多条件筛选）
   */
  static async getOperationLogs(params: OperationLogQueryParams = {}): Promise<OperationLogPageResponse> {
    const { page = 0, size = 20, username, operationType, operationTarget, startTime, endTime } = params
    const url = new URL(`${API_BASE_URL}/operation-logs`)
    
    // 添加查询参数
    url.searchParams.append('page', page.toString())
    url.searchParams.append('size', size.toString())
    
    if (username) {
      url.searchParams.append('username', username)
    }
    if (operationType) {
      url.searchParams.append('operationType', operationType)
    }
    if (operationTarget) {
      url.searchParams.append('operationTarget', operationTarget)
    }
    if (startTime) {
      url.searchParams.append('startTime', startTime)
    }
    if (endTime) {
      url.searchParams.append('endTime', endTime)
    }
    
    try {
      const response = await fetch(url.toString())
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      const result: OperationLogPageResponse = await response.json()
      if (!result.success) {
        throw new Error(result.message || 'Failed to get operation logs')
      }
      
      return result
    } catch (error) {
      console.error('Error fetching operation logs:', error)
      throw error
    }
  }

  /**
   * 根据ID获取操作记录
   */
  static async getOperationLogById(id: number): Promise<OperationLog> {
    const url = `${API_BASE_URL}/operation-logs/${id}`
    
    try {
      const response = await fetch(url)
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      const result: ApiResponse<OperationLog> = await response.json()
      if (!result.success) {
        throw new Error(result.message || 'Failed to get operation log')
      }
      
      return result.data
    } catch (error) {
      console.error('Error fetching operation log:', error)
      throw error
    }
  }





  /**
   * 搜索操作记录
   */
  static async searchOperationLogs(params: OperationLogQueryParams = {}): Promise<OperationLogPageResponse> {
    const { page = 0, size = 20, username, operationType, operationTarget, startTime, endTime } = params
    const searchParams = new URLSearchParams()
    
    if (page !== undefined) searchParams.append('page', page.toString())
    if (size !== undefined) searchParams.append('size', size.toString())
    if (username) searchParams.append('username', username)
    if (operationType) searchParams.append('operationType', operationType)
    if (operationTarget) searchParams.append('operationTarget', operationTarget)
    if (startTime) searchParams.append('startTime', startTime)
    if (endTime) searchParams.append('endTime', endTime)
    
    const url = `${API_BASE_URL}/operation-logs?${searchParams.toString()}`
    
    try {
      const response = await fetch(url)
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      const result: ApiResponse<OperationLogPageResponse> = await response.json()
      if (!result.success) {
        throw new Error(result.message || 'Failed to search operation logs')
      }
      
      return result.data
    } catch (error) {
      console.error('Error searching operation logs:', error)
      throw error
    }
  }

  /**
   * 获取最近的操作记录
   */
  static async getRecentOperationLogs(limit: number = 10): Promise<OperationLog[]> {
    const url = `${API_BASE_URL}/operation-logs/recent?limit=${limit}`
    
    try {
      const response = await fetch(url)
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      const result: ApiResponse<OperationLog[]> = await response.json()
      if (!result.success) {
        throw new Error(result.message || 'Failed to get recent operation logs')
      }
      
      return result.data
    } catch (error) {
      console.error('Error fetching recent operation logs:', error)
      throw error
    }
  }

  /**
   * 导出操作记录
   */
  static async exportOperationLogs(params: OperationLogQueryParams = {}): Promise<Blob> {
    const { username, operationType, operationTarget, startTime, endTime } = params
    const url = new URL(`${API_BASE_URL}/operation-logs/export`)
    
    if (username) {
      url.searchParams.append('username', username)
    }
    if (operationType) {
      url.searchParams.append('operationType', operationType)
    }
    if (operationTarget) {
      url.searchParams.append('operationTarget', operationTarget)
    }
    if (startTime) {
      url.searchParams.append('startTime', startTime)
    }
    if (endTime) {
      url.searchParams.append('endTime', endTime)
    }
    
    try {
      const response = await fetch(url.toString())
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      return await response.blob()
    } catch (error) {
      console.error('Error exporting operation logs:', error)
      throw error
    }
  }

  /**
   * 记录操作日志
   */
  static async logOperation(params: OperationLogCreateParams): Promise<OperationLog> {
    const url = `${API_BASE_URL}/operation-logs`
    
    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(params),
      })
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      const result: ApiResponse<OperationLog> = await response.json()
      if (!result.success) {
        throw new Error(result.message || 'Failed to log operation')
      }
      
      return result.data
    } catch (error) {
      console.error('Error logging operation:', error)
      throw error
    }
  }


}

/**
 * 操作记录工具函数
 */
export class OperationLogUtils {
  /**
   * 格式化操作时间
   */
  static formatOperationTime(time: string): string {
    try {
      const date = new Date(time)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      })
    } catch (error) {
      return time
    }
  }

  /**
   * 获取操作类型标签颜色
   */
  static getOperationTypeColor(operationType: string): string {
    const colorMap: Record<string, string> = {
      CREATE: 'green',
      UPDATE: 'blue',
      DELETE: 'red',
      LOGIN: 'cyan',
      LOGOUT: 'orange',
      VIEW: 'default',
      EXPORT: 'purple',
      IMPORT: 'magenta',
      UPLOAD: 'lime',
      DOWNLOAD: 'geekblue',
    }
    return colorMap[operationType] || 'default'
  }

  /**
   * 获取操作类型显示文本
   */
  static getOperationTypeText(operationType: string): string {
    const textMap: Record<string, string> = {
      CREATE: '创建',
      UPDATE: '更新',
      DELETE: '删除',
      LOGIN: '登录',
      LOGOUT: '登出',
      VIEW: '查看',
      EXPORT: '导出',
      IMPORT: '导入',
      UPLOAD: '上传',
      DOWNLOAD: '下载',
    }
    return textMap[operationType] || operationType
  }

  /**
   * 截断长文本
   */
  static truncateText(text: string, maxLength: number = 50): string {
    if (!text) return ''
    if (text.length <= maxLength) return text
    return text.substring(0, maxLength) + '...'
  }
}

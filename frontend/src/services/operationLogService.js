/**
 * 操作记录服务
 * 提供操作记录相关的API调用功能
 */

import { createApiRequestConfig, handleApiResponse, handlePagedApiResponse } from '../utils/apiUtils.js';

const API_BASE_URL = '/dialingtest/api'

/**
 * 操作记录服务类
 */
export class OperationLogService {
  /**
   * 获取操作记录列表（分页，支持多条件筛选）
   */
  static async getOperationLogs(params = {}) {
    const { page = 0, size = 20, username, operationType, operationTarget, startTime, endTime } = params
    const searchParams = new URLSearchParams()
    
    // 添加查询参数
    searchParams.append('page', page.toString())
    searchParams.append('size', size.toString())
    
    if (username) {
      searchParams.append('username', username)
    }
    if (operationType) {
      searchParams.append('operationType', operationType)
    }
    if (operationTarget) {
      searchParams.append('operationTarget', operationTarget)
    }
    if (startTime) {
      searchParams.append('startTime', startTime)
    }
    if (endTime) {
      searchParams.append('endTime', endTime)
    }
    
    const url = `${API_BASE_URL}/operation-logs?${searchParams.toString()}`
    
    try {
      const response = await fetch(url, createApiRequestConfig('GET'))
      return handlePagedApiResponse(response)
    } catch (error) {
      console.error('Error fetching operation logs:', error)
      throw error
    }
  }

  /**
   * 获取操作记录统计信息
   */
  static async getStatistics() {
    const url = `${API_BASE_URL}/operation-logs/statistics`
    
    try {
      const response = await fetch(url, createApiRequestConfig('GET'))
      return handleApiResponse(response)
    } catch (error) {
      console.error('Error fetching operation log statistics:', error)
      throw error
    }
  }

  /**
   * 根据ID获取操作记录
   */
  static async getOperationLogById(id) {
    const url = `${API_BASE_URL}/operation-logs/${id}`
    
    try {
      const response = await fetch(url, createApiRequestConfig('GET'))
      return handleApiResponse(response)
    } catch (error) {
      console.error('Error fetching operation log:', error)
      throw error
    }
  }




  /**
   * 搜索操作记录
   */
  static async searchOperationLogs(params = {}) {
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
      
      const result = await response.json()
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
  static async getRecentOperationLogs(limit = 10) {
    const url = `${API_BASE_URL}/operation-logs/recent?limit=${limit}`
    
    try {
      const response = await fetch(url)
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      const result = await response.json()
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
  static async exportOperationLogs(params = {}) {
    const { username, operationType, operationTarget, startTime, endTime } = params
    const searchParams = new URLSearchParams()
    
    if (username) {
      searchParams.append('username', username)
    }
    if (operationType) {
      searchParams.append('operationType', operationType)
    }
    if (operationTarget) {
      searchParams.append('operationTarget', operationTarget)
    }
    if (startTime) {
      searchParams.append('startTime', startTime)
    }
    if (endTime) {
      searchParams.append('endTime', endTime)
    }
    
    const url = `${API_BASE_URL}/operation-logs/export?${searchParams.toString()}`
    
    try {
      const response = await fetch(url, createApiRequestConfig('GET'))
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
  static async logOperation(params) {
    const url = `${API_BASE_URL}/operation-logs`
    
    try {
      const response = await fetch(url, createApiRequestConfig('POST', params))
      return handleApiResponse(response)
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
  static formatOperationTime(time) {
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
  static getOperationTypeColor(operationType) {
    const colorMap = {
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
  static getOperationTypeText(operationType, language = 'zh') {
    const textMapZh = {
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
    
    const textMapEn = {
      CREATE: 'Create',
      UPDATE: 'Update',
      DELETE: 'Delete',
      LOGIN: 'Login',
      LOGOUT: 'Logout',
      VIEW: 'View',
      EXPORT: 'Export',
      IMPORT: 'Import',
      UPLOAD: 'Upload',
      DOWNLOAD: 'Download',
    }
    
    const textMap = language === 'en' ? textMapEn : textMapZh
    return textMap[operationType] || operationType
  }

  /**
   * 获取操作对象显示文本
   */
  static getOperationTargetText(operationTarget, language = 'zh') {
    const textMapZh = {
      USER: '执行机账号',
      USER_ROLE: '角色管理',
      TEST_CASE_SET: '测试用例集',
      SOFTWARE_PACKAGE: '应用',
      PREPROCESS_RULE_PACKAGE: '预处理规则包',
      SYSTEM: '系统',
      LOGIN: '系统登录',
      LOGOUT: '系统登出',
      // 兼容中文值
      '执行机账号': '执行机账号',
      '拨测用户': '执行机账号', // 兼容旧的拨测用户
      '用户管理': '执行机账号', // 兼容旧的用户管理
      '角色管理': '角色管理',
      '测试用例集': '测试用例集',
      '软件包管理': '应用', // 兼容旧的软件包管理
      '应用管理': '应用',
      '软件包': '应用',
      '预处理规则包': '预处理规则包',
      '系统': '系统',
      '系统登录': '系统登录',
      '系统登出': '系统登出',
    }
    
    const textMapEn = {
      USER: 'User Management',
      USER_ROLE: 'Role Management',
      TEST_CASE_SET: 'Test Case Set',
      SOFTWARE_PACKAGE: 'Application',
      PREPROCESS_RULE_PACKAGE: 'Preprocess Rule Package',
      SYSTEM: 'System',
      LOGIN: 'System Login',
      LOGOUT: 'System Logout',
    }
    
    const textMap = language === 'en' ? textMapEn : textMapZh
    return textMap[operationTarget] || operationTarget
  }

  /**
   * 截断长文本
   */
  static truncateText(text, maxLength = 50) {
    if (!text) return ''
    if (text.length <= maxLength) return text
    return text.substring(0, maxLength) + '...'
  }
}

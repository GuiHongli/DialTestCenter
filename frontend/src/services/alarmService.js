/**
 * 告警服务
 * 提供告警相关的API调用功能
 */

import { createApiRequestConfig, handleApiResponse, handlePagedApiResponse } from '../utils/apiUtils.js';

const API_BASE_URL = '/dialingtest/api'

/**
 * 告警服务类
 */
export class AlarmService {
  /**
   * 获取告警列表（分页，支持查询当前告警或所有告警）
   */
  static async getAlarms(params = {}) {
    const { page = 0, size = 20, currentOnly = false } = params
    const searchParams = new URLSearchParams()
    
    // 添加查询参数
    searchParams.append('page', page.toString())
    searchParams.append('size', size.toString())
    
    if (currentOnly) {
      searchParams.append('currentOnly', 'true')
    }
    
    const url = `${API_BASE_URL}/alarms?${searchParams.toString()}`
    
    try {
      const response = await fetch(url, createApiRequestConfig('GET'))
      return handlePagedApiResponse(response)
    } catch (error) {
      console.error('Error fetching alarms:', error)
      throw error
    }
  }

  /**
   * 创建告警
   */
  static async createAlarm(alarmData) {
    const url = `${API_BASE_URL}/alarms`
    
    try {
      const response = await fetch(url, createApiRequestConfig('POST', alarmData))
      return handleApiResponse(response)
    } catch (error) {
      console.error('Error creating alarm:', error)
      throw error
    }
  }

  /**
   * 结束告警（删除告警）
   */
  static async endAlarm(id) {
    const url = `${API_BASE_URL}/alarms/${id}`
    
    try {
      const response = await fetch(url, createApiRequestConfig('DELETE'))
      return handleApiResponse(response)
    } catch (error) {
      console.error('Error ending alarm:', error)
      throw error
    }
  }
}

/**
 * 告警工具函数
 */
export class AlarmUtils {
  /**
   * 格式化告警时间
   */
  static formatAlarmTime(time) {
    if (!time) {
      return '-'
    }
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
   * 获取告警等级标签颜色
   */
  static getAlarmLevelColor(level) {
    const colorMap = {
      Urgent: 'red',
      Important: 'orange',
      Minor: 'blue',
    }
    return colorMap[level] || 'default'
  }

  /**
   * 获取告警等级显示文本
   */
  static getAlarmLevelText(level, language = 'zh') {
    const textMapZh = {
      Urgent: '紧急',
      Important: '重要',
      Minor: '一般',
    }
    
    const textMapEn = {
      Urgent: 'Urgent',
      Important: 'Important',
      Minor: 'Minor',
    }
    
    const textMap = language === 'en' ? textMapEn : textMapZh
    return textMap[level] || level
  }
}


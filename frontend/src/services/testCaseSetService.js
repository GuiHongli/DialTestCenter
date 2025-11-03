import { createApiRequestConfig, createFileUploadConfig, handleApiResponse, handleApiResponseWithError, handlePagedApiResponse } from '../utils/apiUtils.js'

class TestCaseSetService {
  constructor() {
    this.baseUrl = '/dialingtest/api/test-case-sets'
  }

  /**
   * 获取用例集列表
   */
  async getTestCaseSets(page = 1, pageSize = 10) {
    const response = await fetch(`${this.baseUrl}?page=${page}&pageSize=${pageSize}`, createApiRequestConfig('GET', undefined, false))
    return handlePagedApiResponse(response)
  }

  /**
   * 获取用例集详情
   */
  async getTestCaseSet(id) {
    const response = await fetch(`${this.baseUrl}/${id}`, createApiRequestConfig('GET', undefined, false))
    return handleApiResponse(response)
  }

  /**
   * 上传用例集
   */
  async uploadTestCaseSet(uploadData) {
    const formData = new FormData()
    formData.append('file', uploadData.file)
    
    // Add form fields as separate FormData entries
    if (uploadData.description) {
      formData.append('description', uploadData.description)
    }
    
    // businessZh and businessEn are required fields
    if (uploadData.businessZh) {
      formData.append('businessZh', uploadData.businessZh)
    } else {
      throw new Error('Business type (Chinese) is required')
    }
    
    if (uploadData.businessEn) {
      formData.append('businessEn', uploadData.businessEn)
    } else {
      throw new Error('Business type (English) is required')
    }
    
    formData.append('overwrite', 'false')

    const response = await fetch(`${this.baseUrl}`, createFileUploadConfig(formData))
    return handleApiResponseWithError(response)
  }

  /**
   * 覆盖上传用例集
   */
  async uploadTestCaseSetWithOverwrite(uploadData) {
    const formData = new FormData()
    formData.append('file', uploadData.file)
    
    // Add form fields as separate FormData entries
    if (uploadData.description) {
      formData.append('description', uploadData.description)
    }
    
    // businessZh and businessEn are required fields
    if (uploadData.businessZh) {
      formData.append('businessZh', uploadData.businessZh)
    } else {
      throw new Error('Business type (Chinese) is required')
    }
    
    if (uploadData.businessEn) {
      formData.append('businessEn', uploadData.businessEn)
    } else {
      throw new Error('Business type (English) is required')
    }
    
    // Always set overwrite to 'true' for overwrite upload
    formData.append('overwrite', 'true')
    
    console.log('Uploading with overwrite=true:', {
      description: uploadData.description,
      businessZh: uploadData.businessZh,
      businessEn: uploadData.businessEn,
      overwrite: 'true'
    })

    const response = await fetch(`${this.baseUrl}`, createFileUploadConfig(formData))
    return handleApiResponseWithError(response)
  }

  /**
   * 下载用例集
   */
  async downloadTestCaseSet(id) {
    const response = await fetch(`${this.baseUrl}/${id}/download`, createApiRequestConfig('GET', undefined, true))
    if (!response.ok) {
      throw new Error('下载用例集失败')
    }
    return response.blob()
  }

  /**
   * 删除用例集
   */
  async deleteTestCaseSet(id) {
    const response = await fetch(`${this.baseUrl}/${id}`, createApiRequestConfig('DELETE'))
    if (!response.ok) {
      throw new Error('删除用例集失败')
    }
  }

  /**
   * 更新用例集信息
   */
  async updateTestCaseSet(id, data) {
    const response = await fetch(`${this.baseUrl}/${id}`, createApiRequestConfig('PUT', data))
    return handleApiResponse(response)
  }


  /**
   * 验证用例集文件
   */
  validateTestCaseSetFile(file) {
    const fileName = file.name.toLowerCase()
    
    // 检查文件类型 - 只支持 .zip
    const supportedExtensions = ['.zip']
    const isValidExtension = supportedExtensions.some(ext => fileName.endsWith(ext))
    
    if (!isValidExtension) {
      return { valid: false, message: '只支持 ZIP 格式文件' }
    }

    // 检查文件大小 (100MB)
    const maxSize = 100 * 1024 * 1024 // 100MB
    if (file.size > maxSize) {
      return { valid: false, message: '文件大小不能超过100MB' }
    }

    // 检查文件命名格式: 用例集名称_用例集版本.zip
    const fileExtension = '.zip'
    const nameWithoutExt = file.name.replace(fileExtension, '')
    const lastUnderscoreIndex = nameWithoutExt.lastIndexOf('_')
    
    if (lastUnderscoreIndex === -1) {
      return { valid: false, message: `文件名格式错误，应为：用例集名称_版本号${fileExtension}` }
    }

    return { valid: true }
  }

  /**
   * 解析文件名获取用例集名称和版本
   */
  parseFileName(fileName) {
    const fileExtension = '.zip'
    const nameWithoutExt = fileName.replace(fileExtension, '')
    const lastUnderscoreIndex = nameWithoutExt.lastIndexOf('_')
    
    if (lastUnderscoreIndex === -1) {
      return null
    }

    const name = nameWithoutExt.substring(0, lastUnderscoreIndex)
    const version = nameWithoutExt.substring(lastUnderscoreIndex + 1)

    return { name, version }
  }

  /**
   * 获取用例集的测试用例列表
   */
  async getTestCases(testCaseSetId, page = 1, pageSize = 10) {
    const response = await fetch(`${this.baseUrl}/${testCaseSetId}/test-cases?page=${page}&pageSize=${pageSize}`, createApiRequestConfig('GET', undefined, false))
    return handlePagedApiResponse(response)
  }

  /**
   * 触发用例集校验任务
   */
  async triggerValidation(testCaseSetId) {
    const response = await fetch(
      `${this.baseUrl}/${testCaseSetId}/validation`,
      createApiRequestConfig('POST', undefined, true)
    )
    // 注意：POST /validation 返回 202 Accepted，所以需要特殊处理
    // 即使状态码是 202，后端也会返回 JSON 响应
    const result = await handleApiResponseWithError(response)
    return result
  }

  /**
   * 获取用例集校验结果
   */
  async getValidationResult(testCaseSetId, forceRefresh = false) {
    const url = forceRefresh 
      ? `${this.baseUrl}/${testCaseSetId}/validation?forceRefresh=true`
      : `${this.baseUrl}/${testCaseSetId}/validation`
    const response = await fetch(url, createApiRequestConfig('GET', undefined, false))
    return handleApiResponseWithError(response)
  }

  /**
   * 导出用例集校验结果Excel
   */
  async exportValidationResult(testCaseSetId) {
    const response = await fetch(`${this.baseUrl}/${testCaseSetId}/validation/export`, createApiRequestConfig('GET', undefined, true))
    if (!response.ok) {
      throw new Error('导出校验结果失败')
    }
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    const contentDisposition = response.headers.get('Content-Disposition')
    let filename = `校验结果_${testCaseSetId}.xlsx`
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
      if (filenameMatch && filenameMatch[1]) {
        filename = filenameMatch[1].replace(/['"]/g, '')
      }
    }
    link.setAttribute('download', filename)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  }
}

export default new TestCaseSetService()

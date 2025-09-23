import { TestCaseSet, TestCaseSetFormData, TestCaseSetUpdateData, TestCaseSetListResponse, TestCaseSetUploadData, TestCaseSetUploadResponse, TestCase, TestCaseListResponse, MissingScriptsResponse, ApiResponse, PagedResponse } from '../types/testCaseSet'

class TestCaseSetService {
  private baseUrl = '/dialingtest/api/test-case-sets'

  /**
   * 获取用例集列表
   */
  async getTestCaseSets(page: number = 1, pageSize: number = 10): Promise<PagedResponse<TestCaseSet>> {
    const response = await fetch(`${this.baseUrl}?page=${page}&pageSize=${pageSize}`)
    if (!response.ok) {
      throw new Error('获取用例集列表失败')
    }
    
    const result: ApiResponse<PagedResponse<TestCaseSet>> = await response.json()
    if (!result.success) {
      throw new Error(result.message || 'Failed to get test case sets')
    }
    
    return result.data!
  }

  /**
   * 获取用例集详情
   */
  async getTestCaseSet(id: number): Promise<TestCaseSet> {
    const response = await fetch(`${this.baseUrl}/${id}`)
    if (!response.ok) {
      throw new Error('获取用例集详情失败')
    }
    
    const result: ApiResponse<TestCaseSet> = await response.json()
    if (!result.success) {
      throw new Error(result.message || 'Failed to get test case set')
    }
    
    return result.data!
  }

  /**
   * 上传用例集
   */
  async uploadTestCaseSet(uploadData: TestCaseSetUploadData): Promise<TestCaseSetUploadResponse> {
    const formData = new FormData()
    formData.append('file', uploadData.file)
    if (uploadData.description) {
      formData.append('description', uploadData.description)
    }
    if (uploadData.businessZh) {
      formData.append('businessZh', uploadData.businessZh)
    }

    const response = await fetch(`${this.baseUrl}`, {
      method: 'POST',
      body: formData,
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || '上传用例集失败')
    }

    return response.json()
  }

  /**
   * 覆盖上传用例集
   */
  async uploadTestCaseSetWithOverwrite(uploadData: TestCaseSetUploadData): Promise<TestCaseSetUploadResponse> {
    const formData = new FormData()
    formData.append('file', uploadData.file)
    if (uploadData.description) {
      formData.append('description', uploadData.description)
    }
    if (uploadData.businessZh) {
      formData.append('businessZh', uploadData.businessZh)
    }
    formData.append('overwrite', 'true')

    const response = await fetch(`${this.baseUrl}`, {
      method: 'POST',
      body: formData,
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || '上传用例集失败')
    }

    return response.json()
  }

  /**
   * 下载用例集
   */
  async downloadTestCaseSet(id: number): Promise<Blob> {
    const response = await fetch(`${this.baseUrl}/${id}/download`)
    if (!response.ok) {
      throw new Error('下载用例集失败')
    }
    return response.blob()
  }

  /**
   * 删除用例集
   */
  async deleteTestCaseSet(id: number): Promise<void> {
    const response = await fetch(`${this.baseUrl}/${id}`, {
      method: 'DELETE',
    })
    if (!response.ok) {
      throw new Error('删除用例集失败')
    }
  }

  /**
   * 更新用例集信息
   */
  async updateTestCaseSet(id: number, data: TestCaseSetUpdateData): Promise<TestCaseSet> {
    const response = await fetch(`${this.baseUrl}/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    })

    if (!response.ok) {
      throw new Error('更新用例集失败')
    }

    return response.json()
  }


  /**
   * 验证用例集文件
   */
  validateTestCaseSetFile(file: File): { valid: boolean; message?: string } {
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
  parseFileName(fileName: string): { name: string; version: string } | null {
    const lowerFileName = fileName.toLowerCase()
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
  async getTestCases(testCaseSetId: number, page: number = 1, pageSize: number = 10): Promise<TestCaseListResponse> {
    const response = await fetch(`${this.baseUrl}/${testCaseSetId}/test-cases?page=${page}&pageSize=${pageSize}`)
    if (!response.ok) {
      throw new Error('获取测试用例列表失败')
    }
    return response.json()
  }

  /**
   * 获取用例集中没有脚本的测试用例列表
   */
  async getMissingScripts(testCaseSetId: number): Promise<MissingScriptsResponse> {
    const response = await fetch(`${this.baseUrl}/${testCaseSetId}/missing-scripts`)
    if (!response.ok) {
      throw new Error('获取缺失脚本信息失败')
    }
    return response.json()
  }
}

export default new TestCaseSetService()

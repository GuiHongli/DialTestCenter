import { TestCaseSet, TestCaseSetFormData, TestCaseSetListResponse, TestCaseSetUploadData, TestCaseSetUploadResponse } from '../types/testCaseSet'

class TestCaseSetService {
  private baseUrl = '/api/test-case-sets'

  /**
   * 获取用例集列表
   */
  async getTestCaseSets(page: number = 1, pageSize: number = 10): Promise<TestCaseSetListResponse> {
    const response = await fetch(`${this.baseUrl}?page=${page}&pageSize=${pageSize}`)
    if (!response.ok) {
      throw new Error('获取用例集列表失败')
    }
    return response.json()
  }

  /**
   * 获取用例集详情
   */
  async getTestCaseSet(id: number): Promise<TestCaseSet> {
    const response = await fetch(`${this.baseUrl}/${id}`)
    if (!response.ok) {
      throw new Error('获取用例集详情失败')
    }
    return response.json()
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

    const response = await fetch(`${this.baseUrl}/upload`, {
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
  async updateTestCaseSet(id: number, data: TestCaseSetFormData): Promise<TestCaseSet> {
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
    // 检查文件类型
    if (!file.name.toLowerCase().endsWith('.zip')) {
      return { valid: false, message: '只支持ZIP格式文件' }
    }

    // 检查文件大小 (100MB)
    const maxSize = 100 * 1024 * 1024 // 100MB
    if (file.size > maxSize) {
      return { valid: false, message: '文件大小不能超过100MB' }
    }

    // 检查文件命名格式: 用例集名称_用例集版本.zip
    const fileName = file.name.replace('.zip', '')
    const lastUnderscoreIndex = fileName.lastIndexOf('_')
    if (lastUnderscoreIndex === -1) {
      return { valid: false, message: '文件名格式错误，应为：用例集名称_版本号.zip' }
    }

    return { valid: true }
  }

  /**
   * 解析文件名获取用例集名称和版本
   */
  parseFileName(fileName: string): { name: string; version: string } | null {
    const nameWithoutExt = fileName.replace('.zip', '')
    const lastUnderscoreIndex = nameWithoutExt.lastIndexOf('_')
    
    if (lastUnderscoreIndex === -1) {
      return null
    }

    const name = nameWithoutExt.substring(0, lastUnderscoreIndex)
    const version = nameWithoutExt.substring(lastUnderscoreIndex + 1)

    return { name, version }
  }
}

export default new TestCaseSetService()

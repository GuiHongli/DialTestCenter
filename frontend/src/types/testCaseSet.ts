// 用例集管理相关类型定义

export interface TestCaseSet {
  id: number
  name: string
  version: string
  zipPath: string
  creator: string
  createdTime: string
  updatedTime: string
  fileSize: number
  description?: string
}

export interface TestCaseSetFormData {
  name: string
  version: string
  description?: string
}

export interface TestCaseSetUploadData {
  file: File
  description?: string
}


// API响应类型
export interface TestCaseSetListResponse {
  data: TestCaseSet[]
  total: number
  page: number
  pageSize: number
}

export interface TestCaseSetUploadResponse {
  success: boolean
  message: string
  data?: TestCaseSet
}

// 用例集管理相关类型定义

export interface TestCaseSet {
  id: number
  name: string
  version: string
  fileContent?: string // Base64编码的文件内容
  fileSize: number
  description?: string
  sha256?: string
  businessZh?: string // 业务类型（中文）
  businessEn?: string // 业务类型（英文）
  missingScriptsCount?: number // 缺失脚本数量
}

export interface TestCaseSetFormData {
  name: string
  version: string
  description?: string
  businessZh?: string
  businessEn?: string
}

export interface TestCaseSetUpdateData {
  description?: string
  businessZh?: string
  businessEn?: string
}

export interface TestCaseSetUploadData {
  file: File
  description?: string
  businessZh?: string
}


// API响应类型
export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message?: string;
  errorCode?: string;
}

export interface PagedResponse<T = any> {
  data: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

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

// 测试用例相关类型定义
export interface TestCase {
  id: number
  testCaseSetId: number
  caseName: string
  caseNumber: string
  networkTopology?: string
  businessCategory?: string
  appName?: string
  testSteps?: string
  expectedResult?: string
  scriptExists: boolean
  createdTime: string
  updatedTime: string
}

export interface TestCaseListResponse {
  data: TestCase[]
  total: number
  page: number
  pageSize: number
}

export interface MissingScriptsResponse {
  data: TestCase[]
  count: number
}

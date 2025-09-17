// 用例集管理相关类型定义

export interface TestCaseSet {
  id: number
  name: string
  version: string
  zipPath: string // 文件路径，支持 .zip 和 .tar.gz 格式
  creator: string
  createdTime: string
  updatedTime: string
  fileSize: number
  description?: string
  fileFormat?: 'zip' | 'tar.gz' // 可选的文件格式字段
  business?: string // 业务类型
  missingScriptsCount?: number // 缺失脚本数量
}

export interface TestCaseSetFormData {
  name: string
  version: string
  description?: string
}

export interface TestCaseSetUploadData {
  file: File
  description?: string
  business?: string
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

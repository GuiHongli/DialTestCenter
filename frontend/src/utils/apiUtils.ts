/**
 * API响应处理工具
 * 统一处理后端API响应格式
 */

/**
 * API响应基础接口
 */
export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message?: string;
  errorCode?: string;
}

/**
 * 分页响应接口
 */
export interface PagedResponse<T = any> {
  data: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

/**
 * 处理API响应的通用函数
 * @param response fetch响应对象
 * @returns 解析后的数据
 */
export async function handleApiResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  
  const result: ApiResponse<T> = await response.json();
  if (!result.success) {
    throw new Error(result.message || 'API request failed');
  }
  
  return result.data!;
}

/**
 * 处理API响应的通用函数（不抛出错误，返回完整响应）
 * @param response fetch响应对象
 * @returns 完整的API响应对象
 */
export async function handleApiResponseWithError<T>(response: Response): Promise<ApiResponse<T>> {
  const result: ApiResponse<T> = await response.json();
  return result;
}

/**
 * 处理分页API响应的通用函数
 * @param response fetch响应对象
 * @returns 解析后的分页数据
 */
export async function handlePagedApiResponse<T>(response: Response): Promise<PagedResponse<T>> {
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  
  const result: ApiResponse<PagedResponse<T>> = await response.json();
  if (!result.success) {
    throw new Error(result.message || 'API request failed');
  }
  
  return result.data!;
}

/**
 * 从cookie中获取xUsername
 * @returns xUsername值，如果不存在则返回默认值
 */
export function getXUsernameFromCookie(): string {
  const cookies = document.cookie.split(';');
  for (const cookie of cookies) {
    const [name, value] = cookie.trim().split('=');
    if (name === 'xUsername') {
      return decodeURIComponent(value);
    }
  }
  // 如果cookie中不存在，返回默认值
  return 'admin';
}

/**
 * 创建API请求的通用配置
 * @param method HTTP方法
 * @param body 请求体（可选）
 * @param includeXUsername 是否包含X-Username头（默认为true）
 * @returns fetch配置对象
 */
export function createApiRequestConfig(method: string = 'GET', body?: any, includeXUsername: boolean = true): RequestInit {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };
  
  // 自动添加X-Username头
  if (includeXUsername) {
    headers['X-Username'] = getXUsernameFromCookie();
  }
  
  const config: RequestInit = {
    method,
    headers,
  };
  
  if (body) {
    config.body = JSON.stringify(body);
  }
  
  return config;
}

/**
 * 创建文件上传请求的配置
 * @param body FormData对象
 * @param includeXUsername 是否包含X-Username头（默认为true）
 * @returns fetch配置对象
 */
export function createFileUploadConfig(body: FormData, includeXUsername: boolean = true): RequestInit {
  const headers: Record<string, string> = {};
  
  // 自动添加X-Username头
  if (includeXUsername) {
    headers['X-Username'] = getXUsernameFromCookie();
  }
  
  return {
    method: 'POST',
    headers,
    body,
  };
}

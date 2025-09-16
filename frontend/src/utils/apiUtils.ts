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
  timestamp?: string;
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
 * 创建API请求的通用配置
 * @param method HTTP方法
 * @param body 请求体（可选）
 * @returns fetch配置对象
 */
export function createApiRequestConfig(method: string = 'GET', body?: any): RequestInit {
  const config: RequestInit = {
    method,
    headers: {
      'Content-Type': 'application/json',
    },
  };
  
  if (body) {
    config.body = JSON.stringify(body);
  }
  
  return config;
}

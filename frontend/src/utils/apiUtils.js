/**
 * API响应处理工具
 * 统一处理后端API响应格式
 */

/**
 * 处理API响应的通用函数
 * @param response fetch响应对象
 * @returns 解析后的数据
 */
export async function handleApiResponse(response) {
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  
  const result = await response.json();
  if (!result.success) {
    throw new Error(result.message || 'API request failed');
  }
  
  return result.data;
}

/**
 * 处理API响应的通用函数（不抛出错误，返回完整响应）
 * @param response fetch响应对象
 * @returns 完整的API响应对象
 */
export async function handleApiResponseWithError(response) {
  const result = await response.json();
  return result;
}

/**
 * 处理分页API响应的通用函数
 * @param response fetch响应对象
 * @returns 解析后的分页数据
 */
export async function handlePagedApiResponse(response) {
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  
  const result = await response.json();
  if (!result.success) {
    throw new Error(result.message || 'API request failed');
  }
  
  return result.data;
}

/**
 * 从cookie中获取xUsername
 * @returns xUsername值，如果不存在则返回默认值
 */
export function getXUsernameFromCookie() {
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
export function createApiRequestConfig(method = 'GET', body, includeXUsername = true) {
  const headers = {
    'Content-Type': 'application/json',
  };
  
  // 自动添加X-Username头
  if (includeXUsername) {
    headers['X-Username'] = getXUsernameFromCookie();
  }
  
  const config = {
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
export function createFileUploadConfig(body, includeXUsername = true) {
  const headers = {};
  
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

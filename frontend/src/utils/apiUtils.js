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

// 缓存用户名
let cachedUsername = null;
let usernamePromise = null;

/**
 * 从API获取当前用户名
 * @returns 用户名
 */
export async function getXUsername() {
  // 如果已缓存，直接返回
  if (cachedUsername) {
    return cachedUsername;
  }
  
  // 如果正在请求，返回同一个 promise
  if (usernamePromise) {
    return usernamePromise;
  }
  
  // 发起新请求
  usernamePromise = fetch('/dialingtest/api/userName', {
    headers: {
      'Content-Type': 'application/json'
    }
  })
    .then(response => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    })
    .then(result => {
      // 直接返回 userName 字段
      if (result && result.userName) {
        cachedUsername = result.userName;
        return cachedUsername;
      } else {
        // 如果获取失败，返回 null
        cachedUsername = null;
        return cachedUsername;
      }
    })
    .catch(error => {
      console.error('Failed to get username from API:', error);
      // 失败时返回 null
      cachedUsername = null;
      return cachedUsername;
    })
    .finally(() => {
      // 请求完成后清除 promise
      usernamePromise = null;
    });
  
  return usernamePromise;
}

/**
 * 初始化用户名（应用启动时调用）
 */
export async function initUsername() {
  if (!cachedUsername) {
    await getXUsername();
  }
}

/**
 * 清除缓存用户名（用于重新获取）
 */
export function clearUsernameCache() {
  cachedUsername = null;
  usernamePromise = null;
}

/**
 * 创建API请求的通用配置（使用缓存的用户名）
 * @param method HTTP方法
 * @param body 请求体（可选）
 * @param includeXUsername 是否包含X-Username头（默认为true）
 * @returns fetch配置对象
 */
export function createApiRequestConfig(method = 'GET', body, includeXUsername = true) {
  const headers = {
    'Content-Type': 'application/json',
  };
  
  // 自动添加X-Username头（使用缓存的值）
  if (includeXUsername && cachedUsername) {
    headers['X-Username'] = cachedUsername;
  }
  
  // 自动添加 CSRF Token
  // 如果 sessionStorage 中没有，尝试从 cookie 或其他地方获取，或使用默认值
  let csrfToken = sessionStorage.getItem('X-CSRF-TOKEN');
  if (!csrfToken) {
    // 尝试从 cookie 中获取
    const cookies = document.cookie.split(';');
    for (let cookie of cookies) {
      const [name, value] = cookie.trim().split('=');
      if (name === 'X-CSRF-TOKEN' || name === 'csrf-token') {
        csrfToken = decodeURIComponent(value);
        sessionStorage.setItem('X-CSRF-TOKEN', csrfToken);
        break;
      }
    }
  }
  
  // 如果仍然没有 token，使用默认值（用于开发环境）
  if (!csrfToken) {
    csrfToken = 'development-token';
    sessionStorage.setItem('X-CSRF-TOKEN', csrfToken);
  }
  
  headers['X-Csrf-Token'] = csrfToken;
  
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
  if (includeXUsername && cachedUsername) {
    headers['X-Username'] = cachedUsername;
  }
  
  // 自动添加 CSRF Token
  // 如果 sessionStorage 中没有，尝试从 cookie 或其他地方获取，或使用默认值
  let csrfToken = sessionStorage.getItem('X-CSRF-TOKEN');
  if (!csrfToken) {
    // 尝试从 cookie 中获取
    const cookies = document.cookie.split(';');
    for (let cookie of cookies) {
      const [name, value] = cookie.trim().split('=');
      if (name === 'X-CSRF-TOKEN' || name === 'csrf-token') {
        csrfToken = decodeURIComponent(value);
        sessionStorage.setItem('X-CSRF-TOKEN', csrfToken);
        break;
      }
    }
  }
  
  // 如果仍然没有 token，使用默认值（用于开发环境）
  if (!csrfToken) {
    csrfToken = 'development-token';
    sessionStorage.setItem('X-CSRF-TOKEN', csrfToken);
  }
  
  headers['X-Csrf-Token'] = csrfToken;
  
  return {
    method: 'POST',
    headers,
    body,
  };
}

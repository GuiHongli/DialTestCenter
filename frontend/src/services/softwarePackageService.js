/**
 * 软件包管理服务
 */

import { createApiRequestConfig, createFileUploadConfig } from '../utils/apiUtils.js';

const API_BASE_URL = '/dialingtest/api/software-packages';
const UPLOAD_API_URL = '/dialingtest/api/software-packages';

// 注意：现在使用 createApiRequestConfig 函数，它会自动从 cookie 中获取用户名

/**
 * 获取软件包列表
 */
export const getSoftwarePackages = async (params = {}) => {
  const searchParams = new URLSearchParams();
  
  if (params.page) searchParams.append('page', params.page.toString());
  if (params.pageSize) searchParams.append('pageSize', params.pageSize.toString());
  if (params.softwareName) searchParams.append('keyword', params.softwareName);

  const response = await fetch(`${API_BASE_URL}?${searchParams.toString()}`, createApiRequestConfig('GET', undefined, false));
  
  if (!response.ok) {
    throw new Error(`Failed to fetch software packages: ${response.statusText}`);
  }
  
  const result = await response.json();
  
  // 转换API响应格式
  return {
    data: result.data?.data || [],
    total: result.data?.totalElements || 0,
    page: result.data?.page || 1,
    pageSize: result.data?.pageSize || 10,
    totalPages: Math.ceil((result.data?.totalElements || 0) / (result.data?.pageSize || 10))
  };
};

/**
 * 获取软件包详情
 */
export const getSoftwarePackage = async (id) => {
  const response = await fetch(`${API_BASE_URL}/${id}`, createApiRequestConfig('GET', undefined, false));
  
  if (!response.ok) {
    throw new Error(`Failed to fetch software package: ${response.statusText}`);
  }
  
  const result = await response.json();
  return result.data;
};

/**
 * 上传单个软件包
 */
export const uploadSoftwarePackage = async (file, description, overwrite = false) => {
  const formData = new FormData();
  formData.append('file', file);
  if (description) {
    formData.append('description', description);
  }
  formData.append('overwrite', overwrite.toString());

  // 调试信息
  console.log('Uploading file:', file.name, 'size:', file.size);
  console.log('Description:', description);
  console.log('FormData entries:');
  for (let [key, value] of formData.entries()) {
    console.log(key, value);
  }

  const response = await fetch(`${UPLOAD_API_URL}`, createFileUploadConfig(formData));

  const result = await response.json();

  if (!response.ok) {
    // 解析后端返回的具体错误消息
    const errorMessage = result.message || `Failed to upload software package: ${response.statusText}`;
    throw new Error(errorMessage);
  }

  return {
    success: result.success,
    message: result.message,
    data: result.data ? {
      id: result.data.id,
      softwareName: result.data.softwareName,
      description: result.data.description,
      fileSize: result.data.fileSize,
      fileSHA256: result.data.fileSHA256
    } : null
  };
};

/**
 * 上传ZIP包（批量上传）
 */
export const uploadZipPackage = async (file, overwrite = false, description) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('overwrite', overwrite.toString());
  if (description) {
    formData.append('description', description);
  }

  // 调试信息
  console.log('Uploading ZIP file:', file.name, 'size:', file.size, 'overwrite:', overwrite);
  console.log('FormData entries:');
  for (let [key, value] of formData.entries()) {
    console.log(key, value);
  }

  const response = await fetch(`${UPLOAD_API_URL}`, createFileUploadConfig(formData));

  const result = await response.json();

  if (!response.ok) {
    // 解析后端返回的具体错误消息
    const errorMessage = result.message || `Failed to upload ZIP package: ${response.statusText}`;
    throw new Error(errorMessage);
  }

  // 处理ZIP包上传的响应格式
  return {
    success: result.success,
    message: result.message,
    count: result.message?.includes('个软件包') ? parseInt(result.message.match(/\d+/)?.[0] || '0') : 0
  };
};

/**
 * 下载软件包（单个或批量）
 */
export const downloadSoftwarePackage = async (ids, zipFileName) => {
  const response = await fetch(`${API_BASE_URL}/download`, createApiRequestConfig('POST', {
    packageIds: ids,
    zipFileName: zipFileName || 'software_packages_batch'
  }));
  
  if (!response.ok) {
    throw new Error(`Failed to download software package: ${response.statusText}`);
  }

  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  
  // 根据下载类型设置文件名
  const fileName = ids.length === 1 ? 'package' : `${zipFileName || 'packages'}.zip`;
  link.download = fileName;
  
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
};

/**
 * 删除软件包
 */
export const deleteSoftwarePackage = async (id) => {
  const response = await fetch(`${API_BASE_URL}/${id}`, createApiRequestConfig('DELETE'));

  if (!response.ok) {
    throw new Error(`Failed to delete software package: ${response.statusText}`);
  }
};

/**
 * 更新软件包信息（仅更新描述）
 */
export const updateSoftwarePackage = async (id, params) => {
  const response = await fetch(`${API_BASE_URL}/${id}`, createApiRequestConfig('PUT', {
    description: params.description
  }));

  if (!response.ok) {
    throw new Error(`Failed to update software package: ${response.statusText}`);
  }

  const result = await response.json();
  return result.data;
};

/**
 * 获取软件包统计信息（模拟实现）
 */
export const getSoftwarePackageStatistics = async () => {
  try {
    const response = await getSoftwarePackages({ page: 1, pageSize: 100 });
    const packages = response.data;
    
    let android = 0;
    let ios = 0;
    
    packages.forEach(pkg => {
      if (pkg.softwareName.toLowerCase().endsWith('.apk')) {
        android++;
      } else if (pkg.softwareName.toLowerCase().endsWith('.ipa')) {
        ios++;
      }
    });
    
    return {
      success: true,
      data: {
        android,
        ios,
        total: packages.length
      }
    };
  } catch (error) {
    return {
      success: false,
      data: {
        android: 0,
        ios: 0,
        total: 0
      }
    };
  }
};

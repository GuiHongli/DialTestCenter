/**
 * 软件包管理服务
 */

import { 
  SoftwarePackage, 
  SoftwarePackageListResponse, 
  SoftwarePackageUploadResponse,
  SoftwarePackageStatisticsResponse,
  SoftwarePackageListParams,
  SoftwarePackageUpdateParams
} from '../types/softwarePackage';

const API_BASE_URL = '/dialingtest/api/software-packages';

/**
 * 获取软件包列表
 */
export const getSoftwarePackages = async (params: SoftwarePackageListParams = {}): Promise<SoftwarePackageListResponse> => {
  const searchParams = new URLSearchParams();
  
  if (params.page) searchParams.append('page', params.page.toString());
  if (params.pageSize) searchParams.append('pageSize', params.pageSize.toString());
  if (params.platform) searchParams.append('platform', params.platform);
  if (params.creator) searchParams.append('creator', params.creator);
  if (params.softwareName) searchParams.append('softwareName', params.softwareName);

  const response = await fetch(`${API_BASE_URL}?${searchParams.toString()}`);
  
  if (!response.ok) {
    throw new Error(`Failed to fetch software packages: ${response.statusText}`);
  }
  
  return response.json();
};

/**
 * 获取软件包详情
 */
export const getSoftwarePackage = async (id: number): Promise<SoftwarePackage> => {
  const response = await fetch(`${API_BASE_URL}/${id}`);
  
  if (!response.ok) {
    throw new Error(`Failed to fetch software package: ${response.statusText}`);
  }
  
  return response.json();
};

/**
 * 上传单个软件包
 */
export const uploadSoftwarePackage = async (file: File, description?: string): Promise<SoftwarePackageUploadResponse> => {
  const formData = new FormData();
  formData.append('file', file);
  if (description) {
    formData.append('description', description);
  }

  const response = await fetch(`${API_BASE_URL}/upload`, {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    throw new Error(`Failed to upload software package: ${response.statusText}`);
  }

  return response.json();
};

/**
 * 上传ZIP包（批量上传）
 */
export const uploadZipPackage = async (file: File): Promise<SoftwarePackageUploadResponse> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch(`${API_BASE_URL}/upload-zip`, {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    throw new Error(`Failed to upload ZIP package: ${response.statusText}`);
  }

  return response.json();
};

/**
 * 下载软件包
 */
export const downloadSoftwarePackage = async (id: number, softwareName: string): Promise<void> => {
  const response = await fetch(`${API_BASE_URL}/${id}/download`);
  
  if (!response.ok) {
    throw new Error(`Failed to download software package: ${response.statusText}`);
  }

  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = softwareName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
};

/**
 * 删除软件包
 */
export const deleteSoftwarePackage = async (id: number): Promise<void> => {
  const response = await fetch(`${API_BASE_URL}/${id}`, {
    method: 'DELETE',
  });

  if (!response.ok) {
    throw new Error(`Failed to delete software package: ${response.statusText}`);
  }
};

/**
 * 更新软件包信息
 */
export const updateSoftwarePackage = async (id: number, params: SoftwarePackageUpdateParams): Promise<SoftwarePackage> => {
  const response = await fetch(`${API_BASE_URL}/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(params),
  });

  if (!response.ok) {
    throw new Error(`Failed to update software package: ${response.statusText}`);
  }

  return response.json();
};

/**
 * 获取软件包统计信息
 */
export const getSoftwarePackageStatistics = async (): Promise<SoftwarePackageStatisticsResponse> => {
  const response = await fetch(`${API_BASE_URL}/statistics`);
  
  if (!response.ok) {
    throw new Error(`Failed to fetch statistics: ${response.statusText}`);
  }
  
  return response.json();
};

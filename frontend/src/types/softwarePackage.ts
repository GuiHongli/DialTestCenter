/**
 * 软件包相关类型定义
 */

export interface SoftwarePackage {
  id: number;
  softwareName: string; // 完整文件名（带后缀）
  fileFormat: 'apk' | 'ipa';
  platform: 'android' | 'ios';
  creator: string;
  fileSize: number;
  sha512: string;
  description?: string;
  createdTime: string;
  updatedTime: string;
}

export interface SoftwarePackageListResponse {
  data: SoftwarePackage[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export interface SoftwarePackageUploadResponse {
  success: boolean;
  message: string;
  data?: SoftwarePackage;
  count?: number;
}

export interface SoftwarePackageStatistics {
  android: number;
  ios: number;
  total: number;
}

export interface SoftwarePackageStatisticsResponse {
  success: boolean;
  data: SoftwarePackageStatistics;
}

export interface SoftwarePackageUploadParams {
  file: File;
  description?: string;
}

export interface SoftwarePackageUpdateParams {
  softwareName: string;
  description?: string;
}

export interface SoftwarePackageListParams {
  page?: number;
  pageSize?: number;
  platform?: 'android' | 'ios';
  creator?: string;
  softwareName?: string;
}

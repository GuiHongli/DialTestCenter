/**
 * 预处理规则管理服务
 */

import { createApiRequestConfig, createFileUploadConfig } from '../utils/apiUtils';

export interface PreprocessRulePackage {
  id: number;
  packageName: string;
  businessZh: string;
  businessEn: string;
  fileSize: number;
  description?: string;
}

export interface PreprocessRule {
  id: number;
  ruleName: string;
  businessZh: string;
  businessEn: string;
  category: string;
  appName: string;
  content: string;
  isCustom: boolean;
  packageId: number;
}

export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data: T;
}

export interface PagedApiResponse<T> {
  success: boolean;
  message: string;
  data: {
    page: number;
    pageSize: number;
    total: number;
    data: T[];
  };
}

const API_BASE_URL = '/dialingtest/api';

export const preprocessRuleService = {
  /**
   * 获取ZIP包列表
   */
  async getPreprocessRulePackages(params: {
    page: number;
    pageSize: number;
    keyword?: string;
    businessZh?: string;
  }): Promise<PagedApiResponse<PreprocessRulePackage>> {
    const queryParams = new URLSearchParams({
      page: params.page.toString(),
      pageSize: params.pageSize.toString(),
    });
    
    if (params.keyword) {
      queryParams.append('keyword', params.keyword);
    }
    if (params.businessZh) {
      queryParams.append('businessZh', params.businessZh);
    }

    const response = await fetch(`${API_BASE_URL}/preprocess-rule-packages?${queryParams}`, 
      createApiRequestConfig('GET', undefined, false));
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return response.json();
  },

  /**
   * 上传预处理规则ZIP包
   */
  async uploadPreprocessRulePackage(formData: FormData): Promise<ApiResponse> {
    const response = await fetch(`${API_BASE_URL}/preprocess-rule-packages/upload`, 
      createFileUploadConfig(formData));
    
    // 无论成功还是失败，都尝试解析JSON响应
    // 这样可以获取到错误消息
    const jsonResponse = await response.json();
    
    // 如果HTTP状态码不是2xx，但我们已经有了JSON响应，直接返回
    // 这样前端可以检查response.success和response.message
    return jsonResponse;
  },

  /**
   * 下载预处理规则ZIP包
   */
  async downloadPreprocessRulePackage(packageId: number): Promise<Blob> {
    const response = await fetch(`${API_BASE_URL}/preprocess-rule-packages/${packageId}/download`, 
      createApiRequestConfig('GET', undefined, true));
    
    if (!response.ok) {
      throw new Error('Download failed');
    }
    
    return response.blob();
  },

  /**
   * 删除预处理规则ZIP包
   */
  async deletePreprocessRulePackage(packageId: number): Promise<ApiResponse> {
    const response = await fetch(`${API_BASE_URL}/preprocess-rule-packages/${packageId}`, 
      createApiRequestConfig('DELETE', undefined, true));
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return response.json();
  },

  /**
   * 获取预处理规则列表
   */
  async getPreprocessRules(params: {
    page: number;
    pageSize: number;
    keyword?: string;
    businessZh?: string;
    category?: string;
    appName?: string;
    ruleName?: string;
  }): Promise<PagedApiResponse<PreprocessRule>> {
    const queryParams = new URLSearchParams({
      page: params.page.toString(),
      pageSize: params.pageSize.toString(),
    });
    
    if (params.keyword) {
      queryParams.append('keyword', params.keyword);
    }
    if (params.businessZh) {
      queryParams.append('businessZh', params.businessZh);
    }
    if (params.category) {
      queryParams.append('category', params.category);
    }
    if (params.appName) {
      queryParams.append('appName', params.appName);
    }
    if (params.ruleName) {
      queryParams.append('ruleName', params.ruleName);
    }

    const response = await fetch(`${API_BASE_URL}/preprocess-rules?${queryParams}`, 
      createApiRequestConfig('GET', undefined, false));
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return response.json();
  },

  /**
   * 获取业务类型列表
   */
  async getBusinessTypes(): Promise<ApiResponse<string[]>> {
    const response = await fetch(`${API_BASE_URL}/preprocess-rules/business-types`, 
      createApiRequestConfig('GET', undefined, false));
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return response.json();
  },

  /**
   * 根据业务类型获取分类列表
   */
  async getCategoriesByBusiness(businessZh: string): Promise<ApiResponse<string[]>> {
    const queryParams = new URLSearchParams({ businessZh });
    const response = await fetch(`${API_BASE_URL}/preprocess-rules/categories?${queryParams}`, 
      createApiRequestConfig('GET', undefined, false));
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return response.json();
  },

  /**
   * 根据业务类型和分类获取应用名称列表
   */
  async getAppNamesByBusinessAndCategory(businessZh: string, category: string): Promise<ApiResponse<string[]>> {
    const queryParams = new URLSearchParams({ businessZh, category });
    const response = await fetch(`${API_BASE_URL}/preprocess-rules/app-names?${queryParams}`, 
      createApiRequestConfig('GET', undefined, false));
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return response.json();
  },

  /**
   * 根据业务类型、分类和应用获取规则名称列表
   */
  async getRuleNamesByBusinessAndCategoryAndApp(businessZh: string, category: string, appName: string): Promise<ApiResponse<string[]>> {
    const queryParams = new URLSearchParams({ businessZh, category, appName });
    const response = await fetch(`${API_BASE_URL}/preprocess-rules/rule-names?${queryParams}`, 
      createApiRequestConfig('GET', undefined, false));
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return response.json();
  },

  /**
   * 获取筛选选项
   */
  async getFilterOptions(): Promise<ApiResponse<{
    businessTypes: string[];
    categories: string[];
    appNames: string[];
    ruleNames: string[];
  }>> {
    const response = await fetch(`${API_BASE_URL}/preprocess-rules/filter-options`, 
      createApiRequestConfig('GET', undefined, false));
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return response.json();
  }
};
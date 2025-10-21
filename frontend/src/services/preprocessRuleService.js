/**
 * 预处理规则管理服务
 */

import { createApiRequestConfig, createFileUploadConfig, handleApiResponse, handlePagedApiResponse } from '../utils/apiUtils.js';

const API_BASE_URL = '/dialingtest/api';

export const preprocessRuleService = {
  /**
   * 获取ZIP包列表
   */
  async getPreprocessRulePackages(params) {
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
    
    return handlePagedApiResponse(response);
  },

  /**
   * 上传预处理规则ZIP包
   */
  async uploadPreprocessRulePackage(formData) {
    const response = await fetch(`${API_BASE_URL}/preprocess-rule-packages/upload`, 
      createFileUploadConfig(formData));
    
    return handleApiResponseWithError(response);
  },

  /**
   * 下载预处理规则ZIP包
   */
  async downloadPreprocessRulePackage(packageId) {
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
  async deletePreprocessRulePackage(packageId) {
    const response = await fetch(`${API_BASE_URL}/preprocess-rule-packages/${packageId}`, 
      createApiRequestConfig('DELETE', undefined, true));
    
    return handleApiResponseWithError(response);
  },

  /**
   * 获取预处理规则列表
   */
  async getPreprocessRules(params) {
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
    
    return handlePagedApiResponse(response);
  },

  /**
   * 获取业务类型列表
   */
  async getBusinessTypes() {
    const response = await fetch(`${API_BASE_URL}/preprocess-rules/business-types`, 
      createApiRequestConfig('GET', undefined, false));
    
    return handleApiResponse(response);
  },

  /**
   * 根据业务类型获取分类列表
   */
  async getCategoriesByBusiness(businessZh) {
    const queryParams = new URLSearchParams({ businessZh });
    const response = await fetch(`${API_BASE_URL}/preprocess-rules/categories?${queryParams}`, 
      createApiRequestConfig('GET', undefined, false));
    
    return handleApiResponse(response);
  },

  /**
   * 根据业务类型和分类获取应用名称列表
   */
  async getAppNamesByBusinessAndCategory(businessZh, category) {
    const queryParams = new URLSearchParams({ businessZh, category });
    const response = await fetch(`${API_BASE_URL}/preprocess-rules/app-names?${queryParams}`, 
      createApiRequestConfig('GET', undefined, false));
    
    return handleApiResponse(response);
  },

  /**
   * 根据业务类型、分类和应用获取规则名称列表
   */
  async getRuleNamesByBusinessAndCategoryAndApp(businessZh, category, appName) {
    const queryParams = new URLSearchParams({ businessZh, category, appName });
    const response = await fetch(`${API_BASE_URL}/preprocess-rules/rule-names?${queryParams}`, 
      createApiRequestConfig('GET', undefined, false));
    
    return handleApiResponse(response);
  },

  /**
   * 获取筛选选项
   */
  async getFilterOptions() {
    const response = await fetch(`${API_BASE_URL}/preprocess-rules/filter-options`, 
      createApiRequestConfig('GET', undefined, false));
    
    return handleApiResponse(response);
  }
};

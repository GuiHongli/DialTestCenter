/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.api.PreprocessRulePackagesApi;
import com.huawei.cloududn.dialingtest.api.PreprocessRulesApi;
import com.huawei.cloududn.dialingtest.model.*;
import com.huawei.cloududn.dialingtest.service.PreprocessRuleService;
import com.huawei.cloududn.dialingtest.util.OperationLogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 预处理规则管理控制器
 * 
 * @author g00940940
 * @since 2025-01-27
 */
@RestController
@RequestMapping("/api")
public class PreprocessRuleController implements PreprocessRulePackagesApi, PreprocessRulesApi {
    
    @Autowired
    private PreprocessRuleService preprocessRuleService;
    
    @Autowired
    private OperationLogUtil operationLogUtil;
    
    // ==================== ZIP包管理接口 ====================
    
    @Override
    public ResponseEntity<PreprocessRulePackageListResponse> preprocessRulePackagesGet(
            Integer page, Integer pageSize, String businessZh, String keyword) {
        try {
            // 设置默认值
            if (page == null) page = 1;
            if (pageSize == null) pageSize = 10;
            
            PreprocessRulePackageListResponse response = preprocessRuleService.getPreprocessRulePackages(
                page, pageSize, businessZh, keyword);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            PreprocessRulePackageListResponse response = new PreprocessRulePackageListResponse();
            response.setSuccess(false);
            response.setMessage("获取ZIP包列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @Override
    public ResponseEntity<SuccessResponse> preprocessRulePackagesIdDelete(Long id, String xUsername) {
        try {
            preprocessRuleService.deletePreprocessRulePackage(id, xUsername);
            
            SuccessResponse response = new SuccessResponse();
            response.setSuccess(true);
            response.setMessage("删除ZIP包成功");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            SuccessResponse response = new SuccessResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            SuccessResponse response = new SuccessResponse();
            response.setSuccess(false);
            response.setMessage("删除ZIP包失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @Override
    public ResponseEntity<Resource> preprocessRulePackagesIdDownloadGet(Long id) {
        try {
            byte[] fileContent = preprocessRuleService.downloadPreprocessRulePackage(id);
            
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=preprocess_rule_package_" + id + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(fileContent.length)
                .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // ==================== 规则管理接口 ====================
    
    @Override
    public ResponseEntity<PreprocessRuleListResponse> preprocessRulesGet(
            Integer page, Integer pageSize, String businessZh, String category, 
            String appName, String ruleName, String keyword) {
        try {
            // 设置默认值
            if (page == null) page = 1;
            if (pageSize == null) pageSize = 10;
            
            PreprocessRuleListResponse response = preprocessRuleService.getPreprocessRules(
                page, pageSize, businessZh, category, appName, ruleName, keyword);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            PreprocessRuleListResponse response = new PreprocessRuleListResponse();
            response.setSuccess(false);
            response.setMessage("获取预处理规则列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // ==================== 筛选选项接口 ====================
    
    @Override
    public ResponseEntity<BusinessTypeListResponse> preprocessRulesBusinessTypesGet() {
        try {
            BusinessTypeListResponse response = preprocessRuleService.getAllBusinessTypes();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            BusinessTypeListResponse response = new BusinessTypeListResponse();
            response.setSuccess(false);
            response.setMessage("获取业务类型列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @Override
    public ResponseEntity<CategoryListResponse> preprocessRulesCategoriesGet(String businessZh) {
        try {
            CategoryListResponse response = preprocessRuleService.getCategoriesByBusiness(businessZh);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CategoryListResponse response = new CategoryListResponse();
            response.setSuccess(false);
            response.setMessage("获取分类列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @Override
    public ResponseEntity<AppNameListResponse> preprocessRulesAppNamesGet(String businessZh, String category) {
        try {
            AppNameListResponse response = preprocessRuleService.getAppNamesByBusinessAndCategory(businessZh, category);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AppNameListResponse response = new AppNameListResponse();
            response.setSuccess(false);
            response.setMessage("获取应用名称列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @Override
    public ResponseEntity<RuleNameListResponse> preprocessRulesRuleNamesGet(
            String businessZh, String category, String appName) {
        try {
            RuleNameListResponse response = preprocessRuleService.getRuleNamesByBusinessAndCategoryAndApp(
                businessZh, category, appName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RuleNameListResponse response = new RuleNameListResponse();
            response.setSuccess(false);
            response.setMessage("获取规则名称列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @Override
    public ResponseEntity<FilterOptionsResponse> preprocessRulesFilterOptionsGet() {
        try {
            FilterOptionsResponse response = preprocessRuleService.getFilterOptions();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            FilterOptionsResponse response = new FilterOptionsResponse();
            response.setSuccess(false);
            response.setMessage("获取筛选选项失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}


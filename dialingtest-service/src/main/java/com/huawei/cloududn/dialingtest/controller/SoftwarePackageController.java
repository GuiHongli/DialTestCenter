/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.api.SoftwarePackagesApi;
import com.huawei.cloududn.dialingtest.model.*;
import com.huawei.cloududn.dialingtest.service.SoftwarePackagesService;
import com.huawei.cloududn.dialingtest.service.UserRoleService;
import com.huawei.cloududn.dialingtest.util.OperationLogUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 软件包管理控制器
 * 
 * @author g00940940
 * @since 2025-09-29
 */
@RestController
@RequestMapping("/api")
public class SoftwarePackageController implements SoftwarePackagesApi {
    
    private static final Logger logger = LoggerFactory.getLogger(SoftwarePackageController.class);
    
    @Autowired
    private SoftwarePackagesService softwarePackageService;
    
    @Autowired
    private UserRoleService userRoleService;
    
    @Autowired
    private OperationLogUtil operationLogUtil;
    
    @Override
    public ResponseEntity<SoftwarePackageListResponse> getSoftwarePackages(Integer page, Integer pageSize, String keyword) {
        try {
            // 设置默认值
            if (page == null) page = 1;
            if (pageSize == null) pageSize = 10;
            
            SoftwarePackageListResponseData data = softwarePackageService.getSoftwarePackageList(page, pageSize, keyword);
            
            SoftwarePackageListResponse response = new SoftwarePackageListResponse();
            response.setSuccess(true);
            response.setMessage("获取软件包列表成功");
            response.setData(data);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            SoftwarePackageListResponse errorResponse = new SoftwarePackageListResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("获取软件包列表失败: " + e.getMessage());
            errorResponse.setData(null);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @Override
    public ResponseEntity<SoftwarePackagePayload> getSoftwarePackageById(Long id) {
        try {
            SoftwarePackageInfo packageInfo = softwarePackageService.getSoftwarePackageById(id);
            if (packageInfo == null) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("软件包不存在");
                errorResponse.setErrorCode("PACKAGE_NOT_FOUND");
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            SoftwarePackagePayload response = new SoftwarePackagePayload();
            response.setSuccess(true);
            response.setMessage("获取软件包详情成功");
            response.setData(packageInfo);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("获取软件包详情失败: " + e.getMessage());
            errorResponse.setErrorCode("GET_PACKAGE_ERROR");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @Override
    public ResponseEntity<SoftwarePackagePayload> updateSoftwarePackage(String xUsername, Long id, UpdateSoftwarePackageBody body) {
        try {
            // 处理用户名，如果为空则使用默认值
            String operatorUsername = (xUsername != null && !xUsername.trim().isEmpty()) ? xUsername : "admin";
            
            // 检查权限 - ADMIN和OPERATOR有更新权限
            List<String> userRoles = userRoleService.getUserRolesByUsername(operatorUsername);
            if (!userRoles.contains("ADMIN") && !userRoles.contains("OPERATOR")) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("权限不足，只有管理员和操作员可以更新软件包信息");
                errorResponse.setErrorCode("INSUFFICIENT_PERMISSION");
                
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }
            
            // 获取更新前的软件包信息
            SoftwarePackageInfo oldPackageInfo = softwarePackageService.getSoftwarePackageById(id);
            if (oldPackageInfo == null) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("软件包不存在");
                errorResponse.setErrorCode("PACKAGE_NOT_FOUND");
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            SoftwarePackageInfo newPackageInfo = softwarePackageService.updateSoftwarePackage(id, body.getDescription());
            if (newPackageInfo == null) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("软件包不存在");
                errorResponse.setErrorCode("PACKAGE_NOT_FOUND");
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // 记录操作日志
            operationLogUtil.logSoftwarePackageUpdate(operatorUsername, oldPackageInfo, newPackageInfo);
            
            SoftwarePackagePayload response = new SoftwarePackagePayload();
            response.setSuccess(true);
            response.setMessage("更新软件包成功");
            response.setData(newPackageInfo);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("更新软件包失败: " + e.getMessage());
            errorResponse.setErrorCode("UPDATE_PACKAGE_ERROR");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @Override
    public ResponseEntity<SuccessResponse> deleteSoftwarePackage(Long id, String xUsername) {
        try {
            // 处理用户名，如果为空则使用默认值
            String operatorUsername = (xUsername != null && !xUsername.trim().isEmpty()) ? xUsername : "admin";
            
            // 检查权限 - ADMIN和OPERATOR有删除权限
            List<String> userRoles = userRoleService.getUserRolesByUsername(operatorUsername);
            if (!userRoles.contains("ADMIN") && !userRoles.contains("OPERATOR")) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("权限不足，只有管理员和操作员可以删除软件包");
                errorResponse.setErrorCode("INSUFFICIENT_PERMISSION");
                
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }
            
            // 获取删除前的软件包信息
            SoftwarePackageInfo packageInfo = softwarePackageService.getSoftwarePackageById(id);
            if (packageInfo == null) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("软件包不存在");
                errorResponse.setErrorCode("PACKAGE_NOT_FOUND");
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // 检查是否被引用
            if (softwarePackageService.isReferencedByTestCaseSet(id)) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("软件包被测试用例集引用，无法删除");
                errorResponse.setErrorCode("PACKAGE_REFERENCED");
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            boolean deleted = softwarePackageService.deleteSoftwarePackage(id);
            if (!deleted) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("删除软件包失败");
                errorResponse.setErrorCode("DELETE_PACKAGE_ERROR");
                
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
            
            // 记录操作日志
            operationLogUtil.logSoftwarePackageDelete(operatorUsername, packageInfo);
            
            SuccessResponse response = new SuccessResponse();
            response.setSuccess(true);
            response.setMessage("删除软件包成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("删除软件包失败: " + e.getMessage());
            errorResponse.setErrorCode("DELETE_PACKAGE_ERROR");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @Override
    public ResponseEntity<Resource> downloadSoftwarePackages(String xUsername, BatchDownloadRequest body) {
        try {
            // 检查权限 - ADMIN和OPERATOR有下载权限
            List<String> userRoles = userRoleService.getUserRolesByUsername(xUsername);
            if (!userRoles.contains("ADMIN") && !userRoles.contains("OPERATOR")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            
            List<Long> packageIds = body.getPackageIds();
            if (packageIds == null || packageIds.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            
            Resource resource = softwarePackageService.downloadSoftwarePackages(packageIds, body.getZipFileName());
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            
            if (packageIds.size() == 1) {
                // 单个文件下载，使用原文件名
                String fileName = softwarePackageService.getSoftwarePackageNameById(packageIds.get(0));
                if (fileName != null) {
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
                    headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
                }
            } else {
                // 批量下载，使用ZIP格式
                String zipFileName = body.getZipFileName() != null ? body.getZipFileName() : "software_packages_batch";
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + ".zip\"");
                headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");
            }
            
            // 记录操作日志 - 简化版本，暂时跳过详细日志记录
            String logMessage = packageIds.size() == 1 ? 
                "下载软件包ID: " + packageIds.get(0) :
                "批量下载软件包: " + packageIds.toString();
            logger.info("Software package download by user: {}, message: {}", xUsername, logMessage);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            logger.error("Software package download failed for user: {}", xUsername, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

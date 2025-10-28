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
            SoftwarePackageInfo packageInfo = checkPackageExists(id);
            if (packageInfo == null) {
                return createSoftwarePackagePayloadError("软件包不存在", HttpStatus.NOT_FOUND);
            }
            
            SoftwarePackagePayload response = new SoftwarePackagePayload();
            response.setSuccess(true);
            response.setMessage("获取软件包详情成功");
            response.setData(packageInfo);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return createSoftwarePackagePayloadError("获取软件包详情失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    public ResponseEntity<SoftwarePackagePayload> updateSoftwarePackage(String xUsername, Long id, UpdateSoftwarePackageBody body) {
        try {
            // 验证用户名
            ResponseEntity<SoftwarePackagePayload> usernameValidation = validateUsername(xUsername);
            if (usernameValidation != null) {
                return usernameValidation;
            }
            
            // 检查权限
            ResponseEntity<SoftwarePackagePayload> permissionCheck = checkPermission(xUsername, "更新软件包信息");
            if (permissionCheck != null) {
                return permissionCheck;
            }
            
            // 检查软件包是否存在
            SoftwarePackageInfo oldPackageInfo = checkPackageExists(id);
            if (oldPackageInfo == null) {
                return createSoftwarePackagePayloadError("软件包不存在", HttpStatus.NOT_FOUND);
            }
            
            SoftwarePackageInfo newPackageInfo = softwarePackageService.updateSoftwarePackage(id, body.getDescription());
            if (newPackageInfo == null) {
                return createSoftwarePackagePayloadError("软件包不存在", HttpStatus.NOT_FOUND);
            }
            
            // 记录操作日志
            operationLogUtil.logSoftwarePackageUpdate(xUsername, oldPackageInfo, newPackageInfo);
            
            SoftwarePackagePayload response = new SoftwarePackagePayload();
            response.setSuccess(true);
            response.setMessage("更新软件包成功");
            response.setData(newPackageInfo);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return createSoftwarePackagePayloadError("更新软件包失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    public ResponseEntity<SuccessResponse> deleteSoftwarePackage(Long id, String xUsername) {
        try {
            // 验证用户名
            ResponseEntity<SuccessResponse> usernameValidation = validateUsernameForSuccessResponse(xUsername);
            if (usernameValidation != null) {
                return usernameValidation;
            }
            
            // 检查权限
            ResponseEntity<SuccessResponse> permissionCheck = checkPermissionForSuccessResponse(xUsername, "删除软件包");
            if (permissionCheck != null) {
                return permissionCheck;
            }
            
            // 检查软件包是否存在
            SoftwarePackageInfo packageInfo = checkPackageExists(id);
            if (packageInfo == null) {
                return createSuccessResponseError("软件包不存在", HttpStatus.NOT_FOUND);
            }
            
            // 检查是否被引用
            if (softwarePackageService.isReferencedByTestCaseSet(id)) {
                return createSuccessResponseError("软件包被测试用例集引用，无法删除", HttpStatus.BAD_REQUEST);
            }
            
            boolean deleted = softwarePackageService.deleteSoftwarePackage(id);
            if (!deleted) {
                return createSuccessResponseError("删除软件包失败", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
            // 记录操作日志
            operationLogUtil.logSoftwarePackageDelete(xUsername, packageInfo);
            
            SuccessResponse response = new SuccessResponse();
            response.setSuccess(true);
            response.setMessage("删除软件包成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return createSuccessResponseError("删除软件包失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    public ResponseEntity<Resource> downloadSoftwarePackages(String xUsername, BatchDownloadRequest body) {
        try {
            // 验证用户名
            if (xUsername == null || xUsername.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            
            // 检查权限 - ADMIN和OPERATOR有下载权限
            List<String> userRoles = userRoleService.getUserRolesByUsername(xUsername);
            if (!userRoles.contains("ADMIN") && !userRoles.contains("OPERATOR")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            
            List<Long> packageIds = body.getPackageIds();
            if (packageIds == null || packageIds.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            
            // 仅实现单个软件包下载（参考用例集下载逻辑）
            Long packageId = packageIds.get(0);
            SoftwarePackageInfo packageInfo = softwarePackageService.getSoftwarePackageById(packageId);
            if (packageInfo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            
            byte[] fileContent = softwarePackageService.getSoftwarePackageFileContent(packageId);
            if (fileContent == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + packageInfo.getSoftwareName() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            
            // 记录操作日志 - 简化版本
            logger.info("Software package download by user: {}, message: {}", xUsername, "下载软件包ID: " + packageId);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(fileContent.length)
                    .body(resource);
        } catch (Exception e) {
            logger.error("Software package download failed for user: {}", xUsername, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 创建SoftwarePackagePayload错误响应
     *
     * @param message 错误消息
     * @param status HTTP状态码
     * @return ResponseEntity<SoftwarePackagePayload>
     */
    private ResponseEntity<SoftwarePackagePayload> createSoftwarePackagePayloadError(String message, HttpStatus status) {
        SoftwarePackagePayload errorResponse = new SoftwarePackagePayload();
        errorResponse.setSuccess(false);
        errorResponse.setMessage(message);
        errorResponse.setData(null);
        
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    /**
     * 创建SuccessResponse错误响应
     *
     * @param message 错误消息
     * @param status HTTP状态码
     * @return ResponseEntity<SuccessResponse>
     */
    private ResponseEntity<SuccessResponse> createSuccessResponseError(String message, HttpStatus status) {
        SuccessResponse errorResponse = new SuccessResponse();
        errorResponse.setSuccess(false);
        errorResponse.setMessage(message);
        
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    /**
     * 检查用户权限
     *
     * @param username 用户名
     * @param operation 操作类型（用于错误消息）
     * @return 权限检查结果，null表示有权限，否则返回错误响应
     */
    private ResponseEntity<SoftwarePackagePayload> checkPermission(String username, String operation) {
        List<String> userRoles = userRoleService.getUserRolesByUsername(username);
        if (!userRoles.contains("ADMIN") && !userRoles.contains("OPERATOR")) {
            return createSoftwarePackagePayloadError(
                "权限不足，只有管理员和操作员可以" + operation, 
                HttpStatus.FORBIDDEN
            );
        }
        return null;
    }
    
    /**
     * 检查用户权限（SuccessResponse版本）
     *
     * @param username 用户名
     * @param operation 操作类型（用于错误消息）
     * @return 权限检查结果，null表示有权限，否则返回错误响应
     */
    private ResponseEntity<SuccessResponse> checkPermissionForSuccessResponse(String username, String operation) {
        List<String> userRoles = userRoleService.getUserRolesByUsername(username);
        if (!userRoles.contains("ADMIN") && !userRoles.contains("OPERATOR")) {
            return createSuccessResponseError(
                "权限不足，只有管理员和操作员可以" + operation, 
                HttpStatus.FORBIDDEN
            );
        }
        return null;
    }
    
    /**
     * 检查软件包是否存在
     *
     * @param id 软件包ID
     * @return 软件包信息，null表示不存在
     */
    private SoftwarePackageInfo checkPackageExists(Long id) {
        return softwarePackageService.getSoftwarePackageById(id);
    }
    
    /**
     * 验证用户名，如果为空则返回错误响应
     *
     * @param username 原始用户名
     * @return 验证结果，null表示验证通过，否则返回错误响应
     */
    private ResponseEntity<SoftwarePackagePayload> validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return createSoftwarePackagePayloadError("用户名不能为空", HttpStatus.BAD_REQUEST);
        }
        return null;
    }
    
    /**
     * 验证用户名，如果为空则返回错误响应（SuccessResponse版本）
     *
     * @param username 原始用户名
     * @return 验证结果，null表示验证通过，否则返回错误响应
     */
    private ResponseEntity<SuccessResponse> validateUsernameForSuccessResponse(String username) {
        if (username == null || username.trim().isEmpty()) {
            return createSuccessResponseError("用户名不能为空", HttpStatus.BAD_REQUEST);
        }
        return null;
    }
}

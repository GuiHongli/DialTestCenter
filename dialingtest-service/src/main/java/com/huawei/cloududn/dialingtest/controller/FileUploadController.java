/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.entity.SoftwarePackage;
import com.huawei.cloududn.dialingtest.model.TestCaseSet;
import com.huawei.cloududn.dialingtest.model.TestCaseSetUploadResponse;
import com.huawei.cloududn.dialingtest.service.SoftwarePackagesService;
import com.huawei.cloududn.dialingtest.service.TestCaseSetService;
import com.huawei.cloududn.dialingtest.service.TestCaseValidationService;
import com.huawei.cloududn.dialingtest.service.UserRoleService;
import com.huawei.cloududn.dialingtest.service.PreprocessRuleService;
import com.huawei.cloududn.dialingtest.util.OperationLogUtil;
import com.huawei.cloududn.dialingtest.util.PermissionValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * 文件上传控制器，提供文件上传的REST API接口
 * 使用HttpServletRequest处理文件上传请求
 *
 * @author g00940940
 * @since 2025-01-27
 */
@RestController
@RequestMapping("/api")
public class FileUploadController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    
    @Autowired
    private TestCaseSetService testCaseSetService;
    
    @Autowired
    private SoftwarePackagesService softwarePackagesService;
    
    @Autowired
    private UserRoleService userRoleService;
    
    @Autowired
    private PreprocessRuleService preprocessRuleService;
    
    @Autowired
    private OperationLogUtil operationLogUtil;
    
    @Autowired
    private TestCaseValidationService testCaseValidationService;
    
    @Autowired
    private PermissionValidator permissionValidator;
    
    /**
     * 上传用例集文件
     *
     * @param request HTTP请求对象
     * @param description 用例集描述信息（可选）
     * @param businessZh 业务类型中文（必填）
     * @param businessEn 业务类型英文（必填）
     * @param overwrite 是否覆盖已存在的用例集（可选，默认为false）
     * @param xUsername 操作用户名（必需，Header中的X-Username）
     * @return 上传结果响应
     */
    @PostMapping("/test-case-sets")
    public ResponseEntity<TestCaseSetUploadResponse> uploadTestCaseSet(
            HttpServletRequest request,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "businessZh", required = true) String businessZh,
            @RequestParam(value = "businessEn", required = true) String businessEn,
            @RequestParam(value = "overwrite", required = false, defaultValue = "false") String overwrite,
            @RequestHeader(value = "X-Username", required = true) String xUsername) {
        logger.info("Received test case set upload request from user: {}", xUsername);
        
        try {
            // 权限验证
            PermissionValidator.PermissionValidationResult permissionResult = permissionValidator.checkAdminOrOperator(xUsername, "上传用例集");
            if (!permissionResult.isValid()) {
                TestCaseSetUploadResponse response = new TestCaseSetUploadResponse();
                response.setSuccess(false);
                response.setMessage(permissionResult.getErrorMessage());
                HttpStatus status = permissionResult.getErrorMessage().contains("未提供用户名") 
                    ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
                return ResponseEntity.status(status).body(response);
            }
            
            // 获取上传文件
            MultipartFile file = getUploadFile(request, "test case set");
            if (file == null) {
                return createTestCaseSetErrorResponse("未提供上传文件", HttpStatus.BAD_REQUEST);
            }
            
            // Validate required business type fields
            if (businessZh == null || businessZh.trim().isEmpty()) {
                return createTestCaseSetErrorResponse("Business type (Chinese) is required", HttpStatus.BAD_REQUEST);
            }
            if (businessEn == null || businessEn.trim().isEmpty()) {
                return createTestCaseSetErrorResponse("Business type (English) is required", HttpStatus.BAD_REQUEST);
            }
            
            // 设置默认值
            boolean isOverwrite = "true".equalsIgnoreCase(overwrite);
            
            logger.info("Processing file upload: {} by user: {}, overwrite parameter: {} (resolved to: {})", 
                       file.getOriginalFilename(), xUsername, overwrite, isOverwrite);
            
            // 调用服务层处理文件上传
            TestCaseSet testCaseSet = testCaseSetService.uploadTestCaseSet(
                file, description, businessZh, businessEn, isOverwrite, xUsername);
            
            TestCaseSetUploadResponse response = new TestCaseSetUploadResponse();
            response.setSuccess(true);
            response.setMessage(isOverwrite ? "覆盖更新用例集成功" : "上传用例集成功");
            response.setData(testCaseSet);
            
            logger.info("File upload completed successfully: {} v{}", 
                       testCaseSet.getName(), testCaseSet.getVersion());
            
            // 上传完成后自动触发校验（异步执行，不阻塞响应）
            try {
                testCaseValidationService.triggerValidation(testCaseSet.getId());
                logger.info("Validation task triggered automatically after upload for test case set: {} (ID: {})", 
                           testCaseSet.getName(), testCaseSet.getId());
            } catch (Exception e) {
                // 校验触发失败不影响上传成功的响应，只记录日志
                logger.warn("Failed to trigger validation automatically after upload for test case set: {} (ID: {}), error: {}", 
                           testCaseSet.getName(), testCaseSet.getId(), e.getMessage());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("File upload validation failed: {}", e.getMessage());
            return createTestCaseSetErrorResponse("上传失败: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            
        } catch (Exception e) {
            logger.error("File upload failed", e);
            return createTestCaseSetErrorResponse("上传失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 上传软件包文件
     *
     * @param request HTTP请求对象
     * @param description 软件包描述信息（可选）
     * @param overwrite 是否覆盖已存在的软件包（可选，默认为false）
     * @param xUsername 操作用户名（必需，Header中的X-Username）
     * @return 上传结果响应
     */
    @PostMapping("/software-packages")
    public ResponseEntity<?> uploadSoftwarePackage(
            HttpServletRequest request,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "overwrite", required = false, defaultValue = "false") String overwrite,
            @RequestHeader(value = "X-Username", required = true) String xUsername) {
        logger.info("Received software package upload request from user: {}", xUsername);
        
        try {
            // 权限验证
            PermissionValidator.PermissionValidationResult permissionResult = permissionValidator.checkAdminOrOperator(xUsername, "上传软件包");
            if (!permissionResult.isValid()) {
                String errorResponse = String.format("{\"success\":false,\"message\":\"%s\"}", permissionResult.getErrorMessage());
                HttpStatus status = permissionResult.getErrorMessage().contains("未提供用户名") 
                    ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
                return ResponseEntity.status(status).body(errorResponse);
            }
            
            // 获取上传文件
            MultipartFile file = getUploadFile(request, "software package");
            if (file == null) {
                return createStringErrorResponse("未提供上传文件", HttpStatus.BAD_REQUEST);
            }
            
            // 设置默认值
            boolean isOverwrite = "true".equalsIgnoreCase(overwrite);
            
            logger.info("Processing software package upload: {} by user: {}", file.getOriginalFilename(), xUsername);
            
            // 判断文件类型并调用相应的上传方法
            String fileName = file.getOriginalFilename();
            String lowerCaseFileName = fileName.toLowerCase();
            
            if (lowerCaseFileName.endsWith(".zip")) {
                // ZIP包上传
                logger.info("Processing ZIP package upload: {}", fileName);
                
                List<SoftwarePackage> uploadedPackages = softwarePackagesService.uploadZipPackage(file, isOverwrite, description, xUsername);
                
                logger.info("ZIP package upload completed successfully: {}, {} packages uploaded", 
                           fileName, uploadedPackages.size());
                           
                return ResponseEntity.ok(String.format("{\"success\":true,\"message\":\"成功上传ZIP包，包含 %d 个软件包\"}", 
                    uploadedPackages.size()));
                
            } else if (lowerCaseFileName.endsWith(".apk") || lowerCaseFileName.endsWith(".ipa")) {
                // 单个软件包上传
                logger.info("Processing single package upload: {}", fileName);
                
                SoftwarePackage softwarePackage = softwarePackagesService.uploadSinglePackage(file, description, isOverwrite, xUsername);
                
                logger.info("Single package upload completed successfully: {}", fileName);
                
                return ResponseEntity.ok("{\"success\":true,\"message\":\"上传软件包成功\",\"data\":{\"id\":" + 
                    softwarePackage.getId() + ",\"softwareName\":\"" + softwarePackage.getSoftwareName() + 
                    "\",\"fileSize\":" + softwarePackage.getFileSize() + "}}");
                
            } else {
                logger.warn("Unsupported file format for software package: {}", fileName);
                return createStringErrorResponse("不支持的文件格式，仅支持.apk、.ipa和.zip文件", HttpStatus.BAD_REQUEST);
            }
            
        } catch (IllegalArgumentException e) {
            logger.warn("Software package upload validation failed: {}", e.getMessage());
            return createStringErrorResponse("上传失败: " + e.getMessage(), HttpStatus.BAD_REQUEST);
                
        } catch (IOException e) {
            logger.error("Software package upload failed", e);
            return createStringErrorResponse("上传失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                
        } catch (Exception e) {
            logger.error("Software package upload failed", e);
            return createStringErrorResponse("上传失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 上传预处理规则ZIP包
     *
     * @param request HTTP请求对象
     * @param businessZh 业务类型（中文）
     * @param businessEn 业务类型（英文）
     * @param description 描述信息
     * @param xUsername 操作用户名
     * @return 上传结果
     */
    @PostMapping("/preprocess-rule-packages/upload")
    public ResponseEntity<String> uploadPreprocessRulePackage(
            HttpServletRequest request,
            @RequestParam(value = "businessZh", required = false) String businessZh,
            @RequestParam(value = "businessEn", required = false) String businessEn,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "forceOverwrite", required = false) String forceOverwrite,
            @RequestHeader(value = "X-Username", required = true) String xUsername) {
        logger.info("Received preprocess rule package upload request from user: {}", xUsername);
        logger.info("Request parameters - businessZh: {}, businessEn: {}, description: {}, forceOverwrite: {}",
                   businessZh, businessEn, description, forceOverwrite);

        try {
            // 权限验证
            PermissionValidator.PermissionValidationResult permissionResult = permissionValidator.checkAdminOrOperator(xUsername, "上传预处理规则包");
            if (!permissionResult.isValid()) {
                String errorResponse = String.format("{\"success\":false,\"message\":\"%s\"}", permissionResult.getErrorMessage());
                HttpStatus status = permissionResult.getErrorMessage().contains("未提供用户名") 
                    ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
                return ResponseEntity.status(status).body(errorResponse);
            }
            
            // 获取上传文件
            MultipartFile file = getUploadFile(request, "preprocess rule package");
            if (file == null) {
                return createStringErrorResponse("未找到上传文件", HttpStatus.BAD_REQUEST);
            }
            
            // 验证文件格式
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
                return createStringErrorResponse("仅支持ZIP格式文件", HttpStatus.BAD_REQUEST);
            }
            
            // 验证业务类型参数
            if (businessZh == null || businessZh.trim().isEmpty()) {
                return createStringErrorResponse("业务类型中文名称不能为空", HttpStatus.BAD_REQUEST);
            }
            
            if (businessEn == null || businessEn.trim().isEmpty()) {
                return createStringErrorResponse("业务类型英文名称不能为空", HttpStatus.BAD_REQUEST);
            }
            
            logger.info("Processing preprocess rule package upload: {} by user: {}", originalFilename, xUsername);
            
            // 解析覆盖标志
            boolean shouldOverwrite = "true".equalsIgnoreCase(forceOverwrite);
            
            // 调用服务层处理上传
            String result = preprocessRuleService.uploadPreprocessRulePackage(
                file, businessZh, businessEn, description, xUsername, shouldOverwrite);
            
            logger.info("Preprocess rule package upload completed successfully: {}", originalFilename);
            
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Preprocess rule package upload validation failed: {}", e.getMessage());
            return createStringErrorResponse("上传失败: " + e.getMessage(), HttpStatus.BAD_REQUEST);
                
        } catch (IOException e) {
            logger.error("Preprocess rule package upload failed", e);
            return createStringErrorResponse("上传失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                
        } catch (Exception e) {
            logger.error("Preprocess rule package upload failed", e);
            return createStringErrorResponse("上传失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    
    /**
     * 获取上传文件
     */
    private MultipartFile getUploadFile(HttpServletRequest request, String resourceType) {
        // 检查请求是否为multipart类型
        if (!(request instanceof MultipartHttpServletRequest)) {
            logger.warn("{} upload request is not multipart type", resourceType);
            return null;
        }
        
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        
        // 获取上传的文件
        MultipartFile file = null;
        Iterator<String> fileNames = multipartRequest.getFileNames();
        if (fileNames.hasNext()) {
            String fileName = fileNames.next();
            file = multipartRequest.getFile(fileName);
        }
        
        if (file == null || file.isEmpty()) {
            logger.warn("No file provided in {} upload request", resourceType);
            return null;
        }
        
        return file;
    }
    
    /**
     * 创建TestCaseSet错误响应
     */
    private ResponseEntity<TestCaseSetUploadResponse> createTestCaseSetErrorResponse(String message, HttpStatus status) {
        TestCaseSetUploadResponse response = new TestCaseSetUploadResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 创建字符串错误响应
     */
    private ResponseEntity<String> createStringErrorResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status)
            .body("{\"success\":false,\"message\":\"" + message + "\"}");
    }
}

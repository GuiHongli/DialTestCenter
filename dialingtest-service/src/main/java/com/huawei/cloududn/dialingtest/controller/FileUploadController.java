/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.entity.SoftwarePackage;
import com.huawei.cloududn.dialingtest.model.TestCaseSet;
import com.huawei.cloududn.dialingtest.model.TestCaseSetUploadResponse;
import com.huawei.cloududn.dialingtest.service.SoftwarePackagesService;
import com.huawei.cloududn.dialingtest.service.TestCaseSetService;
import com.huawei.cloududn.dialingtest.service.UserRoleService;
import com.huawei.cloududn.dialingtest.service.PreprocessRuleService;
import com.huawei.cloududn.dialingtest.util.OperationLogUtil;

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
    
    /**
     * 上传用例集文件
     *
     * @param request HTTP请求对象
     * @param description 用例集描述信息（可选）
     * @param businessZh 业务类型中文（可选）
     * @param businessEn 业务类型英文（可选）
     * @param overwrite 是否覆盖已存在的用例集（可选，默认为false）
     * @param xUsername 操作用户名（可选，默认为admin）
     * @return 上传结果响应
     */
    @PostMapping("/test-case-sets")
    public ResponseEntity<TestCaseSetUploadResponse> uploadTestCaseSet(
            HttpServletRequest request,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "businessZh", required = false) String businessZh,
            @RequestParam(value = "businessEn", required = false) String businessEn,
            @RequestParam(value = "overwrite", required = false, defaultValue = "false") String overwrite,
            @RequestHeader(value = "X-Username", required = false) String xUsername) {
        logger.info("Received file upload request");
        
        try {
            // 检查请求是否为multipart类型
            if (!(request instanceof MultipartHttpServletRequest)) {
                logger.warn("Request is not multipart type");
                TestCaseSetUploadResponse response = new TestCaseSetUploadResponse();
                response.setSuccess(false);
                response.setMessage("请求类型错误，必须是multipart/form-data");
                return ResponseEntity.badRequest().body(response);
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
                logger.warn("No file provided in upload request");
                TestCaseSetUploadResponse response = new TestCaseSetUploadResponse();
                response.setSuccess(false);
                response.setMessage("未提供上传文件");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 设置默认值
            boolean isOverwrite = "true".equalsIgnoreCase(overwrite);
            String operatorUsername = (xUsername != null && !xUsername.trim().isEmpty()) ? xUsername : "admin";
            
            logger.info("Processing file upload: {} by user: {}", file.getOriginalFilename(), operatorUsername);
            
            // 调用服务层处理文件上传
            TestCaseSet testCaseSet = testCaseSetService.uploadTestCaseSet(
                file, description, businessZh, businessEn, isOverwrite, operatorUsername);
            
            TestCaseSetUploadResponse response = new TestCaseSetUploadResponse();
            response.setSuccess(true);
            response.setMessage(isOverwrite ? "覆盖更新用例集成功" : "上传用例集成功");
            response.setData(testCaseSet);
            
            logger.info("File upload completed successfully: {} v{}", 
                       testCaseSet.getName(), testCaseSet.getVersion());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("File upload validation failed: {}", e.getMessage());
            TestCaseSetUploadResponse response = new TestCaseSetUploadResponse();
            response.setSuccess(false);
            response.setMessage("上传失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            logger.error("File upload failed", e);
            TestCaseSetUploadResponse response = new TestCaseSetUploadResponse();
            response.setSuccess(false);
            response.setMessage("上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
            // 检查权限 - ADMIN和OPERATOR有上传权限
            List<String> userRoles = userRoleService.getUserRolesByUsername(xUsername);
            if (!userRoles.contains("ADMIN") && !userRoles.contains("OPERATOR")) {
                logger.warn("Insufficient permission for software package upload by user: {}", xUsername);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"success\":false,\"message\":\"权限不足，只有管理员和操作员可以上传软件包\"}");
            }
            
            // 检查请求是否为multipart类型
            if (!(request instanceof MultipartHttpServletRequest)) {
                logger.warn("Software package upload request is not multipart type");
                return ResponseEntity.badRequest()
                    .body("{\"success\":false,\"message\":\"请求类型错误，必须是multipart/form-data\"}");
            }
            
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            
            // 调试日志：打印所有参数名
            logger.info("All parameter names: {}", multipartRequest.getParameterNames());
            logger.info("All file names: {}", multipartRequest.getFileNames());
            
            // 获取上传的文件
            MultipartFile file = null;
            Iterator<String> fileNames = multipartRequest.getFileNames();
            while (fileNames.hasNext()) {
                String fileName = fileNames.next();
                logger.info("Processing file name: {}", fileName);
                file = multipartRequest.getFile(fileName);
                if (file != null) {
                    logger.info("File found: {}, size: {}, original name: {}", 
                               fileName, file.getSize(), file.getOriginalFilename());
                    break;
                }
            }
            
            if (file == null || file.isEmpty()) {
                logger.warn("No file provided in software package upload request. File names: {}", 
                           multipartRequest.getFileNames());
                return ResponseEntity.badRequest()
                    .body("{\"success\":false,\"message\":\"未提供上传文件\"}");
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
                return ResponseEntity.badRequest()
                    .body("{\"success\":false,\"message\":\"不支持的文件格式，仅支持.apk、.ipa和.zip文件\"}");
            }
            
        } catch (IllegalArgumentException e) {
            logger.warn("Software package upload validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body("{\"success\":false,\"message\":\"上传失败: " + e.getMessage() + "\"}");
                
        } catch (IOException e) {
            logger.error("Software package upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"success\":false,\"message\":\"上传失败: " + e.getMessage() + "\"}");
                
        } catch (Exception e) {
            logger.error("Software package upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"success\":false,\"message\":\"上传失败: " + e.getMessage() + "\"}");
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
            @RequestHeader(value = "X-Username", required = false) String xUsername) {
        logger.info("Received preprocess rule package upload request");
        logger.info("Request parameters - businessZh: {}, businessEn: {}, description: {}, forceOverwrite: {}, X-Username: {}",
                   businessZh, businessEn, description, forceOverwrite, xUsername);

        try {
            // 检查请求是否为multipart类型
            if (!(request instanceof MultipartHttpServletRequest)) {
                logger.warn("Request is not multipart type");
                return ResponseEntity.badRequest()
                    .body("{\"success\":false,\"message\":\"请求类型错误，必须是multipart/form-data\"}");
            }
            
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            logger.info("File names in request: {}", String.join(", ", 
                       (Iterable<String>) () -> multipartRequest.getFileNames()));
            
            // 获取上传的文件
            MultipartFile file = null;
            Iterator<String> fileNames = multipartRequest.getFileNames();
            if (fileNames.hasNext()) {
                String fileName = fileNames.next();
                file = multipartRequest.getFile(fileName);
            }
            
            if (file == null || file.isEmpty()) {
                logger.warn("No file provided in upload request");
                return ResponseEntity.badRequest()
                    .body("{\"success\":false,\"message\":\"未找到上传文件\"}");
            }
            
            // 验证文件格式
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
                return ResponseEntity.badRequest()
                    .body("{\"success\":false,\"message\":\"仅支持ZIP格式文件\"}");
            }
            
            // 验证业务类型参数
            if (businessZh == null || businessZh.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("{\"success\":false,\"message\":\"业务类型中文名称不能为空\"}");
            }
            
            if (businessEn == null || businessEn.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("{\"success\":false,\"message\":\"业务类型英文名称不能为空\"}");
            }
            
            // 获取操作用户名
            String operatorUsername = (xUsername != null && !xUsername.trim().isEmpty()) ? xUsername : "unknown";
            
            // 检查权限（仅ADMIN和OPERATOR可以上传）
            List<String> allowedRoles = Arrays.asList("ADMIN", "OPERATOR");
            if (!userRoleService.hasAnyRole(operatorUsername, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"success\":false,\"message\":\"权限不足，仅ADMIN和OPERATOR可以上传预处理规则包\"}");
            }
            
            logger.info("Processing preprocess rule package upload: {} by user: {}", originalFilename, operatorUsername);
            
            // 解析覆盖标志
            boolean shouldOverwrite = "true".equalsIgnoreCase(forceOverwrite);
            
            // 调用服务层处理上传
            String result = preprocessRuleService.uploadPreprocessRulePackage(
                file, businessZh, businessEn, description, operatorUsername, shouldOverwrite);
            
            logger.info("Preprocess rule package upload completed successfully: {}", originalFilename);
            
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Preprocess rule package upload validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body("{\"success\":false,\"message\":\"上传失败: " + e.getMessage() + "\"}");
                
        } catch (IOException e) {
            logger.error("Preprocess rule package upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"success\":false,\"message\":\"上传失败: " + e.getMessage() + "\"}");
                
        } catch (Exception e) {
            logger.error("Preprocess rule package upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"success\":false,\"message\":\"上传失败: " + e.getMessage() + "\"}");
        }
    }
}

/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.model.TestCaseSet;
import com.huawei.cloududn.dialingtest.model.TestCaseSetUploadResponse;
import com.huawei.cloududn.dialingtest.service.TestCaseSetService;
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

import java.util.Iterator;

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
    private OperationLogUtil operationLogUtil;
    
    /**
     * 上传用例集文件
     *
     * @param request HTTP请求对象
     * @return 上传结果响应
     */
    @PostMapping("/test-case-sets")
    public ResponseEntity<TestCaseSetUploadResponse> uploadTestCaseSet(HttpServletRequest request) {
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
            
            // 获取其他参数
            String description = multipartRequest.getParameter("description");
            String businessZh = multipartRequest.getParameter("businessZh");
            String overwrite = multipartRequest.getParameter("overwrite");
            String xUsername = request.getHeader("X-Username");
            
            // 设置默认值
            if (overwrite == null) {
                overwrite = "false";
            }
            boolean isOverwrite = "true".equalsIgnoreCase(overwrite);
            String operatorUsername = (xUsername != null && !xUsername.trim().isEmpty()) ? xUsername : "admin";
            
            logger.info("Processing file upload: {} by user: {}", file.getOriginalFilename(), operatorUsername);
            
            // 调用服务层处理文件上传
            TestCaseSet testCaseSet = testCaseSetService.uploadTestCaseSet(
                file, description, businessZh, isOverwrite, operatorUsername);
            
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
}

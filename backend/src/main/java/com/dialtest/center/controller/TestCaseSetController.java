/*
 * Copyright (c) Huawei Telchnologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.dialtest.center.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dialtest.center.entity.TestCaseSet;
import com.dialtest.center.service.TestCaseSetService;

/**
 * 用例集控制器，提供用例集管理的REST API接口
 * 支持用例集的上传、下载、查询、删除等操作
 * 处理ZIP和TAR.GZ格式的文件上传和下载
 * 
 * @author g00940940
 * @since 2025-09-06
 */
@RestController
@RequestMapping("/api/test-case-sets")
public class TestCaseSetController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestCaseSetController.class);
    
    @Autowired
    private TestCaseSetService testCaseSetService;
    
    /**
     * 获取用例集列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getTestCaseSets(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        try {
            Page<TestCaseSet> testCaseSets = testCaseSetService.getTestCaseSets(page, pageSize);
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", testCaseSets.getContent());
            response.put("total", testCaseSets.getTotalElements());
            response.put("page", page);
            response.put("pageSize", pageSize);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取用例集列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 获取用例集详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<TestCaseSet> getTestCaseSet(@PathVariable Long id) {
        try {
            Optional<TestCaseSet> testCaseSet = testCaseSetService.getTestCaseSetById(id);
            if (testCaseSet.isPresent()) {
                return ResponseEntity.ok(testCaseSet.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("获取用例集详情失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 上传用例集
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadTestCaseSet(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {
        
        try {
            // 模拟当前用户（实际应该从认证信息中获取）
            String creator = "admin";
            
            TestCaseSet testCaseSet = testCaseSetService.uploadTestCaseSet(file, description, creator);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "上传成功");
            response.put("data", testCaseSet);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("上传用例集失败: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("上传用例集失败: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "上传失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 下载用例集
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadTestCaseSet(@PathVariable Long id) {
        try {
            Optional<TestCaseSet> testCaseSetOpt = testCaseSetService.getTestCaseSetById(id);
            if (!testCaseSetOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            TestCaseSet testCaseSet = testCaseSetOpt.get();
            byte[] fileContent = testCaseSet.getZipFile();
            
            if (fileContent == null || fileContent.length == 0) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new ByteArrayResource(fileContent);
            
            // 根据文件格式确定下载文件名和Content-Type
            String fileFormat = testCaseSet.getFileFormat();
            String fileExtension = ".zip"; // 默认扩展名
            MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
            
            if ("tar.gz".equals(fileFormat)) {
                fileExtension = ".tar.gz";
                contentType = MediaType.parseMediaType("application/gzip");
            } else if ("zip".equals(fileFormat)) {
                fileExtension = ".zip";
                contentType = MediaType.parseMediaType("application/zip");
            }
            
            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + testCaseSet.getName() + "_" + testCaseSet.getVersion() + fileExtension + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("下载用例集失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 删除用例集
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestCaseSet(@PathVariable Long id) {
        try {
            testCaseSetService.deleteTestCaseSet(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("删除用例集失败: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("删除用例集失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 更新用例集信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<TestCaseSet> updateTestCaseSet(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        try {
            String name = request.get("name");
            String version = request.get("version");
            String description = request.get("description");
            
            TestCaseSet updated = testCaseSetService.updateTestCaseSet(id, name, version, description);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.warn("更新用例集失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("更新用例集失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
}

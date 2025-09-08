/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.controller;

import com.huawei.dialtest.center.entity.TestCase;
import com.huawei.dialtest.center.entity.TestCaseSet;
import com.huawei.dialtest.center.service.TestCaseSetService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     *
     * @param page 页码，从1开始
     * @param pageSize 每页大小
     * @return 用例集分页数据
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getTestCaseSets(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        logger.info("Getting test case sets - page: {}, size: {}", page, pageSize);
        try {
            Page<TestCaseSet> testCaseSets = testCaseSetService.getTestCaseSets(page, pageSize);

            Map<String, Object> response = new HashMap<>();
            response.put("data", testCaseSets.getContent());
            response.put("total", testCaseSets.getTotalElements());
            response.put("page", page);
            response.put("pageSize", pageSize);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (DataAccessException e) {
            logger.error("Database error while getting test case sets: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取用例集详情
     *
     * @param id 用例集ID
     * @return 用例集详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<TestCaseSet> getTestCaseSet(@PathVariable Long id) {
        logger.info("Getting test case set details for ID: {}", id);
        try {
            Optional<TestCaseSet> testCaseSet = testCaseSetService.getTestCaseSetById(id);
            if (testCaseSet.isPresent()) {
                return ResponseEntity.ok(testCaseSet.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (DataAccessException e) {
            logger.error("Database error while getting test case set details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 上传用例集
     *
     * @param file 用例集文件，支持.zip和.tar.gz格式
     * @param description 用例集描述信息（可选）
     * @param business 业务类型（可选，默认为"VPN阻断业务"）
     * @return 上传结果
     * @throws IOException 文件读取失败时抛出
     * @throws IllegalArgumentException 当文件格式不正确或参数无效时抛出
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadTestCaseSet(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "business", required = false, defaultValue = "VPN阻断业务") String business) {
        logger.info("Uploading test case set: {}", file.getOriginalFilename());
        try {
            // 模拟当前用户（实际应该从认证信息中获取）
            String creator = "admin";

            TestCaseSet testCaseSet = testCaseSetService.uploadTestCaseSet(file, description, creator, business);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Upload successful");
            response.put("data", testCaseSet);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Test case set upload failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (IOException e) {
            logger.error("File I/O error during upload: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "File processing failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (DataAccessException e) {
            logger.error("Database error during test case set upload: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Upload failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 下载用例集
     *
     * @param id 用例集ID
     * @return 用例集文件资源
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadTestCaseSet(@PathVariable Long id) {
        logger.info("Downloading test case set with ID: {}", id);
        try {
            Optional<TestCaseSet> testCaseSetOpt = testCaseSetService.getTestCaseSetById(id);
            if (!testCaseSetOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            TestCaseSet testCaseSet = testCaseSetOpt.get();
            byte[] fileContent = testCaseSet.getFileContent();

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
            } else {
                logger.warn("Unsupported file format: {}", fileFormat);
                throw new IllegalArgumentException("Unsupported file format: " + fileFormat);
            }

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + testCaseSet.getName() + "_" + testCaseSet.getVersion() + fileExtension + "\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (DataAccessException e) {
            logger.error("Database error while downloading test case set: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除用例集
     *
     * @param id 用例集ID
     * @return 删除结果
     * @throws IllegalArgumentException 当用例集不存在时抛出
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestCaseSet(@PathVariable Long id) {
        logger.info("Deleting test case set with ID: {}", id);
        try {
            testCaseSetService.deleteTestCaseSet(id);
            logger.info("Test case set deleted successfully: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Test case set not found for deletion: {}", id);
            return ResponseEntity.notFound().build();
        } catch (DataAccessException e) {
            logger.error("Database error while deleting test case set: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 更新用例集信息
     *
     * @param id 用例集ID
     * @param request 更新请求数据，包含name、version、description字段
     * @return 更新后的用例集对象
     * @throws IllegalArgumentException 当用例集不存在或参数无效时抛出
     */
    @PutMapping("/{id}")
    public ResponseEntity<TestCaseSet> updateTestCaseSet(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        logger.info("Updating test case set with ID: {}", id);
        try {
            String name = request.get("name");
            String version = request.get("version");
            String description = request.get("description");

            TestCaseSet updated = testCaseSetService.updateTestCaseSet(id, name, version, description);
            logger.info("Test case set updated successfully: {} - {}", name, version);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (DataAccessException e) {
            logger.error("Database error while updating test case set: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取用例集的测试用例列表
     *
     * @param id 用例集ID
     * @param page 页码，从1开始
     * @param pageSize 每页大小
     * @return 测试用例分页数据
     */
    @GetMapping("/{id}/test-cases")
    public ResponseEntity<Map<String, Object>> getTestCases(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        logger.info("Getting test cases for test case set: {}, page: {}, size: {}", id, page, pageSize);
        try {
            Page<TestCase> testCases = testCaseSetService.getTestCases(id, page, pageSize);

            Map<String, Object> response = new HashMap<>();
            response.put("data", testCases.getContent());
            response.put("total", testCases.getTotalElements());
            response.put("page", page);
            response.put("pageSize", pageSize);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (DataAccessException e) {
            logger.error("Database error while getting test cases: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取用例集中没有脚本的测试用例列表
     *
     * @param id 用例集ID
     * @return 没有脚本的测试用例列表
     */
    @GetMapping("/{id}/missing-scripts")
    public ResponseEntity<Map<String, Object>> getMissingScripts(@PathVariable Long id) {
        logger.info("Getting missing scripts for test case set: {}", id);
        try {
            List<TestCase> missingScripts = testCaseSetService.getMissingScripts(id);
            long missingCount = testCaseSetService.countMissingScripts(id);

            Map<String, Object> response = new HashMap<>();
            response.put("data", missingScripts);
            response.put("count", missingCount);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (DataAccessException e) {
            logger.error("Database error while getting missing scripts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}

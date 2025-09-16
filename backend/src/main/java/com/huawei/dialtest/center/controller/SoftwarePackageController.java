/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.controller;

import com.huawei.dialtest.center.dto.ApiResponse;
import com.huawei.dialtest.center.dto.PagedResponse;
import com.huawei.dialtest.center.entity.SoftwarePackage;
import com.huawei.dialtest.center.service.SoftwarePackageService;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 软件包控制器，提供软件包管理的REST API接口
 * 支持软件包的上传、下载、查询、删除等操作
 * 处理APK和IPA格式的文件上传和下载，以及ZIP包批量上传
 *
 * @author g00940940
 * @since 2025-09-09
 */
@RestController
@RequestMapping("/api/software-packages")
public class SoftwarePackageController {
    private static final Logger logger = LoggerFactory.getLogger(SoftwarePackageController.class);

    @Autowired
    private SoftwarePackageService softwarePackageService;

    /**
     * 获取软件包列表
     *
     * @param page 页码，从1开始
     * @param pageSize 每页大小
     * @param platform 平台过滤条件（可选）
     * @param creator 创建者过滤条件（可选）
     * @param softwareName 软件名称过滤条件（可选）
     * @return 软件包分页数据
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<SoftwarePackage>>> getSoftwarePackages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String creator,
            @RequestParam(required = false) String softwareName) {
        logger.info("Getting software packages - page: {}, size: {}, platform: {}, creator: {}, softwareName: {}", 
                   page, pageSize, platform, creator, softwareName);
        try {
            Page<SoftwarePackage> softwarePackages = softwarePackageService.getSoftwarePackages(
                    page, pageSize, platform, creator, softwareName);

            PagedResponse<SoftwarePackage> pagedResponse = new PagedResponse<>(
                softwarePackages.getContent(), 
                softwarePackages.getTotalElements(), 
                page, 
                pageSize
            );

            return ResponseEntity.ok(ApiResponse.success(pagedResponse));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (DataAccessException e) {
            logger.error("Database error while getting software packages: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("DATABASE_ERROR", "Database operation failed"));
        }
    }

    /**
     * 获取软件包详情
     *
     * @param id 软件包ID
     * @return 软件包详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SoftwarePackage>> getSoftwarePackage(@PathVariable Long id) {
        logger.info("Getting software package details for ID: {}", id);
        try {
            Optional<SoftwarePackage> softwarePackage = softwarePackageService.getSoftwarePackageById(id);
            if (softwarePackage.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(softwarePackage.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("NOT_FOUND", "Software package not found"));
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (DataAccessException e) {
            logger.error("Database error while getting software package details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("DATABASE_ERROR", "Database operation failed"));
        }
    }

    /**
     * 上传单个软件包
     *
     * @param file 软件包文件，支持.apk和.ipa格式
     * @param description 软件包描述信息（可选）
     * @return 上传结果
     * @throws IOException 文件读取失败时抛出
     * @throws IllegalArgumentException 当文件格式不正确或参数无效时抛出
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<SoftwarePackage>> uploadSoftwarePackage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {
        logger.info("Uploading software package: {}", file.getOriginalFilename());
        try {
            // 模拟当前用户（实际应该从认证信息中获取）
            String creator = "admin";

            SoftwarePackage softwarePackage = softwarePackageService.uploadSoftwarePackage(file, description, creator);

            return ResponseEntity.ok(ApiResponse.success(softwarePackage, "Upload successful"));
        } catch (IllegalArgumentException e) {
            logger.warn("Software package upload failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (IOException e) {
            logger.error("File I/O error during upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("FILE_PROCESSING_ERROR", "File processing failed"));
        } catch (DataAccessException e) {
            logger.error("Database error during software package upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("DATABASE_ERROR", "Upload failed"));
        }
    }

    /**
     * 上传ZIP包，批量上传多个软件包
     *
     * @param file ZIP文件，包含多个APK或IPA文件
     * @return 上传结果
     * @throws IOException 文件读取失败时抛出
     * @throws IllegalArgumentException 当文件格式不正确或参数无效时抛出
     */
    @PostMapping("/upload-zip")
    public ResponseEntity<ApiResponse<List<SoftwarePackage>>> uploadZipPackage(
            @RequestParam("file") MultipartFile file) {
        logger.info("Uploading ZIP package: {}", file.getOriginalFilename());
        try {
            // 模拟当前用户（实际应该从认证信息中获取）
            String creator = "admin";

            List<SoftwarePackage> softwarePackages = softwarePackageService.uploadZipPackage(file, creator);

            return ResponseEntity.ok(ApiResponse.success(softwarePackages, "ZIP package upload successful"));
        } catch (IllegalArgumentException e) {
            logger.warn("ZIP package upload failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (IOException e) {
            logger.error("File I/O error during ZIP upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("FILE_PROCESSING_ERROR", "ZIP file processing failed"));
        } catch (DataAccessException e) {
            logger.error("Database error during ZIP package upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("DATABASE_ERROR", "ZIP upload failed"));
        }
    }

    /**
     * 下载软件包
     *
     * @param id 软件包ID
     * @return 软件包文件资源
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadSoftwarePackage(@PathVariable Long id) {
        logger.info("Downloading software package with ID: {}", id);
        try {
            Optional<SoftwarePackage> softwarePackageOpt = softwarePackageService.getSoftwarePackageById(id);
            if (!softwarePackageOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            SoftwarePackage softwarePackage = softwarePackageOpt.get();
            byte[] fileContent = softwarePackage.getFileContent();

            if (fileContent == null || fileContent.length == 0) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new ByteArrayResource(fileContent);

            // 根据文件格式确定Content-Type
            String fileFormat = softwarePackage.getFileFormat();
            MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;

            if ("apk".equals(fileFormat)) {
                contentType = MediaType.parseMediaType("application/vnd.android.package-archive");
            } else if ("ipa".equals(fileFormat)) {
                contentType = MediaType.parseMediaType("application/octet-stream");
            } else {
                logger.warn("Unsupported file format: {}", fileFormat);
                throw new IllegalArgumentException("Unsupported file format: " + fileFormat);
            }

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + softwarePackage.getSoftwareName() + "\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (DataAccessException e) {
            logger.error("Database error while downloading software package: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除软件包
     *
     * @param id 软件包ID
     * @return 删除结果
     * @throws IllegalArgumentException 当软件包不存在时抛出
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSoftwarePackage(@PathVariable Long id) {
        logger.info("Deleting software package with ID: {}", id);
        try {
            softwarePackageService.deleteSoftwarePackage(id);
            logger.info("Software package deleted successfully: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Software package not found for deletion: {}", id);
            return ResponseEntity.notFound().build();
        } catch (DataAccessException e) {
            logger.error("Database error while deleting software package: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 更新软件包信息
     *
     * @param id 软件包ID
     * @param request 更新请求数据，包含softwareName、version、description字段
     * @return 更新后的软件包对象
     * @throws IllegalArgumentException 当软件包不存在或参数无效时抛出
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SoftwarePackage>> updateSoftwarePackage(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        logger.info("Updating software package with ID: {}", id);
        try {
            String softwareName = request.get("softwareName");
            String description = request.get("description");

            SoftwarePackage updated = softwarePackageService.updateSoftwarePackage(id, softwareName, description);
            logger.info("Software package updated successfully: {}", softwareName);
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (DataAccessException e) {
            logger.error("Database error while updating software package: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("DATABASE_ERROR", "Database operation failed"));
        }
    }

    /**
     * 获取软件包统计信息
     *
     * @return 包含各平台软件包数量的统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStatistics() {
        logger.info("Getting software package statistics");
        try {
            Map<String, Long> statistics = softwarePackageService.getPlatformStatistics();
            
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (DataAccessException e) {
            logger.error("Database error while getting statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("DATABASE_ERROR", "Failed to get statistics"));
        }
    }
}

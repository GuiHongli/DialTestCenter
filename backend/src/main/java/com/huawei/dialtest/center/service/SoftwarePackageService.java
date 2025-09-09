/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import com.huawei.dialtest.center.entity.SoftwarePackage;
import com.huawei.dialtest.center.repository.SoftwarePackageRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 软件包服务类，提供软件包的业务逻辑处理
 * 包括上传、下载、删除、查询等操作
 * 支持APK和IPA格式的文件处理，以及ZIP包批量上传
 *
 * @author g00940940
 * @since 2025-09-09
 */
@Service
public class SoftwarePackageService {
    private static final Logger logger = LoggerFactory.getLogger(SoftwarePackageService.class);

    @Autowired
    private SoftwarePackageRepository softwarePackageRepository;

    /**
     * 获取软件包列表（分页）
     *
     * @param page 页码，从1开始
     * @param pageSize 每页大小
     * @param platform 平台过滤条件（可选）
     * @param creator 创建者过滤条件（可选）
     * @param softwareName 软件名称过滤条件（可选）
     * @return 软件包分页数据
     */
    public Page<SoftwarePackage> getSoftwarePackages(int page, int pageSize, String platform, String creator, String softwareName) {
        logger.debug("Getting software packages - page: {}, size: {}, platform: {}, creator: {}, softwareName: {}", 
                    page, pageSize, platform, creator, softwareName);
        
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        
        // 如果所有过滤条件都为空，返回所有数据
        if (platform == null && creator == null && softwareName == null) {
            return softwarePackageRepository.findAllByOrderByCreatedTimeDesc(pageable);
        }
        
        // 使用自定义查询方法
        return softwarePackageRepository.findByConditions(platform, creator, softwareName, pageable);
    }

    /**
     * 根据ID获取软件包
     *
     * @param id 软件包ID
     * @return 软件包对象，如果不存在则返回空
     */
    public Optional<SoftwarePackage> getSoftwarePackageById(Long id) {
        logger.debug("Getting software package by ID: {}", id);
        return softwarePackageRepository.findById(id);
    }

    /**
     * 上传单个软件包
     *
     * @param file 软件包文件，支持.apk和.ipa格式
     * @param description 软件包描述信息
     * @param creator 创建者用户名
     * @return 保存后的软件包实体对象
     * @throws IOException 文件读取失败时抛出
     * @throws IllegalArgumentException 当文件格式不正确或参数无效时抛出
     */
    @Transactional
    public SoftwarePackage uploadSoftwarePackage(MultipartFile file, String description, String creator) throws IOException {
        logger.info("Starting software package upload: {}", file.getOriginalFilename());

        // 验证文件
        validateFile(file);

        // 解析文件名和格式
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }

        String fileFormat;
        String platform;
        String softwareName = fileName; // 直接使用完整文件名

        if (fileName.toLowerCase().endsWith(".apk")) {
            fileFormat = "apk";
            platform = "android";
        } else if (fileName.toLowerCase().endsWith(".ipa")) {
            fileFormat = "ipa";
            platform = "ios";
        } else {
            throw new IllegalArgumentException("Only APK and IPA format files are supported");
        }

        // 检查是否已存在相同文件名的软件包
        if (softwarePackageRepository.existsBySoftwareName(softwareName)) {
            throw new IllegalArgumentException("Software package with the same file name already exists");
        }

        // 读取文件内容
        byte[] fileContent = file.getBytes();

        // 计算文件内容的SHA512哈希值
        String sha512 = calculateSHA512(fileContent);

        // 检查是否已存在相同SHA512的软件包
        if (softwarePackageRepository.existsBySha512(sha512)) {
            throw new IllegalArgumentException("Software package with the same content already exists");
        }

        // 创建软件包记录
        SoftwarePackage softwarePackage = new SoftwarePackage(
            softwareName, fileContent, fileFormat, platform, creator, file.getSize(), sha512
        );
        softwarePackage.setDescription(description);

        SoftwarePackage saved = softwarePackageRepository.save(softwarePackage);
        logger.info("Software package uploaded successfully: {}, format: {}, file size: {} bytes, SHA512: {}", 
                   softwareName, fileFormat, fileContent.length, sha512);

        return saved;
    }

    /**
     * 上传ZIP包，解析其中的多个安装包
     *
     * @param file ZIP文件，包含多个APK或IPA文件
     * @param creator 创建者用户名
     * @return 保存后的软件包实体对象列表
     * @throws IOException 文件读取失败时抛出
     * @throws IllegalArgumentException 当文件格式不正确或参数无效时抛出
     */
    @Transactional
    public List<SoftwarePackage> uploadZipPackage(MultipartFile file, String creator) throws IOException {
        logger.info("Starting ZIP package upload: {}", file.getOriginalFilename());

        // 验证ZIP文件
        validateZipFile(file);

        // 读取ZIP文件内容（用于验证文件大小）
        file.getBytes();
        List<SoftwarePackage> uploadedPackages = new ArrayList<>();

        try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String entryName = entry.getName();
                    
                    // 只处理APK和IPA文件
                    if (entryName.toLowerCase().endsWith(".apk") || entryName.toLowerCase().endsWith(".ipa")) {
                        try {
                            // 读取文件内容
                            byte[] fileContent = zipInputStream.readAllBytes();
                            
                            // 解析文件信息
                            String fileFormat;
                            String platform;
                            String softwareName = entryName.substring(entryName.lastIndexOf("/") + 1); // 获取文件名（带后缀）

                            if (entryName.toLowerCase().endsWith(".apk")) {
                                fileFormat = "apk";
                                platform = "android";
                            } else {
                                fileFormat = "ipa";
                                platform = "ios";
                            }

                            // 检查是否已存在
                            if (softwarePackageRepository.existsBySoftwareName(softwareName)) {
                                logger.warn("Software package with file name {} already exists, skipping", softwareName);
                                continue;
                            }

                            // 计算SHA512
                            String sha512 = calculateSHA512(fileContent);
                            if (softwarePackageRepository.existsBySha512(sha512)) {
                                logger.warn("Software package with SHA512 {} already exists, skipping", sha512);
                                continue;
                            }

                            // 创建软件包记录
                            SoftwarePackage softwarePackage = new SoftwarePackage(
                                entryName, fileContent, fileFormat, platform, creator, (long) fileContent.length, sha512
                            );
                            softwarePackage.setDescription("Uploaded from ZIP package: " + file.getOriginalFilename());

                            SoftwarePackage saved = softwarePackageRepository.save(softwarePackage);
                            uploadedPackages.add(saved);
                            
                            logger.info("Software package from ZIP uploaded: {}, format: {}, size: {} bytes", 
                                       entryName, fileFormat, fileContent.length);

                        } catch (Exception e) {
                            logger.error("Failed to process file {} from ZIP: {}", entryName, e.getMessage());
                        }
                    }
                }
                zipInputStream.closeEntry();
            }
        }

        logger.info("ZIP package upload completed, {} software packages uploaded", uploadedPackages.size());
        return uploadedPackages;
    }

    /**
     * 删除软件包
     *
     * @param id 软件包ID
     * @throws IllegalArgumentException 当软件包不存在时抛出
     */
    public void deleteSoftwarePackage(Long id) {
        logger.info("Deleting software package with ID: {}", id);

        Optional<SoftwarePackage> softwarePackageOpt = softwarePackageRepository.findById(id);
        if (softwarePackageOpt.isPresent()) {
            SoftwarePackage softwarePackage = softwarePackageOpt.get();

            // 删除数据库记录（文件内容也会一起删除）
            softwarePackageRepository.deleteById(id);
            logger.info("Software package deleted successfully: {}", softwarePackage.getSoftwareName());
        } else {
            throw new IllegalArgumentException("Software package does not exist");
        }
    }

    /**
     * 更新软件包信息
     *
     * @param id 软件包ID
     * @param softwareName 新的软件名称
     * @param description 新的描述信息
     * @return 更新后的软件包对象
     * @throws IllegalArgumentException 当软件包不存在时抛出
     */
    public SoftwarePackage updateSoftwarePackage(Long id, String softwareName, String description) {
        logger.info("Updating software package with ID: {}", id);

        Optional<SoftwarePackage> softwarePackageOpt = softwarePackageRepository.findById(id);
        if (softwarePackageOpt.isPresent()) {
            SoftwarePackage softwarePackage = softwarePackageOpt.get();

            softwarePackage.setSoftwareName(softwareName);
            softwarePackage.setDescription(description);

            SoftwarePackage updated = softwarePackageRepository.save(softwarePackage);
            logger.info("Software package updated successfully: {}", softwareName);
            return updated;
        } else {
            throw new IllegalArgumentException("Software package does not exist");
        }
    }

    /**
     * 根据平台获取软件包统计信息
     *
     * @return 包含各平台软件包数量的Map
     */
    public java.util.Map<String, Long> getPlatformStatistics() {
        logger.debug("Getting platform statistics");
        
        java.util.Map<String, Long> statistics = new java.util.HashMap<>();
        statistics.put("android", softwarePackageRepository.countByPlatform("android"));
        statistics.put("ios", softwarePackageRepository.countByPlatform("ios"));
        statistics.put("total", softwarePackageRepository.count());
        
        return statistics;
    }

    /**
     * 计算文件内容的SHA512哈希值
     *
     * @param fileContent 文件内容字节数组
     * @return SHA512哈希值的十六进制字符串
     * @throws RuntimeException 当计算哈希值失败时抛出
     */
    private String calculateSHA512(byte[] fileContent) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(fileContent);
            
            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-512 algorithm not available", e);
            throw new RuntimeException("Failed to calculate SHA512 hash", e);
        }
    }

    /**
     * 验证单个软件包文件格式和大小
     *
     * @param file 上传的文件
     * @throws IllegalArgumentException 当文件为空、大小超限或格式不支持时抛出
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // 检查文件大小 (500MB)
        long maxSize = 500 * 1024 * 1024; // 500MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size cannot exceed 500MB");
        }

        // 检查文件类型 - 支持 .apk 和 .ipa
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }

        String lowerFileName = fileName.toLowerCase();
        if (!lowerFileName.endsWith(".apk") && !lowerFileName.endsWith(".ipa")) {
            throw new IllegalArgumentException("Only APK and IPA format files are supported");
        }

        logger.debug("File validation passed for: {}", fileName);
    }

    /**
     * 验证ZIP文件格式和大小
     *
     * @param file 上传的ZIP文件
     * @throws IllegalArgumentException 当文件为空、大小超限或格式不支持时抛出
     */
    private void validateZipFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("ZIP file cannot be empty");
        }

        // 检查文件大小 (1GB)
        long maxSize = 1024 * 1024 * 1024; // 1GB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("ZIP file size cannot exceed 1GB");
        }

        // 检查文件类型 - 支持 .zip
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("ZIP file name cannot be null");
        }

        String lowerFileName = fileName.toLowerCase();
        if (!lowerFileName.endsWith(".zip")) {
            throw new IllegalArgumentException("Only ZIP format files are supported for batch upload");
        }

        logger.debug("ZIP file validation passed for: {}", fileName);
    }
}

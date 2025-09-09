/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 软件包实体类，用于存储和管理手机APP安装包信息
 * 支持Android APK和iOS IPA格式的文件存储，包含文件内容、SHA512哈希值等元数据
 * 提供完整的CRUD操作支持，包括文件上传、下载、查询等功能
 *
 * @author g00940940
 * @since 2025-09-09
 */
@Entity
@Table(name = "software_package")
public class SoftwarePackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "software_name", nullable = false, length = 255)
    private String softwareName; // 软件名称（完整文件名，带后缀）

    @Column(name = "file_content", nullable = false, columnDefinition = "bytea")
    private byte[] fileContent; // 文件内容

    @Column(name = "file_format", nullable = false, length = 10)
    private String fileFormat; // 文件格式：apk 或 ipa

    @Column(name = "sha512", length = 128)
    private String sha512; // 文件内容的SHA512哈希值

    @Column(name = "platform", nullable = false, length = 20)
    private String platform; // 平台：android 或 ios

    @Column(name = "creator", nullable = false, length = 100)
    private String creator; // 创建者

    @Column(name = "file_size", nullable = false)
    private Long fileSize; // 文件大小

    @Column(name = "description", length = 1000)
    private String description; // 描述信息

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 默认构造函数
     *
     * @author g00940940
     * @since 2025-09-09
     */
    public SoftwarePackage() {}

    /**
     * 带参数构造函数，用于创建新的软件包实例
     *
     * @param softwareName 软件名称（完整文件名，带后缀）
     * @param fileContent 文件内容字节数组
     * @param fileFormat 文件格式（apk或ipa）
     * @param platform 平台（android或ios）
     * @param creator 创建者
     * @param fileSize 文件大小
     * @param sha512 文件内容的SHA512哈希值
     * @author g00940940
     * @since 2025-09-09
     */
    public SoftwarePackage(String softwareName, byte[] fileContent, String fileFormat, 
                          String platform, String creator, Long fileSize, String sha512) {
        this.softwareName = softwareName;
        this.fileContent = fileContent;
        this.fileFormat = fileFormat;
        this.platform = platform;
        this.creator = creator;
        this.fileSize = fileSize;
        this.sha512 = sha512;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSoftwareName() {
        return softwareName;
    }

    public void setSoftwareName(String softwareName) {
        this.softwareName = softwareName;
    }


    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public String getSha512() {
        return sha512;
    }

    public void setSha512(String sha512) {
        this.sha512 = sha512;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SoftwarePackage that = (SoftwarePackage) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(softwareName, that.softwareName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, softwareName);
    }

    @Override
    public String toString() {
        return "SoftwarePackage{" +
               "id=" + id +
               ", softwareName='" + softwareName + '\'' +
               ", fileContentSize=" + (fileContent != null ? fileContent.length : 0) +
               ", fileFormat='" + fileFormat + '\'' +
               ", platform='" + platform + '\'' +
               ", creator='" + creator + '\'' +
               ", fileSize=" + fileSize +
               ", sha512='" + sha512 + '\'' +
               ", description='" + description + '\'' +
               ", createdTime=" + createdTime +
               ", updatedTime=" + updatedTime +
               '}';
    }
}

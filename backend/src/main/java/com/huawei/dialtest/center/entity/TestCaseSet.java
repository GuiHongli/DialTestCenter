/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 用例集实体类，用于存储和管理测试用例集信息
 * 支持ZIP和TAR.GZ格式的文件存储，包含文件内容、格式、大小等元数据
 * 提供完整的CRUD操作支持，包括文件上传、下载、查询等功能
 * 
 * @author g00940940
 * @since 2025-09-06
 */
@Entity
@Table(name = "test_case_set")
public class TestCaseSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "version", nullable = false, length = 50)
    private String version;

    @Column(name = "zip_file", nullable = false, columnDefinition = "bytea")
    private byte[] zipFile;

    @Column(name = "file_format", nullable = false, length = 10)
    private String fileFormat; // 文件格式：zip 或 tar.gz

    @Column(name = "creator", nullable = false, length = 100)
    private String creator;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "description", length = 1000)
    private String description;

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
     * @since 2025-09-06
     */
    public TestCaseSet() {}

    /**
     * 带参数构造函数，用于创建新的用例集实例
     * 
     * @param name 用例集名称
     * @param version 用例集版本
     * @param zipFile 文件内容字节数组
     * @param fileFormat 文件格式（zip或tar.gz）
     * @param creator 创建者
     * @param fileSize 文件大小
     * @author g00940940
     * @since 2025-09-06
     */
    public TestCaseSet(String name, String version, byte[] zipFile, String fileFormat, String creator, Long fileSize) {
        this.name = name;
        this.version = version;
        this.zipFile = zipFile;
        this.fileFormat = fileFormat;
        this.creator = creator;
        this.fileSize = fileSize;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public byte[] getZipFile() {
        return zipFile;
    }

    public void setZipFile(byte[] zipFile) {
        this.zipFile = zipFile;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCaseSet that = (TestCaseSet) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(name, that.name) &&
               Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, version);
    }

    @Override
    public String toString() {
        return "TestCaseSet{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", version='" + version + '\'' +
               ", zipFileSize=" + (zipFile != null ? zipFile.length : 0) +
               ", fileFormat='" + fileFormat + '\'' +
               ", creator='" + creator + '\'' +
               ", fileSize=" + fileSize +
               ", description='" + description + '\'' +
               ", createdTime=" + createdTime +
               ", updatedTime=" + updatedTime +
               '}';
    }
}

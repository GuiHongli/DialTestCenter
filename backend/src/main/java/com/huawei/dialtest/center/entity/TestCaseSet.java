/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.entity;

import java.util.Objects;

/**
 * 用例集实体类，用于存储和管理测试用例集信息
 * 支持ZIP和TAR.GZ格式的文件存储，包含文件内容、格式、大小等元数据
 * 提供完整的CRUD操作支持，包括文件上传、下载、查询等功能
 *
 * @author g00940940
 * @since 2025-09-06
 */
public class TestCaseSet {
    private Long id;
    private String name;
    private String version;
    private byte[] fileContent;
    private String fileFormat; // 文件格式：zip 或 tar.gz
    private String sha512; // 文件内容的SHA512哈希值
    private String business; // 业务类型
    private String creator;
    private Long fileSize;
    private String description;

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
     * @param fileContent 文件内容字节数组
     * @param fileFormat 文件格式（zip或tar.gz）
     * @param creator 创建者
     * @param fileSize 文件大小
     * @param sha512 文件内容的SHA512哈希值
     * @param business 业务类型
     * @author g00940940
     * @since 2025-09-06
     */
    public TestCaseSet(String name, String version, byte[] fileContent, String fileFormat, String creator, Long fileSize, String sha512, String business) {
        this.name = name;
        this.version = version;
        this.fileContent = fileContent;
        this.fileFormat = fileFormat;
        this.creator = creator;
        this.fileSize = fileSize;
        this.sha512 = sha512;
        this.business = business;
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

    public String getSha512() {
        return sha512;
    }

    public void setSha512(String sha512) {
        this.sha512 = sha512;
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
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
               ", fileContentSize=" + (fileContent != null ? fileContent.length : 0) +
               ", fileFormat='" + fileFormat + '\'' +
               ", creator='" + creator + '\'' +
               ", fileSize=" + fileSize +
               ", sha512='" + sha512 + '\'' +
               ", business='" + business + '\'' +
               ", description='" + description + '\'' +
               '}';
    }
}

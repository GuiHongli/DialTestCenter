/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * SoftwarePackageFileContent 单元测试
 * 
 * @author g00940940
 * @since 2025-01-27
 */
public class SoftwarePackageFileContentTest {

    private SoftwarePackageFileContent softwarePackageFileContent;

    @Before
    public void setUp() {
        softwarePackageFileContent = new SoftwarePackageFileContent();
    }

    /**
     * 测试默认构造函数
     */
    @Test
    public void testConstructor_Default_CreatesEmptyObject() {
        // Arrange & Act
        SoftwarePackageFileContent content = new SoftwarePackageFileContent();

        // Assert
        assertNotNull(content);
        assertNull(content.getFileContent());
    }

    /**
     * 测试设置和获取文件内容
     */
    @Test
    public void testSetAndGetFileContent_ValidContent_ReturnsContent() {
        // Arrange
        byte[] fileContent = "test content".getBytes();

        // Act
        softwarePackageFileContent.setFileContent(fileContent);

        // Assert
        assertNotNull(softwarePackageFileContent.getFileContent());
        assertArrayEquals(fileContent, softwarePackageFileContent.getFileContent());
    }

    /**
     * 测试设置null文件内容
     */
    @Test
    public void testSetFileContent_Null_SetsNull() {
        // Act
        softwarePackageFileContent.setFileContent(null);

        // Assert
        assertNull(softwarePackageFileContent.getFileContent());
    }

    /**
     * 测试设置空文件内容
     */
    @Test
    public void testSetFileContent_EmptyArray_SetsEmptyArray() {
        // Arrange
        byte[] emptyContent = new byte[0];

        // Act
        softwarePackageFileContent.setFileContent(emptyContent);

        // Assert
        assertNotNull(softwarePackageFileContent.getFileContent());
        assertEquals(0, softwarePackageFileContent.getFileContent().length);
    }

    /**
     * 测试设置大型文件内容
     */
    @Test
    public void testSetFileContent_LargeContent_ReturnsContent() {
        // Arrange
        byte[] largeContent = new byte[1000];
        for (int i = 0; i < 1000; i++) {
            largeContent[i] = (byte) i;
        }

        // Act
        softwarePackageFileContent.setFileContent(largeContent);

        // Assert
        assertNotNull(softwarePackageFileContent.getFileContent());
        assertEquals(1000, softwarePackageFileContent.getFileContent().length);
        assertArrayEquals(largeContent, softwarePackageFileContent.getFileContent());
    }
}


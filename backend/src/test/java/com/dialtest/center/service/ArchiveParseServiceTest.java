/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.dialtest.center.service;

import com.huawei.dialtest.center.service.ArchiveParseService;
import com.huawei.dialtest.center.service.ArchiveParseService.ArchiveValidationResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

/**
 * 压缩包解析服务类单元测试
 * 测试ArchiveParseService的压缩包解析功能，包括ZIP和TAR.GZ格式处理
 *
 * @author g00940940
 * @since 2025-09-08
 */
@RunWith(MockitoJUnitRunner.class)
public class ArchiveParseServiceTest {

    @InjectMocks
    private ArchiveParseService archiveParseService;

    /**
     * 测试提取cases.xlsx文件 - ZIP格式，包含cases.xlsx
     */
    @Test
    public void testExtractCasesExcel_ZipWithCasesXlsx_ShouldReturnExcelData() throws IOException {
        // Arrange
        byte[] zipData = createZipWithCasesXlsx();
        String fileFormat = "zip";

        // Act
        byte[] result = archiveParseService.extractCasesExcel(zipData, fileFormat);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    /**
     * 测试提取cases.xlsx文件 - ZIP格式，不包含cases.xlsx
     */
    @Test
    public void testExtractCasesExcel_ZipWithoutCasesXlsx_ShouldReturnNull() throws IOException {
        // Arrange
        byte[] zipData = createZipWithoutCasesXlsx();
        String fileFormat = "zip";

        // Act
        byte[] result = archiveParseService.extractCasesExcel(zipData, fileFormat);

        // Assert
        assertNull(result);
    }

    /**
     * 测试提取脚本文件名列表 - ZIP格式，包含脚本文件
     */
    @Test
    public void testExtractScriptFileNames_ZipWithScripts_ShouldReturnScriptNames() throws IOException {
        // Arrange
        byte[] zipData = createZipWithScripts();
        String fileFormat = "zip";

        // Act
        List<String> result = archiveParseService.extractScriptFileNames(zipData, fileFormat);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
        assertTrue(result.contains("TC001.py"));
        assertTrue(result.contains("TC002.py"));
    }

    /**
     * 测试提取脚本文件名列表 - ZIP格式，不包含脚本文件
     */
    @Test
    public void testExtractScriptFileNames_ZipWithoutScripts_ShouldReturnEmptyList() throws IOException {
        // Arrange
        byte[] zipData = createZipWithoutScripts();
        String fileFormat = "zip";

        // Act
        List<String> result = archiveParseService.extractScriptFileNames(zipData, fileFormat);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试验证压缩包结构 - 有效结构
     */
    @Test
    public void testValidateArchive_ValidStructure_ShouldReturnValidResult() throws IOException {
        // Arrange
        byte[] zipData = createValidZipArchive();
        String fileFormat = "zip";

        // Act
        ArchiveValidationResult result = archiveParseService.validateArchive(zipData, fileFormat);

        // Assert
        assertNotNull(result);
        assertTrue(result.isHasCasesExcel());
        assertTrue(result.isHasScriptsDir());
        assertTrue(result.getScriptCount() > 0);
        assertTrue(result.isValid());
    }

    /**
     * 测试验证压缩包结构 - 缺少cases.xlsx
     */
    @Test
    public void testValidateArchive_MissingCasesXlsx_ShouldReturnInvalidResult() throws IOException {
        // Arrange
        byte[] zipData = createZipWithoutCasesXlsx();
        String fileFormat = "zip";

        // Act
        ArchiveValidationResult result = archiveParseService.validateArchive(zipData, fileFormat);

        // Assert
        assertNotNull(result);
        assertFalse(result.isHasCasesExcel());
        assertFalse(result.isValid());
    }

    /**
     * 测试验证压缩包结构 - 缺少scripts目录
     */
    @Test
    public void testValidateArchive_MissingScriptsDir_ShouldReturnInvalidResult() throws IOException {
        // Arrange
        byte[] zipData = createZipWithoutScripts();
        String fileFormat = "zip";

        // Act
        ArchiveValidationResult result = archiveParseService.validateArchive(zipData, fileFormat);

        // Assert
        assertNotNull(result);
        assertTrue(result.isHasCasesExcel());
        assertFalse(result.isHasScriptsDir());
        assertFalse(result.isValid());
    }

    /**
     * 测试不支持的文件格式
     */
    @Test(expected = IllegalArgumentException.class)
    public void testExtractCasesExcel_UnsupportedFormat_ShouldThrowException() throws IOException {
        // Arrange
        byte[] data = new byte[]{1, 2, 3, 4};
        String fileFormat = "rar";

        // Act
        archiveParseService.extractCasesExcel(data, fileFormat);

        // Assert - 期望抛出异常
    }

    /**
     * 创建包含cases.xlsx的ZIP文件
     */
    private byte[] createZipWithCasesXlsx() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // 添加cases.xlsx文件
            ZipEntry entry = new ZipEntry("cases.xlsx");
            zos.putNextEntry(entry);
            byte[] content = "Excel content".getBytes();
            zos.write(content);
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    /**
     * 创建不包含cases.xlsx的ZIP文件
     */
    private byte[] createZipWithoutCasesXlsx() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // 添加其他文件
            ZipEntry entry = new ZipEntry("other.xlsx");
            zos.putNextEntry(entry);
            zos.write("Other content".getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    /**
     * 创建包含脚本文件的ZIP文件
     */
    private byte[] createZipWithScripts() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // 添加脚本文件
            ZipEntry entry1 = new ZipEntry("scripts/TC001.py");
            zos.putNextEntry(entry1);
            zos.write("print('TC001')".getBytes());
            zos.closeEntry();

            ZipEntry entry2 = new ZipEntry("scripts/TC002.py");
            zos.putNextEntry(entry2);
            zos.write("print('TC002')".getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    /**
     * 创建不包含脚本文件的ZIP文件
     */
    private byte[] createZipWithoutScripts() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // 添加cases.xlsx文件
            ZipEntry entry = new ZipEntry("cases.xlsx");
            zos.putNextEntry(entry);
            zos.write("Excel content".getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    /**
     * 创建有效的ZIP压缩包（包含cases.xlsx和scripts目录）
     */
    private byte[] createValidZipArchive() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // 添加cases.xlsx文件
            ZipEntry excelEntry = new ZipEntry("cases.xlsx");
            zos.putNextEntry(excelEntry);
            zos.write("Excel content".getBytes());
            zos.closeEntry();

            // 添加脚本文件
            ZipEntry scriptEntry = new ZipEntry("scripts/TC001.py");
            zos.putNextEntry(scriptEntry);
            zos.write("print('TC001')".getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
}

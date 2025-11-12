/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

/**
 * ArchiveParseService 单元测试
 * 
 * @author g00940940
 * @since 2025-01-27
 */
@RunWith(MockitoJUnitRunner.class)
public class ArchiveParseServiceTest {

    @InjectMocks
    private ArchiveParseService archiveParseService;

    private byte[] testZipData;

    @Before
    public void setUp() throws IOException {
        // 创建测试ZIP数据
        testZipData = createTestZipData();
    }

    /**
     * 测试解析ZIP压缩包 - 成功场景
     */
    @Test
    public void testParseArchive_Success_ReturnsArchiveParseResult() {
        // Act
        ArchiveParseResult result = archiveParseService.parseArchive(testZipData);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getExcelData());
        assertNotNull(result.getScriptFileNames());
        assertEquals(2, result.getScriptFileNames().size());
        assertTrue(result.getScriptFileNames().contains("TC001.py"));
        assertTrue(result.getScriptFileNames().contains("TC002.py"));
    }

    /**
     * 测试解析ZIP压缩包 - 包含cases.xlsx文件
     */
    @Test
    public void testParseArchive_WithExcelFile_ReturnsExcelData() {
        // Act
        ArchiveParseResult result = archiveParseService.parseArchive(testZipData);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getExcelData());
        assertTrue(result.getExcelData().length > 0);
    }

    /**
     * 测试解析ZIP压缩包 - 包含Python脚本文件
     */
    @Test
    public void testParseArchive_WithPythonScripts_ReturnsScriptFileNames() {
        // Act
        ArchiveParseResult result = archiveParseService.parseArchive(testZipData);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getScriptFileNames());
        assertEquals(2, result.getScriptFileNames().size());
        
        List<String> expectedScripts = Arrays.asList("TC001.py", "TC002.py");
        for (String expectedScript : expectedScripts) {
            assertTrue("Should contain script: " + expectedScript, 
                      result.getScriptFileNames().contains(expectedScript));
        }
    }

    /**
     * 测试解析ZIP压缩包 - 空ZIP文件
     */
    @Test
    public void testParseArchive_EmptyZip_ReturnsEmptyResult() throws IOException {
        // Arrange
        byte[] emptyZipData = createEmptyZipData();

        // Act
        ArchiveParseResult result = archiveParseService.parseArchive(emptyZipData);

        // Assert
        assertNotNull(result);
        assertNull(result.getExcelData());
        assertNotNull(result.getScriptFileNames());
        assertEquals(0, result.getScriptFileNames().size());
    }

    /**
     * 测试解析ZIP压缩包 - 只有Excel文件，没有脚本文件
     */
    @Test
    public void testParseArchive_OnlyExcelFile_ReturnsExcelDataOnly() throws IOException {
        // Arrange
        byte[] zipWithOnlyExcel = createZipWithOnlyExcel();

        // Act
        ArchiveParseResult result = archiveParseService.parseArchive(zipWithOnlyExcel);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getExcelData());
        assertTrue(result.getExcelData().length > 0);
        assertNotNull(result.getScriptFileNames());
        assertEquals(0, result.getScriptFileNames().size());
    }

    /**
     * 测试解析ZIP压缩包 - 只有脚本文件，没有Excel文件
     */
    @Test
    public void testParseArchive_OnlyScriptFiles_ReturnsScriptFileNamesOnly() throws IOException {
        // Arrange
        byte[] zipWithOnlyScripts = createZipWithOnlyScripts();

        // Act
        ArchiveParseResult result = archiveParseService.parseArchive(zipWithOnlyScripts);

        // Assert
        assertNotNull(result);
        assertNull(result.getExcelData());
        assertNotNull(result.getScriptFileNames());
        assertEquals(1, result.getScriptFileNames().size());
        assertTrue(result.getScriptFileNames().contains("TC001.py"));
    }

    /**
     * 测试解析ZIP压缩包 - 无效ZIP数据
     */
    @Test
    public void testParseArchive_InvalidZipData_ReturnsEmptyResult() {
        // Arrange
        byte[] invalidZipData = "invalid zip data".getBytes();

        // Act
        ArchiveParseResult result = archiveParseService.parseArchive(invalidZipData);

        // Assert
        assertNotNull(result);
        assertNull(result.getExcelData());
        assertNotNull(result.getScriptFileNames());
        assertEquals(0, result.getScriptFileNames().size());
    }

    /**
     * 测试解析ZIP压缩包 - 空数据
     */
    @Test
    public void testParseArchive_EmptyData_ReturnsEmptyResult() {
        // Arrange
        byte[] emptyData = new byte[0];

        // Act
        ArchiveParseResult result = archiveParseService.parseArchive(emptyData);

        // Assert
        assertNotNull(result);
        assertNull(result.getExcelData());
        assertNotNull(result.getScriptFileNames());
        assertEquals(0, result.getScriptFileNames().size());
    }

    /**
     * 测试解析ZIP压缩包 - null数据
     */
    @Test(expected = RuntimeException.class)
    public void testParseArchive_NullData_ThrowsRuntimeException() {
        // Act
        archiveParseService.parseArchive(null);
    }

    /**
     * 测试解析ZIP压缩包 - 包含非scripts目录的Python文件
     */
    @Test
    public void testParseArchive_PythonFileOutsideScripts_IgnoresFile() throws IOException {
        // Arrange
        byte[] zipWithPythonOutsideScripts = createZipWithPythonOutsideScripts();

        // Act
        ArchiveParseResult result = archiveParseService.parseArchive(zipWithPythonOutsideScripts);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getScriptFileNames());
        assertEquals(0, result.getScriptFileNames().size());
    }

    /**
     * 测试解析ZIP压缩包 - 包含子目录中的脚本文件
     */
    @Test
    public void testParseArchive_ScriptInSubdirectory_ReturnsScriptFileName() throws IOException {
        // Arrange
        byte[] zipWithScriptInSubdirectory = createZipWithScriptInSubdirectory();

        // Act
        ArchiveParseResult result = archiveParseService.parseArchive(zipWithScriptInSubdirectory);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getScriptFileNames());
        assertEquals(1, result.getScriptFileNames().size());
        assertTrue(result.getScriptFileNames().contains("TC003.py"));
    }

    // 辅助方法

    /**
     * 创建测试ZIP数据
     */
    private byte[] createTestZipData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // 添加cases.xlsx文件
            ZipEntry excelEntry = new ZipEntry("cases.xlsx");
            zos.putNextEntry(excelEntry);
            zos.write("excel content".getBytes());
            zos.closeEntry();

            // 添加脚本文件
            ZipEntry script1Entry = new ZipEntry("scripts/TC001.py");
            zos.putNextEntry(script1Entry);
            zos.write("print('TC001')".getBytes());
            zos.closeEntry();

            ZipEntry script2Entry = new ZipEntry("scripts/TC002.py");
            zos.putNextEntry(script2Entry);
            zos.write("print('TC002')".getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    /**
     * 创建空ZIP数据
     */
    private byte[] createEmptyZipData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // 空的ZIP文件
        }
        return baos.toByteArray();
    }

    /**
     * 创建只包含Excel文件的ZIP数据
     */
    private byte[] createZipWithOnlyExcel() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry excelEntry = new ZipEntry("cases.xlsx");
            zos.putNextEntry(excelEntry);
            zos.write("excel content only".getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    /**
     * 创建只包含脚本文件的ZIP数据
     */
    private byte[] createZipWithOnlyScripts() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry scriptEntry = new ZipEntry("scripts/TC001.py");
            zos.putNextEntry(scriptEntry);
            zos.write("print('TC001')".getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    /**
     * 创建包含scripts目录外Python文件的ZIP数据
     */
    private byte[] createZipWithPythonOutsideScripts() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry scriptEntry = new ZipEntry("other/TC001.py");
            zos.putNextEntry(scriptEntry);
            zos.write("print('TC001')".getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    /**
     * 创建包含子目录中脚本文件的ZIP数据
     */
    private byte[] createZipWithScriptInSubdirectory() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry scriptEntry = new ZipEntry("scripts/subdir/TC003.py");
            zos.putNextEntry(scriptEntry);
            zos.write("print('TC003')".getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
}

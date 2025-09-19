/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.util;

import com.huawei.cloududn.dialingtest.model.OperationLog;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Excel工具类测试
 *
 * @author g00940940
 * @since 2025-09-19
 */
public class ExcelUtilTest {

    @Test
    public void testGenerateOperationLogsExcel_Success_ReturnsResource() throws IOException {
        // Arrange
        OperationLog log1 = new OperationLog();
        log1.setId(1);
        log1.setUsername("user1");
        log1.setOperationType("CREATE");
        log1.setOperationTarget("USER");
        log1.setOperationDescriptionZh("创建用户");
        log1.setOperationDescriptionEn("Create user");

        OperationLog log2 = new OperationLog();
        log2.setId(2);
        log2.setUsername("user2");
        log2.setOperationType("UPDATE");
        log2.setOperationTarget("USER");
        log2.setOperationDescriptionZh("更新用户");
        log2.setOperationDescriptionEn("Update user");

        List<OperationLog> logs = Arrays.asList(log1, log2);

        // Act
        Resource resource = ExcelUtil.generateOperationLogsExcel(logs);

        // Assert
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.contentLength() > 0);

        // 验证Excel内容
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = resource.getInputStream().read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] excelData = baos.toByteArray();
            
            try (ByteArrayInputStream bais = new ByteArrayInputStream(excelData);
                 Workbook workbook = new XSSFWorkbook(bais)) {
                
                Sheet sheet = workbook.getSheetAt(0);
                assertNotNull(sheet);
                
                // 验证标题行
                Row headerRow = sheet.getRow(0);
                assertNotNull(headerRow);
                assertEquals("ID", headerRow.getCell(0).getStringCellValue());
                assertEquals("操作人", headerRow.getCell(1).getStringCellValue());
                assertEquals("操作类型", headerRow.getCell(2).getStringCellValue());
                assertEquals("操作目标", headerRow.getCell(3).getStringCellValue());
                assertEquals("操作描述(中文)", headerRow.getCell(4).getStringCellValue());
                assertEquals("操作描述(英文)", headerRow.getCell(5).getStringCellValue());
                
                // 验证数据行
                Row dataRow1 = sheet.getRow(1);
                assertNotNull(dataRow1);
                assertEquals(1.0, dataRow1.getCell(0).getNumericCellValue(), 0.01);
                assertEquals("user1", dataRow1.getCell(1).getStringCellValue());
                assertEquals("CREATE", dataRow1.getCell(2).getStringCellValue());
                assertEquals("USER", dataRow1.getCell(3).getStringCellValue());
                assertEquals("创建用户", dataRow1.getCell(4).getStringCellValue());
                assertEquals("Create user", dataRow1.getCell(5).getStringCellValue());
            }
        }
    }

    @Test
    public void testGenerateOperationLogsExcel_EmptyList_ReturnsResource() throws IOException {
        // Arrange
        List<OperationLog> emptyLogs = Arrays.asList();

        // Act
        Resource resource = ExcelUtil.generateOperationLogsExcel(emptyLogs);

        // Assert
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.contentLength() > 0);

        // 验证Excel内容（只有标题行）
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = resource.getInputStream().read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] excelData = baos.toByteArray();
            
            try (ByteArrayInputStream bais = new ByteArrayInputStream(excelData);
                 Workbook workbook = new XSSFWorkbook(bais)) {
                
                Sheet sheet = workbook.getSheetAt(0);
                assertNotNull(sheet);
                
                // 验证只有标题行
                Row headerRow = sheet.getRow(0);
                assertNotNull(headerRow);
                assertEquals("ID", headerRow.getCell(0).getStringCellValue());
                
                // 验证没有数据行
                Row dataRow = sheet.getRow(1);
                assertNull(dataRow);
            }
        }
    }

    @Test
    public void testGenerateOperationLogsExcel_NullList_ReturnsResource() throws IOException {
        // Arrange
        List<OperationLog> nullLogs = null;

        // Act
        Resource resource = ExcelUtil.generateOperationLogsExcel(nullLogs);

        // Assert
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.contentLength() > 0);

        // 验证Excel内容（只有标题行）
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = resource.getInputStream().read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] excelData = baos.toByteArray();
            
            try (ByteArrayInputStream bais = new ByteArrayInputStream(excelData);
                 Workbook workbook = new XSSFWorkbook(bais)) {
                
                Sheet sheet = workbook.getSheetAt(0);
                assertNotNull(sheet);
                
                // 验证只有标题行
                Row headerRow = sheet.getRow(0);
                assertNotNull(headerRow);
                assertEquals("ID", headerRow.getCell(0).getStringCellValue());
                
                // 验证没有数据行
                Row dataRow = sheet.getRow(1);
                assertNull(dataRow);
            }
        }
    }

    @Test
    public void testGenerateOperationLogsExcel_LargeDataset_ReturnsResource() throws IOException {
        // Arrange
        List<OperationLog> largeLogs = Arrays.asList();
        for (int i = 1; i <= 1000; i++) {
            OperationLog log = new OperationLog();
            log.setId(i);
            log.setUsername("user" + i);
            log.setOperationType("CREATE");
            log.setOperationTarget("USER");
            log.setOperationDescriptionZh("创建用户" + i);
            log.setOperationDescriptionEn("Create user" + i);
            largeLogs.add(log);
        }

        // Act
        Resource resource = ExcelUtil.generateOperationLogsExcel(largeLogs);

        // Assert
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.contentLength() > 0);

        // 验证Excel内容
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = resource.getInputStream().read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] excelData = baos.toByteArray();
            
            try (ByteArrayInputStream bais = new ByteArrayInputStream(excelData);
                 Workbook workbook = new XSSFWorkbook(bais)) {
                
                Sheet sheet = workbook.getSheetAt(0);
                assertNotNull(sheet);
                
                // 验证标题行
                Row headerRow = sheet.getRow(0);
                assertNotNull(headerRow);
                assertEquals("ID", headerRow.getCell(0).getStringCellValue());
                
                // 验证数据行数量
                assertEquals(1001, sheet.getLastRowNum() + 1); // 标题行 + 1000数据行
                
                // 验证第一行数据
                Row firstDataRow = sheet.getRow(1);
                assertNotNull(firstDataRow);
                assertEquals(1.0, firstDataRow.getCell(0).getNumericCellValue(), 0.01);
                assertEquals("user1", firstDataRow.getCell(1).getStringCellValue());
                
                // 验证最后一行数据
                Row lastDataRow = sheet.getRow(1000);
                assertNotNull(lastDataRow);
                assertEquals(1000.0, lastDataRow.getCell(0).getNumericCellValue(), 0.01);
                assertEquals("user1000", lastDataRow.getCell(1).getStringCellValue());
            }
        }
    }
}
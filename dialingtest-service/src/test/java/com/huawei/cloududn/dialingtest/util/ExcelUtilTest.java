/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.util;

import com.huawei.cloududn.dialingtest.model.OperationLog;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
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

    private OperationLog testOperationLog1;
    private OperationLog testOperationLog2;

    @Before
    public void setUp() {
        testOperationLog1 = new OperationLog();
        testOperationLog1.setId(1);
        testOperationLog1.setUsername("user1");
        testOperationLog1.setOperationType("CREATE");
        testOperationLog1.setOperationTarget("USER");
        testOperationLog1.setOperationDescriptionZh("创建用户: user1");
        testOperationLog1.setOperationDescriptionEn("Create user: user1");
        testOperationLog1.setOperationTime("2025-09-19T10:00:00Z");

        testOperationLog2 = new OperationLog();
        testOperationLog2.setId(2);
        testOperationLog2.setUsername("user2");
        testOperationLog2.setOperationType("UPDATE");
        testOperationLog2.setOperationTarget("USER");
        testOperationLog2.setOperationDescriptionZh("更新用户: user2");
        testOperationLog2.setOperationDescriptionEn("Update user: user2");
        testOperationLog2.setOperationTime("2025-09-19T11:00:00Z");
    }

    @Test
    public void testGenerateOperationLogsExcel_Success_ReturnsValidExcel() throws IOException {
        // Arrange
        List<OperationLog> operationLogs = Arrays.asList(testOperationLog1, testOperationLog2);

        // Act
        byte[] excelData = ExcelUtil.generateOperationLogsExcel(operationLogs);

        // Assert
        assertNotNull(excelData);
        assertTrue(excelData.length > 0);

        // Verify Excel content
        try (ByteArrayInputStream bis = new ByteArrayInputStream(excelData);
             Workbook workbook = new XSSFWorkbook(bis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            assertNotNull(sheet);
            
            // Check header row
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            assertEquals("ID", getCellValueAsString(headerRow.getCell(0)));
            assertEquals("用户名", getCellValueAsString(headerRow.getCell(1)));
            assertEquals("操作类型", getCellValueAsString(headerRow.getCell(2)));
            assertEquals("操作目标", getCellValueAsString(headerRow.getCell(3)));
            assertEquals("中文描述", getCellValueAsString(headerRow.getCell(4)));
            assertEquals("英文描述", getCellValueAsString(headerRow.getCell(5)));
            assertEquals("操作时间", getCellValueAsString(headerRow.getCell(6)));
            
            // Check data rows
            Row dataRow1 = sheet.getRow(1);
            assertNotNull(dataRow1);
            assertEquals("1", getCellValueAsString(dataRow1.getCell(0)));
            assertEquals("user1", getCellValueAsString(dataRow1.getCell(1)));
            assertEquals("CREATE", getCellValueAsString(dataRow1.getCell(2)));
            assertEquals("USER", getCellValueAsString(dataRow1.getCell(3)));
            assertEquals("创建用户: user1", getCellValueAsString(dataRow1.getCell(4)));
            assertEquals("Create user: user1", getCellValueAsString(dataRow1.getCell(5)));
            assertEquals("2025-09-19T10:00:00Z", getCellValueAsString(dataRow1.getCell(6)));
            
            Row dataRow2 = sheet.getRow(2);
            assertNotNull(dataRow2);
            assertEquals("2", getCellValueAsString(dataRow2.getCell(0)));
            assertEquals("user2", getCellValueAsString(dataRow2.getCell(1)));
            assertEquals("UPDATE", getCellValueAsString(dataRow2.getCell(2)));
            assertEquals("USER", getCellValueAsString(dataRow2.getCell(3)));
            assertEquals("更新用户: user2", getCellValueAsString(dataRow2.getCell(4)));
            assertEquals("Update user: user2", getCellValueAsString(dataRow2.getCell(5)));
            assertEquals("2025-09-19T11:00:00Z", getCellValueAsString(dataRow2.getCell(6)));
        }
    }

    @Test
    public void testGenerateOperationLogsExcel_EmptyList_ReturnsExcelWithHeadersOnly() throws IOException {
        // Arrange
        List<OperationLog> emptyList = Arrays.asList();

        // Act
        byte[] excelData = ExcelUtil.generateOperationLogsExcel(emptyList);

        // Assert
        assertNotNull(excelData);
        assertTrue(excelData.length > 0);

        // Verify Excel content
        try (ByteArrayInputStream bis = new ByteArrayInputStream(excelData);
             Workbook workbook = new XSSFWorkbook(bis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            assertNotNull(sheet);
            
            // Check header row exists
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            assertEquals("ID", getCellValueAsString(headerRow.getCell(0)));
            
            // Check no data rows
            Row dataRow = sheet.getRow(1);
            assertNull(dataRow);
        }
    }

    @Test
    public void testGenerateOperationLogsExcel_NullList_ReturnsExcelWithHeadersOnly() throws IOException {
        // Arrange
        List<OperationLog> nullList = null;

        // Act
        byte[] excelData = ExcelUtil.generateOperationLogsExcel(nullList);

        // Assert
        assertNotNull(excelData);
        assertTrue(excelData.length > 0);

        // Verify Excel content
        try (ByteArrayInputStream bis = new ByteArrayInputStream(excelData);
             Workbook workbook = new XSSFWorkbook(bis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            assertNotNull(sheet);
            
            // Check header row exists
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            assertEquals("ID", getCellValueAsString(headerRow.getCell(0)));
            
            // Check no data rows
            Row dataRow = sheet.getRow(1);
            assertNull(dataRow);
        }
    }

    @Test
    public void testGenerateOperationLogsExcel_NullValues_HandlesNullValuesCorrectly() throws IOException {
        // Arrange
        OperationLog logWithNulls = new OperationLog();
        logWithNulls.setId(1);
        logWithNulls.setUsername("user1");
        logWithNulls.setOperationType("CREATE");
        logWithNulls.setOperationTarget("USER");
        logWithNulls.setOperationDescriptionZh(null);
        logWithNulls.setOperationDescriptionEn(null);
        logWithNulls.setOperationTime(null);
        
        List<OperationLog> operationLogs = Arrays.asList(logWithNulls);

        // Act
        byte[] excelData = ExcelUtil.generateOperationLogsExcel(operationLogs);

        // Assert
        assertNotNull(excelData);
        assertTrue(excelData.length > 0);

        // Verify Excel content
        try (ByteArrayInputStream bis = new ByteArrayInputStream(excelData);
             Workbook workbook = new XSSFWorkbook(bis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            assertNotNull(sheet);
            
            Row dataRow = sheet.getRow(1);
            assertNotNull(dataRow);
            assertEquals("1", getCellValueAsString(dataRow.getCell(0)));
            assertEquals("user1", getCellValueAsString(dataRow.getCell(1)));
            assertEquals("CREATE", getCellValueAsString(dataRow.getCell(2)));
            assertEquals("USER", getCellValueAsString(dataRow.getCell(3)));
            assertEquals("", getCellValueAsString(dataRow.getCell(4))); // null description
            assertEquals("", getCellValueAsString(dataRow.getCell(5))); // null description
            assertEquals("", getCellValueAsString(dataRow.getCell(6))); // null time
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}

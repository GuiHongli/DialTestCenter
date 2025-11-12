/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service;

import com.huawei.cloududn.dialingtest.model.TestCaseInfo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * ExcelParseService 单元测试
 * 
 * @author g00940940
 * @since 2025-01-27
 */
@RunWith(MockitoJUnitRunner.class)
public class ExcelParseServiceTest {

    @InjectMocks
    private ExcelParseService excelParseService;

    private byte[] testExcelData;

    @Before
    public void setUp() throws IOException {
        // 创建测试Excel数据
        testExcelData = createTestExcelData();
    }

    /**
     * 测试解析Excel文件 - 成功场景
     */
    @Test
    public void testParseExcel_Success_ReturnsTestCaseList() {
        // Act
        List<TestCaseInfo> result = excelParseService.parseExcel(testExcelData);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        TestCaseInfo firstCase = result.get(0);
        assertEquals("测试用例1", firstCase.getCaseName());
        assertEquals("TC001", firstCase.getCaseNumber());
        assertEquals("测试步骤1", firstCase.getTestSteps());
        assertEquals("预期结果1", firstCase.getExpectedResult());
        assertEquals("VPN", firstCase.getBusinessCategory());
        assertEquals("TestApp", firstCase.getAppName());
        assertEquals("package1.apk", firstCase.getDependenciesPackage());
        assertEquals("rule1", firstCase.getDependenciesRule());
        assertEquals("{\"env\":\"test\"}", firstCase.getEnvironmentConfig());
    }

    /**
     * 测试解析Excel文件 - 包含多个测试用例
     */
    @Test
    public void testParseExcel_MultipleTestCases_ReturnsAllTestCases() {
        // Act
        List<TestCaseInfo> result = excelParseService.parseExcel(testExcelData);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        TestCaseInfo secondCase = result.get(1);
        assertEquals("测试用例2", secondCase.getCaseName());
        assertEquals("TC002", secondCase.getCaseNumber());
        assertEquals("测试步骤2", secondCase.getTestSteps());
        assertEquals("预期结果2", secondCase.getExpectedResult());
        assertEquals("VoLTE", secondCase.getBusinessCategory());
        assertEquals("TestApp2", secondCase.getAppName());
    }

    /**
     * 测试解析Excel文件 - 空Excel文件
     */
    @Test(expected = RuntimeException.class)
    public void testParseExcel_EmptyExcel_ThrowsRuntimeException() throws IOException {
        // Arrange
        byte[] emptyExcelData = createEmptyExcelData();

        // Act
        excelParseService.parseExcel(emptyExcelData);
    }

    /**
     * 测试解析Excel文件 - 缺少表头
     */
    @Test(expected = RuntimeException.class)
    public void testParseExcel_MissingHeader_ThrowsRuntimeException() throws IOException {
        // Arrange
        byte[] excelWithoutHeader = createExcelWithoutHeader();

        // Act
        excelParseService.parseExcel(excelWithoutHeader);
    }

    /**
     * 测试解析Excel文件 - 只有表头没有数据
     */
    @Test
    public void testParseExcel_OnlyHeader_ReturnsEmptyList() throws IOException {
        // Arrange
        byte[] excelWithOnlyHeader = createExcelWithOnlyHeader();

        // Act
        List<TestCaseInfo> result = excelParseService.parseExcel(excelWithOnlyHeader);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * 测试解析Excel文件 - 包含空行
     */
    @Test
    public void testParseExcel_WithEmptyRows_IgnoresEmptyRows() throws IOException {
        // Arrange
        byte[] excelWithEmptyRows = createExcelWithEmptyRows();

        // Act
        List<TestCaseInfo> result = excelParseService.parseExcel(excelWithEmptyRows);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size()); // 只有一行有效数据
        assertEquals("TC001", result.get(0).getCaseNumber());
    }

    /**
     * 测试解析Excel文件 - 包含部分空字段
     */
    @Test
    public void testParseExcel_WithPartialEmptyFields_HandlesNullValues() throws IOException {
        // Arrange
        byte[] excelWithPartialEmptyFields = createExcelWithPartialEmptyFields();

        // Act
        List<TestCaseInfo> result = excelParseService.parseExcel(excelWithPartialEmptyFields);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        TestCaseInfo testCase = result.get(0);
        assertEquals("TC001", testCase.getCaseNumber());
        assertNull(testCase.getDependenciesPackage());
        assertNull(testCase.getDependenciesRule());
        assertNull(testCase.getEnvironmentConfig());
    }

    /**
     * 测试解析Excel文件 - 无效Excel数据
     */
    @Test(expected = RuntimeException.class)
    public void testParseExcel_InvalidExcelData_ThrowsRuntimeException() {
        // Arrange
        byte[] invalidExcelData = "invalid excel data".getBytes();

        // Act
        excelParseService.parseExcel(invalidExcelData);
    }

    /**
     * 测试解析Excel文件 - null数据
     */
    @Test(expected = RuntimeException.class)
    public void testParseExcel_NullData_ThrowsRuntimeException() {
        // Act
        excelParseService.parseExcel(null);
    }

    /**
     * 测试解析Excel文件 - 包含数字类型数据
     */
    @Test
    public void testParseExcel_WithNumericData_ConvertsToString() throws IOException {
        // Arrange
        byte[] excelWithNumericData = createExcelWithNumericData();

        // Act
        List<TestCaseInfo> result = excelParseService.parseExcel(excelWithNumericData);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        TestCaseInfo testCase = result.get(0);
        assertEquals("123", testCase.getCaseNumber()); // 数字转换为字符串
    }

    /**
     * 测试解析Excel文件 - 包含布尔类型数据
     */
    @Test
    public void testParseExcel_WithBooleanData_ConvertsToString() throws IOException {
        // Arrange
        byte[] excelWithBooleanData = createExcelWithBooleanData();

        // Act
        List<TestCaseInfo> result = excelParseService.parseExcel(excelWithBooleanData);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        TestCaseInfo testCase = result.get(0);
        assertEquals("true", testCase.getDependenciesRule()); // 布尔值转换为字符串
    }

    // 辅助方法

    /**
     * 创建测试Excel数据
     */
    private byte[] createTestExcelData() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "用例_名称", "用例_编号", "用例_测试步骤", "用例_预期结果", 
            "用例_业务大类", "用例_App", "依赖软件包", "依赖规则", "环境配置"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
        
        // 创建第一行数据
        Row dataRow1 = sheet.createRow(1);
        dataRow1.createCell(0).setCellValue("测试用例1");
        dataRow1.createCell(1).setCellValue("TC001");
        dataRow1.createCell(2).setCellValue("测试步骤1");
        dataRow1.createCell(3).setCellValue("预期结果1");
        dataRow1.createCell(4).setCellValue("VPN");
        dataRow1.createCell(5).setCellValue("TestApp");
        dataRow1.createCell(6).setCellValue("package1.apk");
        dataRow1.createCell(7).setCellValue("rule1");
        dataRow1.createCell(8).setCellValue("{\"env\":\"test\"}");
        
        // 创建第二行数据
        Row dataRow2 = sheet.createRow(2);
        dataRow2.createCell(0).setCellValue("测试用例2");
        dataRow2.createCell(1).setCellValue("TC002");
        dataRow2.createCell(2).setCellValue("测试步骤2");
        dataRow2.createCell(3).setCellValue("预期结果2");
        dataRow2.createCell(4).setCellValue("VoLTE");
        dataRow2.createCell(5).setCellValue("TestApp2");
        dataRow2.createCell(6).setCellValue("package2.apk");
        dataRow2.createCell(7).setCellValue("rule2");
        dataRow2.createCell(8).setCellValue("{\"env\":\"prod\"}");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        
        return baos.toByteArray();
    }

    /**
     * 创建空Excel数据
     */
    private byte[] createEmptyExcelData() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        workbook.createSheet("Empty");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        
        return baos.toByteArray();
    }

    /**
     * 创建没有表头的Excel数据
     */
    private byte[] createExcelWithoutHeader() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("No Header");
        
        // 创建空的工作表，没有表头行
        // 不创建任何行，这样sheet.getRow(0)会返回null
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        
        return baos.toByteArray();
    }

    /**
     * 创建只有表头的Excel数据
     */
    private byte[] createExcelWithOnlyHeader() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Header Only");
        
        // 只创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("用例_名称");
        headerRow.createCell(1).setCellValue("用例_编号");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        
        return baos.toByteArray();
    }

    /**
     * 创建包含空行的Excel数据
     */
    private byte[] createExcelWithEmptyRows() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("With Empty Rows");
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("用例_名称");
        headerRow.createCell(1).setCellValue("用例_编号");
        
        // 创建空行（第1行）
        // 第2行为空
        
        // 创建有效数据行（第3行）
        Row dataRow = sheet.createRow(2);
        dataRow.createCell(0).setCellValue("测试用例1");
        dataRow.createCell(1).setCellValue("TC001");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        
        return baos.toByteArray();
    }

    /**
     * 创建包含部分空字段的Excel数据
     */
    private byte[] createExcelWithPartialEmptyFields() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Partial Empty");
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "用例_名称", "用例_编号", "用例_测试步骤", "用例_预期结果", 
            "用例_业务大类", "用例_App", "依赖软件包", "依赖规则", "环境配置"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
        
        // 创建数据行，部分字段为空
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("测试用例1");
        dataRow.createCell(1).setCellValue("TC001");
        dataRow.createCell(2).setCellValue("测试步骤1");
        dataRow.createCell(3).setCellValue("预期结果1");
        dataRow.createCell(4).setCellValue("VPN");
        dataRow.createCell(5).setCellValue("TestApp");
        // 依赖软件包、依赖规则、环境配置为空
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        
        return baos.toByteArray();
    }

    /**
     * 创建包含数字类型数据的Excel数据
     */
    private byte[] createExcelWithNumericData() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Numeric Data");
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("用例_名称");
        headerRow.createCell(1).setCellValue("用例_编号");
        
        // 创建包含数字的数据行
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("测试用例1");
        dataRow.createCell(1).setCellValue(123); // 数字类型
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        
        return baos.toByteArray();
    }

    /**
     * 创建包含布尔类型数据的Excel数据
     */
    private byte[] createExcelWithBooleanData() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Boolean Data");
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("用例_名称");
        headerRow.createCell(1).setCellValue("依赖规则");
        
        // 创建包含布尔值的数据行
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("测试用例1");
        dataRow.createCell(1).setCellValue(true); // 布尔类型
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        
        return baos.toByteArray();
    }
}

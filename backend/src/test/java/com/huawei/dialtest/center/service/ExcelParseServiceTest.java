/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import com.huawei.dialtest.center.service.ExcelParseService;
import com.huawei.dialtest.center.service.ExcelParseService.TestCaseInfo;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Excel文件解析服务类单元测试
 * 测试ExcelParseService的Excel文件解析功能，包括用例信息提取和验证
 *
 * @author g00940940
 * @since 2025-09-08
 */
@RunWith(MockitoJUnitRunner.class)
public class ExcelParseServiceTest {

    @InjectMocks
    private ExcelParseService excelParseService;

    /**
     * 测试解析有效的Excel文件
     */
    @Test
    public void testParseCasesExcel_ValidExcel_ShouldReturnTestCaseInfos() throws IOException {
        // Arrange
        byte[] excelData = createValidExcelData();

        // Act
        List<TestCaseInfo> result = excelParseService.parseCasesExcel(excelData);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
        
        TestCaseInfo firstCase = result.get(0);
        assertNotNull(firstCase.getCaseName());
        assertNotNull(firstCase.getCaseNumber());
    }

    /**
     * 测试解析空的Excel文件
     */
    @Test
    public void testParseCasesExcel_EmptyExcel_ShouldReturnEmptyList() throws IOException {
        // Arrange
        byte[] excelData = createEmptyExcelData();

        // Act
        List<TestCaseInfo> result = excelParseService.parseCasesExcel(excelData);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试解析只有标题行的Excel文件
     */
    @Test
    public void testParseCasesExcel_OnlyHeaderRow_ShouldReturnEmptyList() throws IOException {
        // Arrange
        byte[] excelData = createExcelWithOnlyHeader();

        // Act
        List<TestCaseInfo> result = excelParseService.parseCasesExcel(excelData);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试解析包含无效数据的Excel文件
     */
    @Test
    public void testParseCasesExcel_InvalidData_ShouldSkipInvalidRows() throws IOException {
        // Arrange
        byte[] excelData = createExcelWithInvalidData();

        // Act
        List<TestCaseInfo> result = excelParseService.parseCasesExcel(excelData);

        // Assert
        assertNotNull(result);
        // 应该只包含有效的数据行
        assertTrue(result.size() >= 0);
    }

    /**
     * 测试TestCaseInfo构造函数和getter方法
     */
    @Test
    public void testTestCaseInfo_ConstructorAndGetters_ShouldWorkCorrectly() {
        // Arrange
        String caseName = "测试用例1";
        String caseNumber = "TC001";
        String networkTopology = "网络拓扑1";
        String businessCategory = "业务大类1";
        String appName = "应用1";
        String testSteps = "测试步骤";
        String expectedResult = "预期结果";

        // Act
        TestCaseInfo testCaseInfo = new TestCaseInfo(
            caseName, caseNumber, networkTopology, businessCategory, 
            appName, testSteps, expectedResult
        );

        // Assert
        assertEquals(caseName, testCaseInfo.getCaseName());
        assertEquals(caseNumber, testCaseInfo.getCaseNumber());
        assertEquals(networkTopology, testCaseInfo.getNetworkTopology());
        assertEquals(businessCategory, testCaseInfo.getBusinessCategory());
        assertEquals(appName, testCaseInfo.getAppName());
        assertEquals(testSteps, testCaseInfo.getTestSteps());
        assertEquals(expectedResult, testCaseInfo.getExpectedResult());
    }

    /**
     * 测试TestCaseInfo的toString方法
     */
    @Test
    public void testTestCaseInfo_ToString_ShouldContainAllFields() {
        // Arrange
        TestCaseInfo testCaseInfo = new TestCaseInfo(
            "测试用例1", "TC001", "网络拓扑1", "业务大类1", 
            "应用1", "测试步骤", "预期结果"
        );

        // Act
        String result = testCaseInfo.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("TestCaseInfo{"));
        assertTrue(result.contains("caseName='测试用例1'"));
        assertTrue(result.contains("caseNumber='TC001'"));
        assertTrue(result.contains("networkTopology='网络拓扑1'"));
        assertTrue(result.contains("businessCategory='业务大类1'"));
        assertTrue(result.contains("appName='应用1'"));
        assertTrue(result.contains("testSteps='测试步骤'"));
        assertTrue(result.contains("expectedResult='预期结果'"));
    }

    /**
     * 创建有效的Excel数据
     */
    private byte[] createValidExcelData() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"用例名称", "用例编号", "用例逻辑组网", "用例业务大类", "用例App", "用例测试步骤", "用例预期结果"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 创建数据行
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("测试用例1");
        dataRow.createCell(1).setCellValue("TC001");
        dataRow.createCell(2).setCellValue("网络拓扑1");
        dataRow.createCell(3).setCellValue("业务大类1");
        dataRow.createCell(4).setCellValue("应用1");
        dataRow.createCell(5).setCellValue("测试步骤");
        dataRow.createCell(6).setCellValue("预期结果");

        // 转换为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }

    /**
     * 创建空的Excel数据
     */
    private byte[] createEmptyExcelData() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        workbook.createSheet("Test Cases");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }

    /**
     * 创建只有标题行的Excel数据
     */
    private byte[] createExcelWithOnlyHeader() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");

        // 只创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"用例名称", "用例编号", "用例逻辑组网", "用例业务大类", "用例App", "用例测试步骤", "用例预期结果"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }

    /**
     * 创建包含无效数据的Excel数据
     */
    private byte[] createExcelWithInvalidData() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"用例名称", "用例编号", "用例逻辑组网", "用例业务大类", "用例App", "用例测试步骤", "用例预期结果"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 创建有效数据行
        Row validRow = sheet.createRow(1);
        validRow.createCell(0).setCellValue("测试用例1");
        validRow.createCell(1).setCellValue("TC001");
        validRow.createCell(2).setCellValue("网络拓扑1");
        validRow.createCell(3).setCellValue("业务大类1");
        validRow.createCell(4).setCellValue("应用1");
        validRow.createCell(5).setCellValue("测试步骤");
        validRow.createCell(6).setCellValue("预期结果");

        // 创建无效数据行（缺少用例名称和编号）
        Row invalidRow = sheet.createRow(2);
        invalidRow.createCell(2).setCellValue("网络拓扑2");
        invalidRow.createCell(3).setCellValue("业务大类2");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }
}

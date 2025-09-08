/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel文件解析服务类，用于解析cases.xlsx文件
 * 提取用例信息：用例名称、用例编号、用例逻辑组网、用例业务大类、用例App、用例测试步骤、用例预期结果
 * 支持Excel格式验证和数据提取
 *
 * @author g00940940
 * @since 2025-09-08
 */
@Service
public class ExcelParseService {
    private static final Logger logger = LoggerFactory.getLogger(ExcelParseService.class);

    // Excel列索引定义
    private static final int CASE_NAME_COLUMN = 0;        // 用例名称
    private static final int CASE_NUMBER_COLUMN = 1;      // 用例编号
    private static final int NETWORK_TOPOLOGY_COLUMN = 2; // 用例逻辑组网
    private static final int BUSINESS_CATEGORY_COLUMN = 3; // 用例业务大类
    private static final int APP_NAME_COLUMN = 4;         // 用例App
    private static final int TEST_STEPS_COLUMN = 5;       // 用例测试步骤
    private static final int EXPECTED_RESULT_COLUMN = 6;  // 用例预期结果

    /**
     * 解析Excel文件并提取用例信息
     *
     * @param excelData Excel文件字节数据
     * @return 用例信息列表
     * @throws IOException 解析过程中发生IO异常时抛出
     * @throws IllegalArgumentException 当Excel格式不正确时抛出
     */
    public List<TestCaseInfo> parseCasesExcel(byte[] excelData) throws IOException {
        logger.debug("Parsing cases.xlsx file, size: {} bytes", excelData.length);
        
        List<TestCaseInfo> testCases = new ArrayList<>();
        
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(excelData);
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("Excel file does not contain any sheets");
            }
            
            // 跳过标题行，从第二行开始读取数据
            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();
            
            logger.debug("Excel sheet has {} rows, starting from row {}", lastRowNum - firstRowNum + 1, firstRowNum);
            
            for (int rowIndex = firstRowNum + 1; rowIndex <= lastRowNum; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                
                TestCaseInfo testCaseInfo = parseTestCaseRow(row, rowIndex);
                if (testCaseInfo != null && isValidTestCase(testCaseInfo)) {
                    testCases.add(testCaseInfo);
                    logger.debug("Parsed test case: {}", testCaseInfo.getCaseNumber());
                }
            }
        }
        
        logger.info("Successfully parsed {} test cases from Excel file", testCases.size());
        return testCases;
    }

    /**
     * 解析单行用例数据
     *
     * @param row Excel行对象
     * @param rowIndex 行索引
     * @return 用例信息对象，如果行数据无效则返回null
     */
    private TestCaseInfo parseTestCaseRow(Row row, int rowIndex) {
        try {
            String caseName = getCellValueAsString(row.getCell(CASE_NAME_COLUMN));
            String caseNumber = getCellValueAsString(row.getCell(CASE_NUMBER_COLUMN));
            String networkTopology = getCellValueAsString(row.getCell(NETWORK_TOPOLOGY_COLUMN));
            String businessCategory = getCellValueAsString(row.getCell(BUSINESS_CATEGORY_COLUMN));
            String appName = getCellValueAsString(row.getCell(APP_NAME_COLUMN));
            String testSteps = getCellValueAsString(row.getCell(TEST_STEPS_COLUMN));
            String expectedResult = getCellValueAsString(row.getCell(EXPECTED_RESULT_COLUMN));
            
            return new TestCaseInfo(caseName, caseNumber, networkTopology, 
                                  businessCategory, appName, testSteps, expectedResult);
        } catch (Exception e) {
            logger.warn("Failed to parse row {}: {}", rowIndex, e.getMessage());
            return null;
        }
    }

    /**
     * 获取单元格值并转换为字符串
     *
     * @param cell Excel单元格对象
     * @return 单元格值字符串，如果单元格为空则返回空字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // 处理数字类型，避免科学计数法
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    return String.valueOf((long) numericValue);
                } else {
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    /**
     * 验证用例信息是否有效
     *
     * @param testCaseInfo 用例信息对象
     * @return 是否有效
     */
    private boolean isValidTestCase(TestCaseInfo testCaseInfo) {
        return testCaseInfo != null && 
               testCaseInfo.getCaseName() != null && !testCaseInfo.getCaseName().isEmpty() &&
               testCaseInfo.getCaseNumber() != null && !testCaseInfo.getCaseNumber().isEmpty();
    }

    /**
     * 用例信息数据传输对象
     */
    public static class TestCaseInfo {
        private final String caseName;
        private final String caseNumber;
        private final String networkTopology;
        private final String businessCategory;
        private final String appName;
        private final String testSteps;
        private final String expectedResult;

        public TestCaseInfo(String caseName, String caseNumber, String networkTopology,
                           String businessCategory, String appName, String testSteps, String expectedResult) {
            this.caseName = caseName;
            this.caseNumber = caseNumber;
            this.networkTopology = networkTopology;
            this.businessCategory = businessCategory;
            this.appName = appName;
            this.testSteps = testSteps;
            this.expectedResult = expectedResult;
        }

        public String getCaseName() {
            return caseName;
        }

        public String getCaseNumber() {
            return caseNumber;
        }

        public String getNetworkTopology() {
            return networkTopology;
        }

        public String getBusinessCategory() {
            return businessCategory;
        }

        public String getAppName() {
            return appName;
        }

        public String getTestSteps() {
            return testSteps;
        }

        public String getExpectedResult() {
            return expectedResult;
        }

        @Override
        public String toString() {
            return "TestCaseInfo{" +
                   "caseName='" + caseName + '\'' +
                   ", caseNumber='" + caseNumber + '\'' +
                   ", networkTopology='" + networkTopology + '\'' +
                   ", businessCategory='" + businessCategory + '\'' +
                   ", appName='" + appName + '\'' +
                   ", testSteps='" + testSteps + '\'' +
                   ", expectedResult='" + expectedResult + '\'' +
                   '}';
        }
    }
}

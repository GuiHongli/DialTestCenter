/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * ValidationResult 单元测试
 * 
 * @author g00940940
 * @since 2025-01-27
 */
public class ValidationResultTest {

    private ValidationResult validationResult;

    @Before
    public void setUp() {
        validationResult = new ValidationResult();
    }

    /**
     * 测试默认构造函数
     */
    @Test
    public void testConstructor_Default_CreatesEmptyObject() {
        // Arrange & Act
        ValidationResult result = new ValidationResult();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCaseResults());
        assertTrue(result.getCaseResults().isEmpty());
        assertEquals(0, result.getTotalCaseCount());
        assertEquals(0, result.getPassedCaseCount());
        assertEquals(0, result.getFailedCaseCount());
        assertEquals(0.0, result.getMatchRate(), 0.01);
    }

    /**
     * 测试设置和获取用例集ID
     */
    @Test
    public void testSetAndGetTestCaseSetId_ValidId_ReturnsId() {
        // Arrange
        Long testCaseSetId = 1L;

        // Act
        validationResult.setTestCaseSetId(testCaseSetId);

        // Assert
        assertEquals(testCaseSetId, validationResult.getTestCaseSetId());
    }

    /**
     * 测试设置和获取用例集名称
     */
    @Test
    public void testSetAndGetTestCaseSetName_ValidName_ReturnsName() {
        // Arrange
        String name = "测试用例集";

        // Act
        validationResult.setTestCaseSetName(name);

        // Assert
        assertEquals(name, validationResult.getTestCaseSetName());
    }

    /**
     * 测试设置和获取用例集版本
     */
    @Test
    public void testSetAndGetTestCaseSetVersion_ValidVersion_ReturnsVersion() {
        // Arrange
        String version = "v1.0";

        // Act
        validationResult.setTestCaseSetVersion(version);

        // Assert
        assertEquals(version, validationResult.getTestCaseSetVersion());
    }

    /**
     * 测试设置和获取业务中文名称
     */
    @Test
    public void testSetAndGetBusinessZh_ValidBusinessZh_ReturnsBusinessZh() {
        // Arrange
        String businessZh = "VPN阻断";

        // Act
        validationResult.setBusinessZh(businessZh);

        // Assert
        assertEquals(businessZh, validationResult.getBusinessZh());
    }

    /**
     * 测试设置和获取业务英文名称
     */
    @Test
    public void testSetAndGetBusinessEn_ValidBusinessEn_ReturnsBusinessEn() {
        // Arrange
        String businessEn = "VPN_BLOCK";

        // Act
        validationResult.setBusinessEn(businessEn);

        // Assert
        assertEquals(businessEn, validationResult.getBusinessEn());
    }

    /**
     * 测试设置和获取用例总数
     */
    @Test
    public void testSetAndGetTotalCaseCount_ValidCount_ReturnsCount() {
        // Arrange
        int count = 10;

        // Act
        validationResult.setTotalCaseCount(count);

        // Assert
        assertEquals(count, validationResult.getTotalCaseCount());
    }

    /**
     * 测试设置和获取通过用例数
     */
    @Test
    public void testSetAndGetPassedCaseCount_ValidCount_ReturnsCount() {
        // Arrange
        int count = 8;

        // Act
        validationResult.setPassedCaseCount(count);

        // Assert
        assertEquals(count, validationResult.getPassedCaseCount());
    }

    /**
     * 测试设置和获取失败用例数
     */
    @Test
    public void testSetAndGetFailedCaseCount_ValidCount_ReturnsCount() {
        // Arrange
        int count = 2;

        // Act
        validationResult.setFailedCaseCount(count);

        // Assert
        assertEquals(count, validationResult.getFailedCaseCount());
    }

    /**
     * 测试设置和获取匹配率
     */
    @Test
    public void testSetAndGetMatchRate_ValidRate_ReturnsRate() {
        // Arrange
        double rate = 80.5;

        // Act
        validationResult.setMatchRate(rate);

        // Assert
        assertEquals(rate, validationResult.getMatchRate(), 0.01);
    }

    /**
     * 测试获取用例结果列表
     */
    @Test
    public void testGetCaseResults_Default_ReturnsEmptyList() {
        // Act & Assert
        assertNotNull(validationResult.getCaseResults());
        assertTrue(validationResult.getCaseResults().isEmpty());
    }

    /**
     * 测试添加用例结果
     */
    @Test
    public void testAddCaseResult_ValidResult_AddsToList() {
        // Arrange
        CaseValidationResult caseResult = new CaseValidationResult();
        caseResult.setCaseNumber("TC001");

        // Act
        validationResult.addCaseResult(caseResult);

        // Assert
        assertEquals(1, validationResult.getCaseResults().size());
        assertEquals(caseResult, validationResult.getCaseResults().get(0));
    }

    /**
     * 测试添加null用例结果
     */
    @Test
    public void testAddCaseResult_Null_DoesNotAddToList() {
        // Act
        validationResult.addCaseResult(null);

        // Assert
        assertTrue(validationResult.getCaseResults().isEmpty());
    }

    /**
     * 测试添加多个用例结果
     */
    @Test
    public void testAddCaseResult_MultipleResults_AddsAllToList() {
        // Arrange
        CaseValidationResult caseResult1 = new CaseValidationResult();
        caseResult1.setCaseNumber("TC001");
        CaseValidationResult caseResult2 = new CaseValidationResult();
        caseResult2.setCaseNumber("TC002");

        // Act
        validationResult.addCaseResult(caseResult1);
        validationResult.addCaseResult(caseResult2);

        // Assert
        assertEquals(2, validationResult.getCaseResults().size());
        assertEquals(caseResult1, validationResult.getCaseResults().get(0));
        assertEquals(caseResult2, validationResult.getCaseResults().get(1));
    }
}


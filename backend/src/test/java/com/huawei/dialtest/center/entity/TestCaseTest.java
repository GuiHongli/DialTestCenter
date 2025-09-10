/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.entity;

import com.huawei.dialtest.center.entity.TestCase;
import com.huawei.dialtest.center.entity.TestCaseSet;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * 测试用例实体类单元测试
 * 测试TestCase实体的构造函数、getter/setter方法、equals/hashCode/toString方法
 *
 * @author g00940940
 * @since 2025-09-08
 */
public class TestCaseTest {

    /**
     * 测试默认构造函数
     */
    @Test
    public void testDefaultConstructor_ShouldCreateEmptyInstance() {
        // Arrange & Act
        TestCase testCase = new TestCase();

        // Assert
        assertNotNull(testCase);
        assertNull(testCase.getId());
        assertNull(testCase.getTestCaseSet());
        assertNull(testCase.getCaseName());
        assertNull(testCase.getCaseNumber());
        assertNull(testCase.getNetworkTopology());
        assertNull(testCase.getBusinessCategory());
        assertNull(testCase.getAppName());
        assertNull(testCase.getTestSteps());
        assertNull(testCase.getExpectedResult());
        assertEquals(false, testCase.getScriptExists());
        assertNull(testCase.getCreatedTime());
        assertNull(testCase.getUpdatedTime());
    }

    /**
     * 测试带参数构造函数
     */
    @Test
    public void testParameterizedConstructor_ShouldCreateInstanceWithValues() {
        // Arrange
        TestCaseSet testCaseSet = new TestCaseSet();
        testCaseSet.setId(1L);
        String caseName = "测试用例1";
        String caseNumber = "TC001";
        String networkTopology = "网络拓扑1";
        String businessCategory = "业务大类1";
        String appName = "应用1";
        String testSteps = "测试步骤";
        String expectedResult = "预期结果";

        // Act
        TestCase testCase = new TestCase(testCaseSet, caseName, caseNumber,
                networkTopology, businessCategory, appName, testSteps, expectedResult);

        // Assert
        assertNotNull(testCase);
        assertEquals(testCaseSet, testCase.getTestCaseSet());
        assertEquals(caseName, testCase.getCaseName());
        assertEquals(caseNumber, testCase.getCaseNumber());
        assertEquals(networkTopology, testCase.getNetworkTopology());
        assertEquals(businessCategory, testCase.getBusinessCategory());
        assertEquals(appName, testCase.getAppName());
        assertEquals(testSteps, testCase.getTestSteps());
        assertEquals(expectedResult, testCase.getExpectedResult());
    }

    /**
     * 测试ID的getter和setter
     */
    @Test
    public void testIdGetterSetter_ShouldSetAndGetCorrectly() {
        // Arrange
        TestCase testCase = new TestCase();
        Long id = 1L;

        // Act
        testCase.setId(id);

        // Assert
        assertEquals(id, testCase.getId());
    }

    /**
     * 测试用例集的getter和setter
     */
    @Test
    public void testTestCaseSetGetterSetter_ShouldSetAndGetCorrectly() {
        // Arrange
        TestCase testCase = new TestCase();
        TestCaseSet testCaseSet = new TestCaseSet();
        testCaseSet.setId(1L);

        // Act
        testCase.setTestCaseSet(testCaseSet);

        // Assert
        assertEquals(testCaseSet, testCase.getTestCaseSet());
    }

    /**
     * 测试用例名称的getter和setter
     */
    @Test
    public void testCaseNameGetterSetter_ShouldSetAndGetCorrectly() {
        // Arrange
        TestCase testCase = new TestCase();
        String caseName = "测试用例名称";

        // Act
        testCase.setCaseName(caseName);

        // Assert
        assertEquals(caseName, testCase.getCaseName());
    }

    /**
     * 测试用例编号的getter和setter
     */
    @Test
    public void testCaseNumberGetterSetter_ShouldSetAndGetCorrectly() {
        // Arrange
        TestCase testCase = new TestCase();
        String caseNumber = "TC001";

        // Act
        testCase.setCaseNumber(caseNumber);

        // Assert
        assertEquals(caseNumber, testCase.getCaseNumber());
    }

    /**
     * 测试脚本存在状态的getter和setter
     */
    @Test
    public void testScriptExistsGetterSetter_ShouldSetAndGetCorrectly() {
        // Arrange
        TestCase testCase = new TestCase();
        Boolean scriptExists = true;

        // Act
        testCase.setScriptExists(scriptExists);

        // Assert
        assertEquals(scriptExists, testCase.getScriptExists());
    }

    /**
     * 测试equals方法 - 相同对象
     */
    @Test
    public void testEquals_SameObject_ShouldReturnTrue() {
        // Arrange
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setCaseNumber("TC001");

        // Act & Assert
        assertTrue(testCase.equals(testCase));
    }

    /**
     * 测试equals方法 - 相同ID和用例编号
     */
    @Test
    public void testEquals_SameIdAndCaseNumber_ShouldReturnTrue() {
        // Arrange
        TestCase testCase1 = new TestCase();
        testCase1.setId(1L);
        testCase1.setCaseNumber("TC001");

        TestCase testCase2 = new TestCase();
        testCase2.setId(1L);
        testCase2.setCaseNumber("TC001");

        // Act & Assert
        assertTrue(testCase1.equals(testCase2));
    }

    /**
     * 测试equals方法 - 不同ID
     */
    @Test
    public void testEquals_DifferentId_ShouldReturnFalse() {
        // Arrange
        TestCase testCase1 = new TestCase();
        testCase1.setId(1L);
        testCase1.setCaseNumber("TC001");

        TestCase testCase2 = new TestCase();
        testCase2.setId(2L);
        testCase2.setCaseNumber("TC001");

        // Act & Assert
        assertFalse(testCase1.equals(testCase2));
    }

    /**
     * 测试equals方法 - null对象
     */
    @Test
    public void testEquals_NullObject_ShouldReturnFalse() {
        // Arrange
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setCaseNumber("TC001");

        // Act & Assert
        assertFalse(testCase.equals(null));
    }

    /**
     * 测试equals方法 - 不同类型对象
     */
    @Test
    public void testEquals_DifferentClass_ShouldReturnFalse() {
        // Arrange
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setCaseNumber("TC001");

        String otherObject = "not a TestCase";

        // Act & Assert
        assertFalse(testCase.equals(otherObject));
    }

    /**
     * 测试hashCode方法 - 相同对象
     */
    @Test
    public void testHashCode_SameObject_ShouldReturnSameHashCode() {
        // Arrange
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setCaseNumber("TC001");

        // Act
        int hashCode1 = testCase.hashCode();
        int hashCode2 = testCase.hashCode();

        // Assert
        assertEquals(hashCode1, hashCode2);
    }

    /**
     * 测试hashCode方法 - 相同ID和用例编号
     */
    @Test
    public void testHashCode_SameIdAndCaseNumber_ShouldReturnSameHashCode() {
        // Arrange
        TestCase testCase1 = new TestCase();
        testCase1.setId(1L);
        testCase1.setCaseNumber("TC001");

        TestCase testCase2 = new TestCase();
        testCase2.setId(1L);
        testCase2.setCaseNumber("TC001");

        // Act
        int hashCode1 = testCase1.hashCode();
        int hashCode2 = testCase2.hashCode();

        // Assert
        assertEquals(hashCode1, hashCode2);
    }

    /**
     * 测试toString方法
     */
    @Test
    public void testToString_ShouldContainAllFields() {
        // Arrange
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setCaseName("测试用例");
        testCase.setCaseNumber("TC001");
        testCase.setNetworkTopology("网络拓扑");
        testCase.setBusinessCategory("业务大类");
        testCase.setAppName("应用");
        testCase.setTestSteps("测试步骤");
        testCase.setExpectedResult("预期结果");
        testCase.setScriptExists(true);
        testCase.setCreatedTime(LocalDateTime.now());
        testCase.setUpdatedTime(LocalDateTime.now());

        // Act
        String result = testCase.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("TestCase{"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("caseName='测试用例'"));
        assertTrue(result.contains("caseNumber='TC001'"));
        assertTrue(result.contains("networkTopology='网络拓扑'"));
        assertTrue(result.contains("businessCategory='业务大类'"));
        assertTrue(result.contains("appName='应用'"));
        assertTrue(result.contains("testSteps='测试步骤'"));
        assertTrue(result.contains("expectedResult='预期结果'"));
        assertTrue(result.contains("scriptExists=true"));
    }
}

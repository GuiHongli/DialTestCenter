/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.entity;

import java.util.Objects;

/**
 * 测试用例实体类，用于存储从cases.xlsx解析出的用例信息
 * 包含用例名称、编号、逻辑组网、业务大类、App、测试步骤、预期结果等信息
 * 与TestCaseSet建立多对一关系，支持用例与用例集的关联管理
 *
 * @author g00940940
 * @since 2025-09-08
 */
public class TestCase {
    private Long id;
    private TestCaseSet testCaseSet;
    private String caseName;
    private String caseNumber;
    private String networkTopology;
    private String businessCategory;
    private String appName;
    private String testSteps;
    private String expectedResult;
    private Boolean scriptExists = false;

    /**
     * 默认构造函数
     *
     * @author g00940940
     * @since 2025-09-08
     */
    public TestCase() {}

    /**
     * 带参数构造函数，用于创建新的测试用例实例
     *
     * @param testCaseSet 关联的用例集
     * @param caseName 用例名称
     * @param caseNumber 用例编号
     * @param networkTopology 逻辑组网
     * @param businessCategory 业务大类
     * @param appName 用例App
     * @param testSteps 测试步骤
     * @param expectedResult 预期结果
     * @author g00940940
     * @since 2025-09-08
     */
    public TestCase(TestCaseSet testCaseSet, String caseName, String caseNumber, 
                   String networkTopology, String businessCategory, String appName, 
                   String testSteps, String expectedResult) {
        this.testCaseSet = testCaseSet;
        this.caseName = caseName;
        this.caseNumber = caseNumber;
        this.networkTopology = networkTopology;
        this.businessCategory = businessCategory;
        this.appName = appName;
        this.testSteps = testSteps;
        this.expectedResult = expectedResult;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestCaseSet getTestCaseSet() {
        return testCaseSet;
    }

    public void setTestCaseSet(TestCaseSet testCaseSet) {
        this.testCaseSet = testCaseSet;
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getNetworkTopology() {
        return networkTopology;
    }

    public void setNetworkTopology(String networkTopology) {
        this.networkTopology = networkTopology;
    }

    public String getBusinessCategory() {
        return businessCategory;
    }

    public void setBusinessCategory(String businessCategory) {
        this.businessCategory = businessCategory;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTestSteps() {
        return testSteps;
    }

    public void setTestSteps(String testSteps) {
        this.testSteps = testSteps;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }

    public Boolean getScriptExists() {
        return scriptExists;
    }

    public void setScriptExists(Boolean scriptExists) {
        this.scriptExists = scriptExists;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestCase testCase = (TestCase) o;
        return Objects.equals(id, testCase.id) &&
               Objects.equals(caseNumber, testCase.caseNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, caseNumber);
    }

    @Override
    public String toString() {
        return "TestCase{" +
               "id=" + id +
               ", caseName='" + caseName + '\'' +
               ", caseNumber='" + caseNumber + '\'' +
               ", networkTopology='" + networkTopology + '\'' +
               ", businessCategory='" + businessCategory + '\'' +
               ", appName='" + appName + '\'' +
               ", testSteps='" + testSteps + '\'' +
               ", expectedResult='" + expectedResult + '\'' +
               ", scriptExists=" + scriptExists +
               '}';
    }
}

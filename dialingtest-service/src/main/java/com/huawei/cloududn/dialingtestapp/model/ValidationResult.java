package com.huawei.cloududn.dialingtestapp.model;

import com.huawei.cloududn.dialingtest.model.CaseValidationResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 用例集校验结果
 *
 * @author g00940940
 * @since 2025-10-30
 */
public class ValidationResult {
    private Long testCaseSetId;
    private String testCaseSetName;
    private String testCaseSetVersion;
    private String businessZh;
    private String businessEn;
    private int totalCaseCount;
    private int passedCaseCount;
    private int failedCaseCount;
    private double matchRate;
    private final List<CaseValidationResult> caseResults = new ArrayList<>();

    public Long getTestCaseSetId() { return testCaseSetId; }
    public void setTestCaseSetId(Long testCaseSetId) { this.testCaseSetId = testCaseSetId; }
    public String getTestCaseSetName() { return testCaseSetName; }
    public void setTestCaseSetName(String testCaseSetName) { this.testCaseSetName = testCaseSetName; }
    public String getTestCaseSetVersion() { return testCaseSetVersion; }
    public void setTestCaseSetVersion(String testCaseSetVersion) { this.testCaseSetVersion = testCaseSetVersion; }
    public String getBusinessZh() { return businessZh; }
    public void setBusinessZh(String businessZh) { this.businessZh = businessZh; }
    public String getBusinessEn() { return businessEn; }
    public void setBusinessEn(String businessEn) { this.businessEn = businessEn; }
    public int getTotalCaseCount() { return totalCaseCount; }
    public void setTotalCaseCount(int totalCaseCount) { this.totalCaseCount = totalCaseCount; }
    public int getPassedCaseCount() { return passedCaseCount; }
    public void setPassedCaseCount(int passedCaseCount) { this.passedCaseCount = passedCaseCount; }
    public int getFailedCaseCount() { return failedCaseCount; }
    public void setFailedCaseCount(int failedCaseCount) { this.failedCaseCount = failedCaseCount; }
    public double getMatchRate() { return matchRate; }
    public void setMatchRate(double matchRate) { this.matchRate = matchRate; }
    public List<CaseValidationResult> getCaseResults() { return caseResults; }
    public void addCaseResult(CaseValidationResult r) { if (r != null) { caseResults.add(r); } }
}




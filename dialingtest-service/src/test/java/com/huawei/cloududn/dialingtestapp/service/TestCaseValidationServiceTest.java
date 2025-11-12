/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.cloududn.dialingtestapp.dao.PreprocessRuleDao;
import com.huawei.cloududn.dialingtestapp.dao.SoftwarePackageDao;
import com.huawei.cloududn.dialingtestapp.dao.TestCaseDao;
import com.huawei.cloududn.dialingtestapp.dao.TestCaseSetDao;
import com.huawei.cloududn.dialingtestapp.dao.ValidationResultDao;
import com.huawei.cloududn.dialingtestapp.dao.ValidationTaskDao;
import com.huawei.cloududn.dialingtestapp.entity.ValidationTask;
import com.huawei.cloududn.dialingtest.model.CaseValidationResult;
import com.huawei.cloududn.dialingtest.model.PreprocessRule;
import com.huawei.cloududn.dialingtest.model.SoftwarePackageInfo;
import com.huawei.cloududn.dialingtest.model.TestCase;
import com.huawei.cloududn.dialingtest.model.TestCaseSet;
import com.huawei.cloududn.dialingtestapp.model.ValidationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TestCaseValidationService 单元测试
 * 
 * @author g00940940
 * @since 2025-01-15
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCaseValidationServiceTest {

    @Mock
    private TestCaseSetDao testCaseSetDao;

    @Mock
    private TestCaseDao testCaseDao;

    @Mock
    private ArchiveParseService archiveParseService;

    @Mock
    private PreprocessRuleDao preprocessRuleDao;

    @Mock
    private SoftwarePackageDao softwarePackageDao;

    @Mock
    private ValidationTaskDao validationTaskDao;

    @Mock
    private ValidationResultDao validationResultDao;

    @InjectMocks
    private TestCaseValidationService testCaseValidationService;

    private TestCaseSet testTestCaseSet;
    private TestCase testCase1;
    private TestCase testCase2;
    private List<TestCase> testCases;
    private ArchiveParseResult archiveParseResult;

    @Before
    public void setUp() {
        // 准备测试数据
        testTestCaseSet = new TestCaseSet();
        testTestCaseSet.setId(1L);
        testTestCaseSet.setName("测试用例集");
        testTestCaseSet.setVersion("v1.0");
        testTestCaseSet.setBusinessZh("VPN阻断");
        testTestCaseSet.setBusinessEn("VPN_BLOCK");
        testTestCaseSet.setFileContent("test.zip".getBytes());

        testCase1 = new TestCase();
        testCase1.setId(1L);
        testCase1.setTestCaseSetId(1L);
        testCase1.setCaseNumber("TC001");
        testCase1.setCaseName("测试用例1");
        testCase1.setBusinessCategory("业务1");
        testCase1.setAppName("应用1");
        testCase1.setDependenciesRule("规则1");
        testCase1.setDependenciesPackage("软件包1");

        testCase2 = new TestCase();
        testCase2.setId(2L);
        testCase2.setTestCaseSetId(1L);
        testCase2.setCaseNumber("TC002");
        testCase2.setCaseName("测试用例2");
        testCase2.setBusinessCategory("业务2");
        testCase2.setAppName("应用2");
        testCase2.setDependenciesRule(null);
        testCase2.setDependenciesPackage(null);

        testCases = Arrays.asList(testCase1, testCase2);

        archiveParseResult = new ArchiveParseResult();
        archiveParseResult.addScriptFileName("TC001.py");
        archiveParseResult.addScriptFileName("TC002.py");
    }

    /**
     * 测试校验用例集 - 成功场景（所有校验通过）
     */
    @Test
    public void testValidateTestCaseSet_AllValid_ReturnsValidationResult() {
        // Arrange
        when(testCaseSetDao.findById(1L)).thenReturn(testTestCaseSet);
        when(testCaseDao.findAllByTestCaseSetId(1L)).thenReturn(testCases);
        when(archiveParseService.parseArchive(any(byte[].class))).thenReturn(archiveParseResult);
        PreprocessRule rule = new PreprocessRule();
        when(preprocessRuleDao.findByRuleNameAndBusinessZh("规则1", "VPN阻断")).thenReturn(rule);
        SoftwarePackageInfo pkg = new SoftwarePackageInfo();
        when(softwarePackageDao.getSoftwarePackageByName("软件包1")).thenReturn(pkg);

        // Act
        ValidationResult result = testCaseValidationService.validateTestCaseSet(1L);

        // Assert
        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getTestCaseSetId());
        assertEquals("测试用例集", result.getTestCaseSetName());
        assertEquals("v1.0", result.getTestCaseSetVersion());
        assertEquals(2, result.getTotalCaseCount());
        assertEquals(2, result.getPassedCaseCount());
        assertEquals(0, result.getFailedCaseCount());
        assertEquals(100.0, result.getMatchRate(), 0.01);
        assertEquals(2, result.getCaseResults().size());
    }

    /**
     * 测试校验用例集 - 用例集不存在
     */
    @Test(expected = IllegalArgumentException.class)
    public void testValidateTestCaseSet_TestCaseSetNotFound_ThrowsException() {
        // Arrange
        when(testCaseSetDao.findById(999L)).thenReturn(null);

        // Act
        testCaseValidationService.validateTestCaseSet(999L);
    }

    /**
     * 测试校验用例集 - 脚本不存在
     */
    @Test
    public void testValidateTestCaseSet_ScriptMissing_SetsScriptInvalid() {
        // Arrange
        ArchiveParseResult emptyScripts = new ArchiveParseResult();
        when(testCaseSetDao.findById(1L)).thenReturn(testTestCaseSet);
        when(testCaseDao.findAllByTestCaseSetId(1L)).thenReturn(testCases);
        when(archiveParseService.parseArchive(any(byte[].class))).thenReturn(emptyScripts);

        // Act
        ValidationResult result = testCaseValidationService.validateTestCaseSet(1L);

        // Assert
        assertNotNull(result);
        CaseValidationResult caseResult = result.getCaseResults().get(0);
        assertFalse(caseResult.isScriptMatchValid());
        assertTrue(caseResult.getInvalidReasons().contains("脚本[TC001.py]不存在"));
    }

    /**
     * 测试校验用例集 - 预处理规则不存在
     */
    @Test
    public void testValidateTestCaseSet_RuleMissing_SetsRuleInvalid() {
        // Arrange
        when(testCaseSetDao.findById(1L)).thenReturn(testTestCaseSet);
        when(testCaseDao.findAllByTestCaseSetId(1L)).thenReturn(testCases);
        when(archiveParseService.parseArchive(any(byte[].class))).thenReturn(archiveParseResult);
        when(preprocessRuleDao.findByRuleNameAndBusinessZh("规则1", "VPN阻断")).thenReturn(null);
        when(preprocessRuleDao.findByRuleNameAndBusinessEn("规则1", "VPN_BLOCK")).thenReturn(null);

        // Act
        ValidationResult result = testCaseValidationService.validateTestCaseSet(1L);

        // Assert
        assertNotNull(result);
        CaseValidationResult caseResult = result.getCaseResults().get(0);
        assertFalse(caseResult.isPreprocessRuleValid());
        assertTrue(caseResult.getInvalidReasons().contains("预处理规则[规则1]不存在"));
    }

    /**
     * 测试校验用例集 - 软件包不存在
     */
    @Test
    public void testValidateTestCaseSet_PackageMissing_SetsPackageInvalid() {
        // Arrange
        when(testCaseSetDao.findById(1L)).thenReturn(testTestCaseSet);
        when(testCaseDao.findAllByTestCaseSetId(1L)).thenReturn(testCases);
        when(archiveParseService.parseArchive(any(byte[].class))).thenReturn(archiveParseResult);
        PreprocessRule rule = new PreprocessRule();
        when(preprocessRuleDao.findByRuleNameAndBusinessZh("规则1", "VPN阻断")).thenReturn(rule);
        when(softwarePackageDao.getSoftwarePackageByName("软件包1")).thenReturn(null);

        // Act
        ValidationResult result = testCaseValidationService.validateTestCaseSet(1L);

        // Assert
        assertNotNull(result);
        CaseValidationResult caseResult = result.getCaseResults().get(0);
        assertFalse(caseResult.isSoftwarePackageValid());
        assertTrue(caseResult.getInvalidReasons().contains("软件包[软件包1]不存在"));
    }

    /**
     * 测试校验用例集 - 空用例列表
     */
    @Test
    public void testValidateTestCaseSet_EmptyTestCases_ReturnsEmptyResult() {
        // Arrange
        when(testCaseSetDao.findById(1L)).thenReturn(testTestCaseSet);
        when(testCaseDao.findAllByTestCaseSetId(1L)).thenReturn(Arrays.asList());
        when(archiveParseService.parseArchive(any(byte[].class))).thenReturn(new ArchiveParseResult());

        // Act
        ValidationResult result = testCaseValidationService.validateTestCaseSet(1L);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalCaseCount());
        assertEquals(0.0, result.getMatchRate(), 0.01);
        assertTrue(result.getCaseResults().isEmpty());
    }

    /**
     * 测试触发校验任务 - 成功场景
     */
    @Test
    public void testTriggerValidation_Success_ReturnsTaskInfo() {
        // Arrange
        when(testCaseSetDao.findById(1L)).thenReturn(testTestCaseSet);
        when(testCaseDao.countByTestCaseSetId(1L)).thenReturn(10L);
        when(validationTaskDao.insert(any(ValidationTask.class))).thenReturn(1);

        // Act
        TestCaseValidationService.ValidationTaskInfo taskInfo = testCaseValidationService.triggerValidation(1L);

        // Assert
        assertNotNull(taskInfo);
        assertNotNull(taskInfo.getTaskId());
        assertEquals(Long.valueOf(1L), taskInfo.getTestCaseSetId());
        assertEquals("PENDING", taskInfo.getStatus());
        assertEquals(Integer.valueOf(1), taskInfo.getEstimatedTime());
        verify(validationTaskDao).insert(any(ValidationTask.class));
    }

    /**
     * 测试触发校验任务 - 用例集不存在
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTriggerValidation_TestCaseSetNotFound_ThrowsException() {
        // Arrange
        when(testCaseSetDao.findById(999L)).thenReturn(null);

        // Act
        testCaseValidationService.triggerValidation(999L);
    }

    /**
     * 测试获取校验结果 - 结果存在
     */
    @Test
    public void testGetValidationResult_ResultExists_ReturnsValidationResult() throws Exception {
        // Arrange
        ValidationResult expectedResult = new ValidationResult();
        expectedResult.setTestCaseSetId(1L);
        ObjectMapper objectMapper = new ObjectMapper();
        String resultJson = objectMapper.writeValueAsString(expectedResult);
        when(validationResultDao.findByTestCaseSetId(1L)).thenReturn(resultJson);

        // Act
        ValidationResult result = testCaseValidationService.getValidationResult(1L);

        // Assert
        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getTestCaseSetId());
    }

    /**
     * 测试获取校验结果 - 任务正在执行中
     */
    @Test
    public void testGetValidationResult_TaskRunning_ReturnsNull() {
        // Arrange
        when(validationResultDao.findByTestCaseSetId(1L)).thenReturn(null);
        ValidationTask runningTask = new ValidationTask();
        runningTask.setStatus("RUNNING");
        when(validationTaskDao.findLatestByTestCaseSetId(1L)).thenReturn(runningTask);

        // Act
        ValidationResult result = testCaseValidationService.getValidationResult(1L);

        // Assert
        assertNull(result);
    }

    /**
     * 测试获取任务状态 - 成功场景
     */
    @Test
    public void testGetTaskStatus_Success_ReturnsTask() {
        // Arrange
        ValidationTask task = new ValidationTask();
        task.setTaskId("task-123");
        task.setStatus("COMPLETED");
        when(validationTaskDao.findLatestByTestCaseSetId(1L)).thenReturn(task);

        // Act
        ValidationTask result = testCaseValidationService.getTaskStatus(1L);

        // Assert
        assertNotNull(result);
        assertEquals("task-123", result.getTaskId());
        assertEquals("COMPLETED", result.getStatus());
    }

    /**
     * 测试获取任务状态 - 任务不存在
     */
    @Test
    public void testGetTaskStatus_TaskNotFound_ReturnsNull() {
        // Arrange
        when(validationTaskDao.findLatestByTestCaseSetId(1L)).thenReturn(null);

        // Act
        ValidationTask result = testCaseValidationService.getTaskStatus(1L);

        // Assert
        assertNull(result);
    }

    /**
     * 测试校验用例集 - 无依赖规则和软件包
     */
    @Test
    public void testValidateTestCaseSet_NoDependencies_ReturnsValid() {
        // Arrange
        TestCase testCase = new TestCase();
        testCase.setId(3L);
        testCase.setTestCaseSetId(1L);
        testCase.setCaseNumber("TC003");
        testCase.setCaseName("测试用例3");
        testCase.setDependenciesRule(null);
        testCase.setDependenciesPackage(null);

        when(testCaseSetDao.findById(1L)).thenReturn(testTestCaseSet);
        when(testCaseDao.findAllByTestCaseSetId(1L)).thenReturn(Arrays.asList(testCase));
        archiveParseResult.addScriptFileName("TC003.py");
        when(archiveParseService.parseArchive(any(byte[].class))).thenReturn(archiveParseResult);

        // Act
        ValidationResult result = testCaseValidationService.validateTestCaseSet(1L);

        // Assert
        assertNotNull(result);
        CaseValidationResult caseResult = result.getCaseResults().get(0);
        assertTrue(caseResult.isPreprocessRuleValid());
        assertTrue(caseResult.isSoftwarePackageValid());
    }

    /**
     * 测试获取校验结果 - JSON解析失败
     */
    @Test
    public void testGetValidationResult_InvalidJson_ReturnsNull() {
        // Arrange
        when(validationResultDao.findByTestCaseSetId(1L)).thenReturn("invalid json");
        when(validationTaskDao.findLatestByTestCaseSetId(1L)).thenReturn(null);

        // Act
        ValidationResult result = testCaseValidationService.getValidationResult(1L);

        // Assert
        assertNull(result);
    }

    /**
     * 测试获取校验结果 - 结果不存在且任务为PENDING
     */
    @Test
    public void testGetValidationResult_TaskPending_ReturnsNull() {
        // Arrange
        when(validationResultDao.findByTestCaseSetId(1L)).thenReturn(null);
        ValidationTask pendingTask = new ValidationTask();
        pendingTask.setStatus("PENDING");
        when(validationTaskDao.findLatestByTestCaseSetId(1L)).thenReturn(pendingTask);

        // Act
        ValidationResult result = testCaseValidationService.getValidationResult(1L);

        // Assert
        assertNull(result);
    }

    /**
     * 测试获取校验结果 - 结果不存在且任务为COMPLETED
     */
    @Test
    public void testGetValidationResult_TaskCompleted_ReturnsNull() {
        // Arrange
        when(validationResultDao.findByTestCaseSetId(1L)).thenReturn(null);
        ValidationTask completedTask = new ValidationTask();
        completedTask.setStatus("COMPLETED");
        when(validationTaskDao.findLatestByTestCaseSetId(1L)).thenReturn(completedTask);

        // Act
        ValidationResult result = testCaseValidationService.getValidationResult(1L);

        // Assert
        assertNull(result);
    }

    /**
     * 测试异步执行校验任务 - 成功场景
     */
    @Test
    public void testExecuteValidationTaskAsync_Success_UpdatesTaskStatus() throws Exception {
        // Arrange
        String taskId = "task-123";
        when(testCaseSetDao.findById(1L)).thenReturn(testTestCaseSet);
        when(testCaseDao.findAllByTestCaseSetId(1L)).thenReturn(testCases);
        when(archiveParseService.parseArchive(any(byte[].class))).thenReturn(archiveParseResult);
        when(preprocessRuleDao.findByRuleNameAndBusinessZh(anyString(), anyString())).thenReturn(null);
        when(softwarePackageDao.getSoftwarePackageByName(anyString())).thenReturn(null);
        when(validationTaskDao.updateStatus(anyString(), anyString(), anyInt(), 
            any(LocalDateTime.class), any(LocalDateTime.class), anyString())).thenReturn(1);
        when(validationResultDao.save(anyLong(), anyString(), anyString())).thenReturn(1);

        // Act
        CompletableFuture<Void> future = testCaseValidationService.executeValidationTaskAsync(1L, taskId);

        // Assert
        assertNotNull(future);
        future.get(); // Wait for completion
        verify(validationTaskDao, atLeastOnce()).updateStatus(eq(taskId), eq("RUNNING"), eq(0), 
            any(LocalDateTime.class), isNull(), isNull());
        verify(validationTaskDao, atLeastOnce()).updateStatus(eq(taskId), eq("COMPLETED"), eq(100), 
            any(LocalDateTime.class), any(LocalDateTime.class), isNull());
        verify(validationResultDao, times(1)).save(eq(1L), eq(taskId), anyString());
    }

    /**
     * 测试异步执行校验任务 - 校验失败场景
     */
    @Test
    public void testExecuteValidationTaskAsync_ValidationFails_UpdatesTaskStatusToFailed() throws Exception {
        // Arrange
        String taskId = "task-123";
        when(testCaseSetDao.findById(1L)).thenReturn(null); // 用例集不存在，会抛出异常
        when(validationTaskDao.updateStatus(anyString(), anyString(), isNull(), 
            isNull(), any(LocalDateTime.class), anyString())).thenReturn(1);

        // Act
        CompletableFuture<Void> future = testCaseValidationService.executeValidationTaskAsync(1L, taskId);

        // Assert
        assertNotNull(future);
        future.get(); // Wait for completion
        verify(validationTaskDao, atLeastOnce()).updateStatus(eq(taskId), eq("RUNNING"), eq(0), 
            any(LocalDateTime.class), isNull(), isNull());
        verify(validationTaskDao, atLeastOnce()).updateStatus(eq(taskId), eq("FAILED"), isNull(), 
            isNull(), any(LocalDateTime.class), anyString());
    }
}


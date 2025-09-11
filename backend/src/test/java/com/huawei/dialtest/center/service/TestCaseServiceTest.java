/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import com.huawei.dialtest.center.entity.TestCase;
import com.huawei.dialtest.center.entity.TestCaseSet;
import com.huawei.dialtest.center.mapper.TestCaseMapper;
import com.huawei.dialtest.center.service.TestCaseService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试用例服务类单元测试
 * 测试TestCaseService的业务逻辑方法，包括CRUD操作和脚本匹配功能
 *
 * @author g00940940
 * @since 2025-09-08
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCaseServiceTest {

    @Mock
    private TestCaseMapper testCaseMapper;

    @InjectMocks
    private TestCaseService testCaseService;

    private TestCaseSet testCaseSet;
    private TestCase testCase;

    @Before
    public void setUp() {
        // 准备测试数据
        testCaseSet = new TestCaseSet();
        testCaseSet.setId(1L);
        testCaseSet.setName("测试用例集");
        testCaseSet.setVersion("v1.0");

        testCase = new TestCase();
        testCase.setId(1L);
        testCase.setTestCaseSet(testCaseSet);
        testCase.setCaseName("测试用例1");
        testCase.setCaseNumber("TC001");
        testCase.setNetworkTopology("网络拓扑1");
        testCase.setBusinessCategory("业务大类1");
        testCase.setAppName("应用1");
        testCase.setTestSteps("测试步骤");
        testCase.setExpectedResult("预期结果");
        testCase.setScriptExists(true);
        testCase.setCreatedTime(LocalDateTime.now());
        testCase.setUpdatedTime(LocalDateTime.now());
    }

    /**
     * 测试根据用例集获取测试用例列表（分页）
     */
    @Test
    public void testGetTestCasesByTestCaseSet_WithPagination_ShouldReturnPage() {
        // Arrange
        Long testCaseSetId = 1L;
        int page = 1;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        List<TestCase> testCases = Arrays.asList(testCase);
        Page<TestCase> expectedPage = new PageImpl<>(testCases, pageable, 1);

        when(testCaseMapper.findByTestCaseSetIdWithPage(testCaseSetId, page - 1, pageSize))
                .thenReturn(testCases);
        when(testCaseMapper.countByTestCaseSetId(testCaseSetId))
                .thenReturn(1L);

        // Act
        Page<TestCase> result = testCaseService.getTestCasesByTestCaseSet(testCaseSetId, page, pageSize);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testCase, result.getContent().get(0));
        verify(testCaseMapper).findByTestCaseSetIdWithPage(testCaseSetId, page - 1, pageSize);
        verify(testCaseMapper).countByTestCaseSetId(testCaseSetId);
    }

    /**
     * 测试根据用例集获取所有测试用例
     */
    @Test
    public void testGetTestCasesByTestCaseSet_WithoutPagination_ShouldReturnList() {
        // Arrange
        List<TestCase> expectedTestCases = Arrays.asList(testCase);
        when(testCaseMapper.findByTestCaseSetId(testCaseSet.getId())).thenReturn(expectedTestCases);

        // Act
        List<TestCase> result = testCaseService.getTestCasesByTestCaseSet(testCaseSet);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCase, result.get(0));
        verify(testCaseMapper).findByTestCaseSetId(testCaseSet.getId());
    }

    /**
     * 测试根据ID获取测试用例 - 存在
     */
    @Test
    public void testGetTestCaseById_ExistingId_ShouldReturnTestCase() {
        // Arrange
        Long id = 1L;
        when(testCaseMapper.findById(id)).thenReturn(testCase);

        // Act
        Optional<TestCase> result = testCaseService.getTestCaseById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testCase, result.get());
        verify(testCaseMapper).findById(id);
    }

    /**
     * 测试根据ID获取测试用例 - 不存在
     */
    @Test
    public void testGetTestCaseById_NonExistingId_ShouldReturnEmpty() {
        // Arrange
        Long id = 999L;
        when(testCaseMapper.findById(id)).thenReturn(null);

        // Act
        Optional<TestCase> result = testCaseService.getTestCaseById(id);

        // Assert
        assertFalse(result.isPresent());
        verify(testCaseMapper).findById(id);
    }

    /**
     * 测试根据用例编号获取测试用例 - 存在
     */
    @Test
    public void testGetTestCaseByCaseNumber_ExistingCaseNumber_ShouldReturnTestCase() {
        // Arrange
        String caseNumber = "TC001";
        when(testCaseMapper.findByCaseNumber(caseNumber)).thenReturn(testCase);

        // Act
        Optional<TestCase> result = testCaseService.getTestCaseByCaseNumber(caseNumber);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testCase, result.get());
        verify(testCaseMapper).findByCaseNumber(caseNumber);
    }

    /**
     * 测试根据用例编号获取测试用例 - 不存在
     */
    @Test
    public void testGetTestCaseByCaseNumber_NonExistingCaseNumber_ShouldReturnEmpty() {
        // Arrange
        String caseNumber = "TC999";
        when(testCaseMapper.findByCaseNumber(caseNumber)).thenReturn(null);

        // Act
        Optional<TestCase> result = testCaseService.getTestCaseByCaseNumber(caseNumber);

        // Assert
        assertFalse(result.isPresent());
        verify(testCaseMapper).findByCaseNumber(caseNumber);
    }

    /**
     * 测试保存测试用例
     */
    @Test
    public void testSaveTestCase_ShouldSaveAndReturnTestCase() {
        // Arrange
        when(testCaseMapper.insert(testCase)).thenReturn(1);

        // Act
        TestCase result = testCaseService.saveTestCase(testCase);

        // Assert
        assertNotNull(result);
        assertEquals(testCase, result);
        verify(testCaseMapper).insert(testCase);
    }

    /**
     * 测试批量保存测试用例
     */
    @Test
    public void testSaveTestCases_ShouldSaveAllTestCases() {
        // Arrange
        List<TestCase> testCases = Arrays.asList(testCase);
        when(testCaseMapper.insert(any(TestCase.class))).thenReturn(1);

        // Act
        List<TestCase> result = testCaseService.saveTestCases(testCases);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCase, result.get(0));
        verify(testCaseMapper, times(testCases.size())).insert(any(TestCase.class));
    }

    /**
     * 测试更新脚本存在状态 - 成功
     */
    @Test
    public void testUpdateScriptExists_ExistingTestCase_ShouldUpdateAndReturn() {
        // Arrange
        Long testCaseId = 1L;
        Boolean scriptExists = false;
        when(testCaseMapper.findById(testCaseId)).thenReturn(testCase);
        when(testCaseMapper.update(testCase)).thenReturn(1);

        // Act
        TestCase result = testCaseService.updateScriptExists(testCaseId, scriptExists);

        // Assert
        assertNotNull(result);
        assertEquals(scriptExists, result.getScriptExists());
        verify(testCaseMapper).findById(testCaseId);
        verify(testCaseMapper).update(testCase);
    }

    /**
     * 测试更新脚本存在状态 - 测试用例不存在
     */
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateScriptExists_NonExistingTestCase_ShouldThrowException() {
        // Arrange
        Long testCaseId = 999L;
        Boolean scriptExists = false;
        when(testCaseMapper.findById(testCaseId)).thenReturn(null);

        // Act
        testCaseService.updateScriptExists(testCaseId, scriptExists);

        // Assert - 期望抛出异常
    }

    /**
     * 测试获取没有脚本的测试用例列表
     */
    @Test
    public void testGetMissingScripts_ShouldReturnMissingScripts() {
        // Arrange
        Long testCaseSetId = 1L;
        List<TestCase> missingScripts = Arrays.asList(testCase);
        when(testCaseMapper.findMissingScripts(testCaseSetId)).thenReturn(missingScripts);

        // Act
        List<TestCase> result = testCaseService.getMissingScripts(testCaseSetId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCase, result.get(0));
        verify(testCaseMapper).findMissingScripts(testCaseSetId);
    }

    /**
     * 测试统计没有脚本的测试用例数量
     */
    @Test
    public void testCountMissingScripts_ShouldReturnCount() {
        // Arrange
        Long testCaseSetId = 1L;
        long expectedCount = 5L;
        when(testCaseMapper.countMissingScripts(testCaseSetId)).thenReturn(expectedCount);

        // Act
        long result = testCaseService.countMissingScripts(testCaseSetId);

        // Assert
        assertEquals(expectedCount, result);
        verify(testCaseMapper).countMissingScripts(testCaseSetId);
    }

    /**
     * 测试删除测试用例 - 成功
     */
    @Test
    public void testDeleteTestCase_ExistingTestCase_ShouldDelete() {
        // Arrange
        Long testCaseId = 1L;
        when(testCaseMapper.findById(testCaseId)).thenReturn(testCase);
        when(testCaseMapper.deleteById(testCaseId)).thenReturn(1);

        // Act
        testCaseService.deleteTestCase(testCaseId);

        // Assert
        verify(testCaseMapper).findById(testCaseId);
        verify(testCaseMapper).deleteById(testCaseId);
    }

    /**
     * 测试删除测试用例 - 测试用例不存在
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDeleteTestCase_NonExistingTestCase_ShouldThrowException() {
        // Arrange
        Long testCaseId = 999L;
        when(testCaseMapper.findById(testCaseId)).thenReturn(null);

        // Act
        testCaseService.deleteTestCase(testCaseId);

        // Assert - 期望抛出异常
    }

    /**
     * 测试根据用例集删除所有测试用例
     */
    @Test
    public void testDeleteTestCasesByTestCaseSet_ShouldDeleteAllTestCases() {
        // Arrange
        // 不需要mock，因为方法内部直接调用repository

        // Act
        testCaseService.deleteTestCasesByTestCaseSet(testCaseSet);

        // Assert
        verify(testCaseMapper).deleteByTestCaseSetId(testCaseSet.getId());
    }
}

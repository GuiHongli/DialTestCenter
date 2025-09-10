/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import com.huawei.dialtest.center.service.ScriptMatchService;
import com.huawei.dialtest.center.service.ScriptMatchService.ScriptMatchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 脚本匹配服务类单元测试
 * 测试ScriptMatchService的脚本匹配功能，包括用例编号与脚本文件的匹配验证
 *
 * @author g00940940
 * @since 2025-09-08
 */
@RunWith(MockitoJUnitRunner.class)
public class ScriptMatchServiceTest {

    @InjectMocks
    private ScriptMatchService scriptMatchService;

    /**
     * 测试完全匹配的情况
     */
    @Test
    public void testMatchScripts_AllMatched_ShouldReturnAllMatched() {
        // Arrange
        List<String> caseNumbers = Arrays.asList("TC001", "TC002", "TC003");
        List<String> scriptFileNames = Arrays.asList("TC001.py", "TC002.py", "TC003.py");

        // Act
        ScriptMatchResult result = scriptMatchService.matchScripts(caseNumbers, scriptFileNames);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalCases());
        assertEquals(3, result.getMatchedCount());
        assertEquals(0, result.getMissingCount());
        assertEquals(0, result.getExtraCount());
        assertTrue(result.isAllMatched());
        assertFalse(result.hasMissingScripts());
        assertFalse(result.hasExtraScripts());
        
        // 验证匹配映射
        assertTrue(result.getMatchMap().get("TC001"));
        assertTrue(result.getMatchMap().get("TC002"));
        assertTrue(result.getMatchMap().get("TC003"));
        assertTrue(result.getMissingScripts().isEmpty());
        assertTrue(result.getExtraScripts().isEmpty());
    }

    /**
     * 测试部分匹配的情况
     */
    @Test
    public void testMatchScripts_PartialMatch_ShouldReturnPartialMatch() {
        // Arrange
        List<String> caseNumbers = Arrays.asList("TC001", "TC002", "TC003");
        List<String> scriptFileNames = Arrays.asList("TC001.py", "TC003.py");

        // Act
        ScriptMatchResult result = scriptMatchService.matchScripts(caseNumbers, scriptFileNames);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalCases());
        assertEquals(2, result.getMatchedCount());
        assertEquals(1, result.getMissingCount());
        assertEquals(0, result.getExtraCount());
        assertFalse(result.isAllMatched());
        assertTrue(result.hasMissingScripts());
        assertFalse(result.hasExtraScripts());
        
        // 验证匹配映射
        assertTrue(result.getMatchMap().get("TC001"));
        assertFalse(result.getMatchMap().get("TC002"));
        assertTrue(result.getMatchMap().get("TC003"));
        
        // 验证缺失脚本列表
        assertEquals(1, result.getMissingScripts().size());
        assertTrue(result.getMissingScripts().contains("TC002"));
    }

    /**
     * 测试有额外脚本的情况
     */
    @Test
    public void testMatchScripts_WithExtraScripts_ShouldReturnExtraScripts() {
        // Arrange
        List<String> caseNumbers = Arrays.asList("TC001", "TC002");
        List<String> scriptFileNames = Arrays.asList("TC001.py", "TC002.py", "TC003.py", "TC004.py");

        // Act
        ScriptMatchResult result = scriptMatchService.matchScripts(caseNumbers, scriptFileNames);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalCases());
        assertEquals(2, result.getMatchedCount());
        assertEquals(0, result.getMissingCount());
        assertEquals(2, result.getExtraCount());
        assertTrue(result.isAllMatched());
        assertFalse(result.hasMissingScripts());
        assertTrue(result.hasExtraScripts());
        
        // 验证匹配映射
        assertTrue(result.getMatchMap().get("TC001"));
        assertTrue(result.getMatchMap().get("TC002"));
        
        // 验证额外脚本列表
        assertEquals(2, result.getExtraScripts().size());
        assertTrue(result.getExtraScripts().contains("TC003.py"));
        assertTrue(result.getExtraScripts().contains("TC004.py"));
    }

    /**
     * 测试空用例编号列表
     */
    @Test
    public void testMatchScripts_EmptyCaseNumbers_ShouldReturnEmptyResult() {
        // Arrange
        List<String> caseNumbers = Arrays.asList();
        List<String> scriptFileNames = Arrays.asList("TC001.py", "TC002.py");

        // Act
        ScriptMatchResult result = scriptMatchService.matchScripts(caseNumbers, scriptFileNames);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalCases());
        assertEquals(0, result.getMatchedCount());
        assertEquals(0, result.getMissingCount());
        assertEquals(2, result.getExtraCount());
        assertTrue(result.isAllMatched());
        assertFalse(result.hasMissingScripts());
        assertTrue(result.hasExtraScripts());
    }

    /**
     * 测试空脚本文件名列表
     */
    @Test
    public void testMatchScripts_EmptyScriptFileNames_ShouldReturnAllMissing() {
        // Arrange
        List<String> caseNumbers = Arrays.asList("TC001", "TC002", "TC003");
        List<String> scriptFileNames = Arrays.asList();

        // Act
        ScriptMatchResult result = scriptMatchService.matchScripts(caseNumbers, scriptFileNames);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalCases());
        assertEquals(0, result.getMatchedCount());
        assertEquals(3, result.getMissingCount());
        assertEquals(0, result.getExtraCount());
        assertFalse(result.isAllMatched());
        assertTrue(result.hasMissingScripts());
        assertFalse(result.hasExtraScripts());
        
        // 验证所有用例都缺失脚本
        assertFalse(result.getMatchMap().get("TC001"));
        assertFalse(result.getMatchMap().get("TC002"));
        assertFalse(result.getMatchMap().get("TC003"));
        
        assertEquals(3, result.getMissingScripts().size());
        assertTrue(result.getMissingScripts().contains("TC001"));
        assertTrue(result.getMissingScripts().contains("TC002"));
        assertTrue(result.getMissingScripts().contains("TC003"));
    }

    /**
     * 测试验证单个用例编号是否有对应脚本 - 存在
     */
    @Test
    public void testHasScriptForCase_ExistingScript_ShouldReturnTrue() {
        // Arrange
        String caseNumber = "TC001";
        List<String> scriptFileNames = Arrays.asList("TC001.py", "TC002.py");

        // Act
        boolean result = scriptMatchService.hasScriptForCase(caseNumber, scriptFileNames);

        // Assert
        assertTrue(result);
    }

    /**
     * 测试验证单个用例编号是否有对应脚本 - 不存在
     */
    @Test
    public void testHasScriptForCase_NonExistingScript_ShouldReturnFalse() {
        // Arrange
        String caseNumber = "TC003";
        List<String> scriptFileNames = Arrays.asList("TC001.py", "TC002.py");

        // Act
        boolean result = scriptMatchService.hasScriptForCase(caseNumber, scriptFileNames);

        // Assert
        assertFalse(result);
    }

    /**
     * 测试验证单个用例编号是否有对应脚本 - 空用例编号
     */
    @Test
    public void testHasScriptForCase_EmptyCaseNumber_ShouldReturnFalse() {
        // Arrange
        String caseNumber = "";
        List<String> scriptFileNames = Arrays.asList("TC001.py", "TC002.py");

        // Act
        boolean result = scriptMatchService.hasScriptForCase(caseNumber, scriptFileNames);

        // Assert
        assertFalse(result);
    }

    /**
     * 测试验证单个用例编号是否有对应脚本 - null用例编号
     */
    @Test
    public void testHasScriptForCase_NullCaseNumber_ShouldReturnFalse() {
        // Arrange
        String caseNumber = null;
        List<String> scriptFileNames = Arrays.asList("TC001.py", "TC002.py");

        // Act
        boolean result = scriptMatchService.hasScriptForCase(caseNumber, scriptFileNames);

        // Assert
        assertFalse(result);
    }

    /**
     * 测试获取用例编号对应的脚本文件名 - 存在
     */
    @Test
    public void testGetScriptFileNameForCase_ExistingScript_ShouldReturnFileName() {
        // Arrange
        String caseNumber = "TC001";
        List<String> scriptFileNames = Arrays.asList("TC001.py", "TC002.py");

        // Act
        String result = scriptMatchService.getScriptFileNameForCase(caseNumber, scriptFileNames);

        // Assert
        assertNotNull(result);
        assertEquals("TC001.py", result);
    }

    /**
     * 测试获取用例编号对应的脚本文件名 - 不存在
     */
    @Test
    public void testGetScriptFileNameForCase_NonExistingScript_ShouldReturnNull() {
        // Arrange
        String caseNumber = "TC003";
        List<String> scriptFileNames = Arrays.asList("TC001.py", "TC002.py");

        // Act
        String result = scriptMatchService.getScriptFileNameForCase(caseNumber, scriptFileNames);

        // Assert
        assertNull(result);
    }

    /**
     * 测试ScriptMatchResult的toString方法
     */
    @Test
    public void testScriptMatchResult_ToString_ShouldContainStatistics() {
        // Arrange
        List<String> caseNumbers = Arrays.asList("TC001", "TC002");
        List<String> scriptFileNames = Arrays.asList("TC001.py");

        // Act
        ScriptMatchResult result = scriptMatchService.matchScripts(caseNumbers, scriptFileNames);
        String toString = result.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("ScriptMatchResult{"));
        assertTrue(toString.contains("totalCases=2"));
        assertTrue(toString.contains("matched=1"));
        assertTrue(toString.contains("missing=1"));
        assertTrue(toString.contains("extra=0"));
    }
}

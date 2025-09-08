/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 脚本匹配服务类，用于匹配用例编号与Python脚本文件
 * 支持脚本存在性验证和缺失脚本记录
 * 提供匹配结果统计和详细信息
 *
 * @author g00940940
 * @since 2025-09-08
 */
@Service
public class ScriptMatchService {
    private static final Logger logger = LoggerFactory.getLogger(ScriptMatchService.class);

    /**
     * 匹配用例编号与脚本文件
     *
     * @param caseNumbers 用例编号列表
     * @param scriptFileNames 脚本文件名列表
     * @return 匹配结果对象
     */
    public ScriptMatchResult matchScripts(List<String> caseNumbers, List<String> scriptFileNames) {
        logger.debug("Matching {} case numbers with {} script files", caseNumbers.size(), scriptFileNames.size());
        
        Map<String, Boolean> matchMap = new HashMap<>();
        List<String> missingScripts = new ArrayList<>();
        List<String> extraScripts = new ArrayList<>();
        
        // 创建脚本文件名到用例编号的映射（去除.py扩展名）
        Map<String, String> scriptToCaseMap = new HashMap<>();
        for (String scriptFileName : scriptFileNames) {
            String caseNumber = removePythonExtension(scriptFileName);
            scriptToCaseMap.put(caseNumber, scriptFileName);
        }
        
        // 检查每个用例编号是否有对应的脚本
        for (String caseNumber : caseNumbers) {
            if (scriptToCaseMap.containsKey(caseNumber)) {
                matchMap.put(caseNumber, true);
                logger.debug("Found script for case: {}", caseNumber);
            } else {
                matchMap.put(caseNumber, false);
                missingScripts.add(caseNumber);
                logger.debug("Missing script for case: {}", caseNumber);
            }
        }
        
        // 检查多余的脚本文件
        for (String caseNumber : caseNumbers) {
            scriptToCaseMap.remove(caseNumber);
        }
        extraScripts.addAll(scriptToCaseMap.values());
        
        ScriptMatchResult result = new ScriptMatchResult(matchMap, missingScripts, extraScripts);
        logger.info("Script matching completed: {} matched, {} missing, {} extra", 
                   result.getMatchedCount(), result.getMissingCount(), result.getExtraCount());
        
        return result;
    }

    /**
     * 验证单个用例编号是否有对应的脚本文件
     *
     * @param caseNumber 用例编号
     * @param scriptFileNames 脚本文件名列表
     * @return 是否存在对应的脚本文件
     */
    public boolean hasScriptForCase(String caseNumber, List<String> scriptFileNames) {
        if (caseNumber == null || caseNumber.isEmpty()) {
            return false;
        }
        
        String expectedScriptName = caseNumber + ".py";
        return scriptFileNames.contains(expectedScriptName);
    }

    /**
     * 获取用例编号对应的脚本文件名
     *
     * @param caseNumber 用例编号
     * @param scriptFileNames 脚本文件名列表
     * @return 脚本文件名，如果不存在则返回null
     */
    public String getScriptFileNameForCase(String caseNumber, List<String> scriptFileNames) {
        if (caseNumber == null || caseNumber.isEmpty()) {
            return null;
        }
        
        String expectedScriptName = caseNumber + ".py";
        if (scriptFileNames.contains(expectedScriptName)) {
            return expectedScriptName;
        }
        
        return null;
    }

    /**
     * 移除Python文件扩展名
     *
     * @param fileName 文件名
     * @return 去除扩展名后的文件名
     */
    private String removePythonExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        
        if (fileName.toLowerCase().endsWith(".py")) {
            return fileName.substring(0, fileName.length() - 3);
        }
        
        return fileName;
    }

    /**
     * 脚本匹配结果类
     */
    public static class ScriptMatchResult {
        private final Map<String, Boolean> matchMap;
        private final List<String> missingScripts;
        private final List<String> extraScripts;

        public ScriptMatchResult(Map<String, Boolean> matchMap, List<String> missingScripts, List<String> extraScripts) {
            this.matchMap = matchMap;
            this.missingScripts = missingScripts;
            this.extraScripts = extraScripts;
        }

        public Map<String, Boolean> getMatchMap() {
            return matchMap;
        }

        public List<String> getMissingScripts() {
            return missingScripts;
        }

        public List<String> getExtraScripts() {
            return extraScripts;
        }

        public int getMatchedCount() {
            return (int) matchMap.values().stream().filter(Boolean::booleanValue).count();
        }

        public int getMissingCount() {
            return missingScripts.size();
        }

        public int getExtraCount() {
            return extraScripts.size();
        }

        public int getTotalCases() {
            return matchMap.size();
        }

        public boolean isAllMatched() {
            return missingScripts.isEmpty();
        }

        public boolean hasMissingScripts() {
            return !missingScripts.isEmpty();
        }

        public boolean hasExtraScripts() {
            return !extraScripts.isEmpty();
        }

        @Override
        public String toString() {
            return "ScriptMatchResult{" +
                   "totalCases=" + getTotalCases() +
                   ", matched=" + getMatchedCount() +
                   ", missing=" + getMissingCount() +
                   ", extra=" + getExtraCount() +
                   '}';
        }
    }
}

/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import com.huawei.dialtest.center.entity.TestCase;
import com.huawei.dialtest.center.entity.TestCaseSet;
import com.huawei.dialtest.center.repository.TestCaseRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 测试用例服务类，提供测试用例的业务逻辑处理
 * 包括解析Excel文件、匹配脚本文件、查询用例信息等操作
 * 支持用例与脚本的匹配验证和缺失脚本的记录
 *
 * @author g00940940
 * @since 2025-09-08
 */
@Service
public class TestCaseService {
    private static final Logger logger = LoggerFactory.getLogger(TestCaseService.class);

    @Autowired
    private TestCaseRepository testCaseRepository;

    /**
     * 根据用例集获取测试用例列表（分页）
     *
     * @param testCaseSetId 用例集ID
     * @param page 页码，从1开始
     * @param pageSize 每页大小
     * @return 测试用例分页数据
     */
    public Page<TestCase> getTestCasesByTestCaseSet(Long testCaseSetId, int page, int pageSize) {
        logger.debug("Getting test cases for test case set: {}, page: {}, size: {}", testCaseSetId, page, pageSize);
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        return testCaseRepository.findByTestCaseSetId(testCaseSetId, pageable);
    }

    /**
     * 根据用例集获取所有测试用例
     *
     * @param testCaseSet 用例集对象
     * @return 测试用例列表
     */
    public List<TestCase> getTestCasesByTestCaseSet(TestCaseSet testCaseSet) {
        logger.debug("Getting all test cases for test case set: {}", testCaseSet.getId());
        return testCaseRepository.findByTestCaseSet(testCaseSet);
    }

    /**
     * 根据ID获取测试用例
     *
     * @param id 测试用例ID
     * @return 测试用例对象，如果不存在则返回空
     */
    public Optional<TestCase> getTestCaseById(Long id) {
        logger.debug("Getting test case by ID: {}", id);
        return testCaseRepository.findById(id);
    }

    /**
     * 根据用例编号获取测试用例
     *
     * @param caseNumber 用例编号
     * @return 测试用例对象，如果不存在则返回空
     */
    public Optional<TestCase> getTestCaseByCaseNumber(String caseNumber) {
        logger.debug("Getting test case by case number: {}", caseNumber);
        return testCaseRepository.findByCaseNumber(caseNumber);
    }

    /**
     * 保存测试用例
     *
     * @param testCase 测试用例对象
     * @return 保存后的测试用例对象
     */
    @Transactional
    public TestCase saveTestCase(TestCase testCase) {
        logger.debug("Saving test case: {}", testCase.getCaseNumber());
        return testCaseRepository.save(testCase);
    }

    /**
     * 批量保存测试用例
     *
     * @param testCases 测试用例列表
     * @return 保存后的测试用例列表
     */
    @Transactional
    public List<TestCase> saveTestCases(List<TestCase> testCases) {
        logger.info("Saving {} test cases", testCases.size());
        return testCaseRepository.saveAll(testCases);
    }

    /**
     * 更新测试用例的脚本存在状态
     *
     * @param testCaseId 测试用例ID
     * @param scriptExists 脚本是否存在
     * @return 更新后的测试用例对象
     * @throws IllegalArgumentException 当测试用例不存在时抛出
     */
    @Transactional
    public TestCase updateScriptExists(Long testCaseId, Boolean scriptExists) {
        logger.debug("Updating script exists status for test case: {}, exists: {}", testCaseId, scriptExists);
        
        Optional<TestCase> testCaseOpt = testCaseRepository.findById(testCaseId);
        if (testCaseOpt.isPresent()) {
            TestCase testCase = testCaseOpt.get();
            testCase.setScriptExists(scriptExists);
            return testCaseRepository.save(testCase);
        } else {
            throw new IllegalArgumentException("Test case does not exist");
        }
    }

    /**
     * 获取没有脚本的测试用例列表
     *
     * @param testCaseSetId 用例集ID
     * @return 没有脚本的测试用例列表
     */
    public List<TestCase> getMissingScripts(Long testCaseSetId) {
        logger.debug("Getting missing scripts for test case set: {}", testCaseSetId);
        return testCaseRepository.findMissingScripts(testCaseSetId);
    }

    /**
     * 统计没有脚本的测试用例数量
     *
     * @param testCaseSetId 用例集ID
     * @return 没有脚本的测试用例数量
     */
    public long countMissingScripts(Long testCaseSetId) {
        logger.debug("Counting missing scripts for test case set: {}", testCaseSetId);
        return testCaseRepository.countMissingScripts(testCaseSetId);
    }

    /**
     * 删除测试用例
     *
     * @param id 测试用例ID
     * @throws IllegalArgumentException 当测试用例不存在时抛出
     */
    @Transactional
    public void deleteTestCase(Long id) {
        logger.info("Deleting test case with ID: {}", id);
        
        if (testCaseRepository.existsById(id)) {
            testCaseRepository.deleteById(id);
            logger.info("Test case deleted successfully: {}", id);
        } else {
            throw new IllegalArgumentException("Test case does not exist");
        }
    }

    /**
     * 根据用例集删除所有测试用例
     *
     * @param testCaseSet 用例集对象
     */
    @Transactional
    public void deleteTestCasesByTestCaseSet(TestCaseSet testCaseSet) {
        logger.info("Deleting all test cases for test case set: {}", testCaseSet.getId());
        testCaseRepository.deleteByTestCaseSet(testCaseSet);
        logger.info("All test cases deleted for test case set: {}", testCaseSet.getId());
    }
}

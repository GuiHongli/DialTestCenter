/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.repository;

import com.huawei.dialtest.center.entity.TestCase;
import com.huawei.dialtest.center.entity.TestCaseSet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 测试用例数据访问层接口
 * 提供测试用例的CRUD操作和查询功能
 * 支持按用例集、用例编号等条件查询
 *
 * @author g00940940
 * @since 2025-09-08
 */
@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

    /**
     * 根据用例集查找所有测试用例
     *
     * @param testCaseSet 用例集对象
     * @return 测试用例列表
     */
    List<TestCase> findByTestCaseSet(TestCaseSet testCaseSet);

    /**
     * 根据用例集ID查找所有测试用例（分页）
     *
     * @param testCaseSetId 用例集ID
     * @param pageable 分页参数
     * @return 测试用例分页数据
     */
    Page<TestCase> findByTestCaseSetId(Long testCaseSetId, Pageable pageable);

    /**
     * 根据用例编号查找测试用例
     *
     * @param caseNumber 用例编号
     * @return 测试用例对象，如果不存在则返回空
     */
    Optional<TestCase> findByCaseNumber(String caseNumber);

    /**
     * 根据用例集和用例编号查找测试用例
     *
     * @param testCaseSet 用例集对象
     * @param caseNumber 用例编号
     * @return 测试用例对象，如果不存在则返回空
     */
    Optional<TestCase> findByTestCaseSetAndCaseNumber(TestCaseSet testCaseSet, String caseNumber);

    /**
     * 查找没有对应脚本的测试用例
     *
     * @param testCaseSetId 用例集ID
     * @return 没有脚本的测试用例列表
     */
    @Query("SELECT tc FROM TestCase tc WHERE tc.testCaseSet.id = :testCaseSetId AND tc.scriptExists = false")
    List<TestCase> findMissingScripts(@Param("testCaseSetId") Long testCaseSetId);

    /**
     * 统计用例集中没有脚本的用例数量
     *
     * @param testCaseSetId 用例集ID
     * @return 没有脚本的用例数量
     */
    @Query("SELECT COUNT(tc) FROM TestCase tc WHERE tc.testCaseSet.id = :testCaseSetId AND tc.scriptExists = false")
    long countMissingScripts(@Param("testCaseSetId") Long testCaseSetId);

    /**
     * 根据用例集删除所有测试用例
     *
     * @param testCaseSet 用例集对象
     */
    void deleteByTestCaseSet(TestCaseSet testCaseSet);
}

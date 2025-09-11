/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.mapper;

import com.huawei.dialtest.center.entity.TestCase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 测试用例Mapper接口
 * 提供测试用例的CRUD操作和查询功能
 * 支持按用例集、用例编号等条件查询
 *
 * @author g00940940
 * @since 2025-09-09
 */
@Mapper
public interface TestCaseMapper {

    /**
     * 根据ID查找测试用例
     *
     * @param id 测试用例ID
     * @return 测试用例对象
     */
    TestCase findById(@Param("id") Long id);

    /**
     * 根据用例集ID查找所有测试用例
     *
     * @param testCaseSetId 用例集ID
     * @return 测试用例列表
     */
    List<TestCase> findByTestCaseSetId(@Param("testCaseSetId") Long testCaseSetId);

    /**
     * 根据用例集ID查找所有测试用例（分页）
     *
     * @param testCaseSetId 用例集ID
     * @param pageNo 页码（从0开始）
     * @param pageSize 每页大小
     * @return 测试用例列表
     */
    List<TestCase> findByTestCaseSetIdWithPage(@Param("testCaseSetId") Long testCaseSetId,
                                              @Param("pageNo") int pageNo,
                                              @Param("pageSize") int pageSize);

    /**
     * 根据用例编号查找测试用例
     *
     * @param caseNumber 用例编号
     * @return 测试用例对象
     */
    TestCase findByCaseNumber(@Param("caseNumber") String caseNumber);

    /**
     * 根据用例集ID和用例编号查找测试用例
     *
     * @param testCaseSetId 用例集ID
     * @param caseNumber 用例编号
     * @return 测试用例对象
     */
    TestCase findByTestCaseSetIdAndCaseNumber(@Param("testCaseSetId") Long testCaseSetId,
                                             @Param("caseNumber") String caseNumber);

    /**
     * 查找没有对应脚本的测试用例
     *
     * @param testCaseSetId 用例集ID
     * @return 没有脚本的测试用例列表
     */
    List<TestCase> findMissingScripts(@Param("testCaseSetId") Long testCaseSetId);

    /**
     * 统计用例集中没有脚本的用例数量
     *
     * @param testCaseSetId 用例集ID
     * @return 没有脚本的用例数量
     */
    long countMissingScripts(@Param("testCaseSetId") Long testCaseSetId);

    /**
     * 统计用例集中的用例总数
     *
     * @param testCaseSetId 用例集ID
     * @return 用例总数
     */
    long countByTestCaseSetId(@Param("testCaseSetId") Long testCaseSetId);

    /**
     * 根据用例集ID删除所有测试用例
     *
     * @param testCaseSetId 用例集ID
     * @return 影响行数
     */
    int deleteByTestCaseSetId(@Param("testCaseSetId") Long testCaseSetId);

    /**
     * 插入测试用例
     *
     * @param testCase 测试用例对象
     * @return 影响行数
     */
    int insert(TestCase testCase);

    /**
     * 更新测试用例
     *
     * @param testCase 测试用例对象
     * @return 影响行数
     */
    int update(TestCase testCase);

    /**
     * 根据ID删除测试用例
     *
     * @param id 测试用例ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
}

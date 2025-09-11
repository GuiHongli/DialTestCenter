/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.mapper;

import com.huawei.dialtest.center.entity.TestCaseSet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用例集Mapper接口，提供用例集相关的数据库操作
 * 包括用例集的查询、创建、更新、删除等基本CRUD操作
 *
 * @author g00940940
 * @since 2025-09-09
 */
@Mapper
public interface TestCaseSetMapper {

    /**
     * 根据ID查找用例集
     *
     * @param id 用例集ID
     * @return 用例集对象
     */
    TestCaseSet findById(@Param("id") Long id);

    /**
     * 根据名称和版本查找用例集
     *
     * @param name 用例集名称
     * @param version 用例集版本
     * @return 用例集对象
     */
    TestCaseSet findByNameAndVersion(@Param("name") String name, @Param("version") String version);

    /**
     * 根据名称查找用例集列表
     *
     * @param name 用例集名称
     * @return 用例集列表
     */
    List<TestCaseSet> findByNameOrderByCreatedTimeDesc(@Param("name") String name);

    /**
     * 根据创建人查找用例集列表
     *
     * @param creator 创建人
     * @return 用例集列表
     */
    List<TestCaseSet> findByCreatorOrderByCreatedTimeDesc(@Param("creator") String creator);

    /**
     * 分页查询用例集列表
     *
     * @param pageNo 页码（从0开始）
     * @param pageSize 每页大小
     * @return 用例集列表
     */
    List<TestCaseSet> findAllByOrderByCreatedTimeDesc(@Param("pageNo") int pageNo, @Param("pageSize") int pageSize);

    /**
     * 检查名称和版本是否存在
     *
     * @param name 用例集名称
     * @param version 用例集版本
     * @return 是否存在
     */
    boolean existsByNameAndVersion(@Param("name") String name, @Param("version") String version);

    /**
     * 统计用例集总数
     *
     * @return 用例集总数
     */
    long count();

    /**
     * 插入用例集
     *
     * @param testCaseSet 用例集对象
     * @return 影响行数
     */
    int insert(TestCaseSet testCaseSet);

    /**
     * 更新用例集
     *
     * @param testCaseSet 用例集对象
     * @return 影响行数
     */
    int update(TestCaseSet testCaseSet);

    /**
     * 根据ID删除用例集
     *
     * @param id 用例集ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
}

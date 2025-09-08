/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.repository;

import com.huawei.dialtest.center.entity.TestCaseSet;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 用例集Repository接口，提供用例集相关的数据库操作
 * 包括用例集的查询、创建、更新、删除等基本CRUD操作
 *
 * @author g00940940
 * @since 2025-09-06
 */
@Repository
public interface TestCaseSetRepository extends JpaRepository<TestCaseSet, Long> {
    /**
     * 根据名称和版本查找用例集
     *
     * @param name 用例集名称
     * @param version 用例集版本
     * @return 用例集对象
     */
    Optional<TestCaseSet> findByNameAndVersion(String name, String version);

    /**
     * 根据名称查找用例集列表
     *
     * @param name 用例集名称
     * @return 用例集列表
     */
    List<TestCaseSet> findByNameOrderByCreatedTimeDesc(String name);

    /**
     * 根据创建人查找用例集列表
     *
     * @param creator 创建人
     * @return 用例集列表
     */
    List<TestCaseSet> findByCreatorOrderByCreatedTimeDesc(String creator);

    /**
     * 分页查询用例集列表
     *
     * @param pageable 分页参数
     * @return 用例集分页数据
     */
    Page<TestCaseSet> findAllByOrderByCreatedTimeDesc(Pageable pageable);

    /**
     * 检查名称和版本是否存在
     *
     * @param name 用例集名称
     * @param version 用例集版本
     * @return 是否存在
     */
    boolean existsByNameAndVersion(String name, String version);
}

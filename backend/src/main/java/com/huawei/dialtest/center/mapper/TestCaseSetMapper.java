/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.mapper;

import com.huawei.dialtest.center.entity.TestCaseSet;
import org.apache.ibatis.annotations.*;

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
    @Select("SELECT * FROM test_case_set WHERE id = #{id}")
    TestCaseSet findById(@Param("id") Long id);

    /**
     * 根据名称和版本查找用例集
     *
     * @param name 用例集名称
     * @param version 用例集版本
     * @return 用例集对象
     */
    @Select("SELECT * FROM test_case_set WHERE name = #{name} AND version = #{version}")
    TestCaseSet findByNameAndVersion(@Param("name") String name, @Param("version") String version);

    /**
     * 根据名称查找用例集列表
     *
     * @param name 用例集名称
     * @return 用例集列表
     */
    @Select("SELECT * FROM test_case_set WHERE name = #{name} ORDER BY created_time DESC")
    List<TestCaseSet> findByNameOrderByCreatedTimeDesc(@Param("name") String name);

    /**
     * 根据创建人查找用例集列表
     *
     * @param creator 创建人
     * @return 用例集列表
     */
    @Select("SELECT * FROM test_case_set WHERE creator = #{creator} ORDER BY created_time DESC")
    List<TestCaseSet> findByCreatorOrderByCreatedTimeDesc(@Param("creator") String creator);

    /**
     * 分页查询用例集列表
     *
     * @param pageNo 页码（从0开始）
     * @param pageSize 每页大小
     * @return 用例集列表
     */
    @Select("SELECT * FROM test_case_set ORDER BY created_time DESC LIMIT #{pageSize} OFFSET #{pageNo}")
    List<TestCaseSet> findAllByOrderByCreatedTimeDesc(@Param("pageNo") int pageNo, @Param("pageSize") int pageSize);

    /**
     * 检查名称和版本是否存在
     *
     * @param name 用例集名称
     * @param version 用例集版本
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM test_case_set WHERE name = #{name} AND version = #{version}")
    boolean existsByNameAndVersion(@Param("name") String name, @Param("version") String version);

    /**
     * 统计用例集总数
     *
     * @return 用例集总数
     */
    @Select("SELECT COUNT(*) FROM test_case_set")
    long count();

    /**
     * 插入用例集
     *
     * @param testCaseSet 用例集对象
     * @return 影响行数
     */
    @Insert("INSERT INTO test_case_set (name, version, description, file_content, file_format, file_size, sha512, creator, created_time, updated_time) " +
            "VALUES (#{name}, #{version}, #{description}, #{fileContent}, #{fileFormat}, #{fileSize}, #{sha512}, #{creator}, #{createdTime}, #{updatedTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TestCaseSet testCaseSet);

    /**
     * 更新用例集
     *
     * @param testCaseSet 用例集对象
     * @return 影响行数
     */
    @Update("UPDATE test_case_set SET name = #{name}, version = #{version}, description = #{description}, " +
            "file_content = #{fileContent}, file_format = #{fileFormat}, file_size = #{fileSize}, " +
            "sha512 = #{sha512}, creator = #{creator}, updated_time = #{updatedTime} WHERE id = #{id}")
    int update(TestCaseSet testCaseSet);

    /**
     * 根据ID删除用例集
     *
     * @param id 用例集ID
     * @return 影响行数
     */
    @Delete("DELETE FROM test_case_set WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}

/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.repository;

import com.huawei.dialtest.center.entity.SoftwarePackage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 软件包数据访问层接口
 * 提供软件包实体的基本CRUD操作和自定义查询方法
 * 支持按平台、创建者、软件名称等条件进行查询
 *
 * @author g00940940
 * @since 2025-01-15
 */
@Repository
public interface SoftwarePackageRepository extends JpaRepository<SoftwarePackage, Long> {

    /**
     * 根据软件名称查找软件包
     *
     * @param softwareName 软件名称
     * @return 软件包对象，如果不存在则返回空
     */
    Optional<SoftwarePackage> findBySoftwareName(String softwareName);

    /**
     * 根据SHA512哈希值查找软件包
     *
     * @param sha512 SHA512哈希值
     * @return 软件包对象，如果不存在则返回空
     */
    Optional<SoftwarePackage> findBySha512(String sha512);

    /**
     * 检查软件名称是否已存在
     *
     * @param softwareName 软件名称
     * @return 如果存在返回true，否则返回false
     */
    boolean existsBySoftwareName(String softwareName);

    /**
     * 检查SHA512哈希值是否已存在
     *
     * @param sha512 SHA512哈希值
     * @return 如果存在返回true，否则返回false
     */
    boolean existsBySha512(String sha512);

    /**
     * 根据平台查找软件包列表（分页）
     *
     * @param platform 平台（android或ios）
     * @param pageable 分页参数
     * @return 软件包分页数据
     */
    Page<SoftwarePackage> findByPlatformOrderByCreatedTimeDesc(String platform, Pageable pageable);

    /**
     * 根据创建者查找软件包列表（分页）
     *
     * @param creator 创建者
     * @param pageable 分页参数
     * @return 软件包分页数据
     */
    Page<SoftwarePackage> findByCreatorOrderByCreatedTimeDesc(String creator, Pageable pageable);

    /**
     * 根据软件名称模糊查询软件包列表（分页）
     *
     * @param softwareName 软件名称（支持模糊查询）
     * @param pageable 分页参数
     * @return 软件包分页数据
     */
    Page<SoftwarePackage> findBySoftwareNameContainingIgnoreCaseOrderByCreatedTimeDesc(String softwareName, Pageable pageable);

    /**
     * 根据平台和软件名称模糊查询软件包列表（分页）
     *
     * @param platform 平台
     * @param softwareName 软件名称（支持模糊查询）
     * @param pageable 分页参数
     * @return 软件包分页数据
     */
    Page<SoftwarePackage> findByPlatformAndSoftwareNameContainingIgnoreCaseOrderByCreatedTimeDesc(
            String platform, String softwareName, Pageable pageable);

    /**
     * 获取所有软件包列表（分页，按创建时间倒序）
     *
     * @param pageable 分页参数
     * @return 软件包分页数据
     */
    Page<SoftwarePackage> findAllByOrderByCreatedTimeDesc(Pageable pageable);

    /**
     * 根据平台获取软件包数量
     *
     * @param platform 平台
     * @return 软件包数量
     */
    long countByPlatform(String platform);

    /**
     * 根据创建者获取软件包数量
     *
     * @param creator 创建者
     * @return 软件包数量
     */
    long countByCreator(String creator);

    /**
     * 获取所有软件包数量
     *
     * @return 软件包总数量
     */
    long count();

    /**
     * 根据平台获取软件包列表（不分页）
     *
     * @param platform 平台
     * @return 软件包列表
     */
    List<SoftwarePackage> findByPlatformOrderByCreatedTimeDesc(String platform);

    /**
     * 根据创建者获取软件包列表（不分页）
     *
     * @param creator 创建者
     * @return 软件包列表
     */
    List<SoftwarePackage> findByCreatorOrderByCreatedTimeDesc(String creator);

    /**
     * 根据软件名称模糊查询软件包列表（不分页）
     *
     * @param softwareName 软件名称（支持模糊查询）
     * @return 软件包列表
     */
    List<SoftwarePackage> findBySoftwareNameContainingIgnoreCaseOrderByCreatedTimeDesc(String softwareName);

    /**
     * 自定义查询：根据多个条件查询软件包
     *
     * @param platform 平台（可选）
     * @param creator 创建者（可选）
     * @param softwareName 软件名称（可选，支持模糊查询）
     * @param pageable 分页参数
     * @return 软件包分页数据
     */
    @Query("SELECT sp FROM SoftwarePackage sp WHERE " +
           "(:platform IS NULL OR sp.platform = :platform) AND " +
           "(:creator IS NULL OR sp.creator = :creator) AND " +
           "(:softwareName IS NULL OR LOWER(sp.softwareName) LIKE LOWER(CONCAT('%', :softwareName, '%'))) " +
           "ORDER BY sp.createdTime DESC")
    Page<SoftwarePackage> findByConditions(@Param("platform") String platform,
                                          @Param("creator") String creator,
                                          @Param("softwareName") String softwareName,
                                          Pageable pageable);
}

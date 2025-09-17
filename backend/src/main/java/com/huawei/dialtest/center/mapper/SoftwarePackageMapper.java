/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.mapper;

import com.huawei.dialtest.center.entity.SoftwarePackage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 软件包Mapper接口
 * 提供软件包实体的基本CRUD操作和自定义查询方法
 * 支持按平台、创建者、软件名称等条件进行查询
 *
 * @author g00940940
 * @since 2025-09-09
 */
@Mapper
public interface SoftwarePackageMapper {

    /**
     * 根据ID查找软件包
     *
     * @param id 软件包ID
     * @return 软件包对象
     */
    @Select("SELECT * FROM software_package WHERE id = #{id}")
    SoftwarePackage findById(@Param("id") Long id);

    /**
     * 根据软件名称查找软件包
     *
     * @param softwareName 软件名称
     * @return 软件包对象
     */
    @Select("SELECT * FROM software_package WHERE software_name = #{softwareName}")
    SoftwarePackage findBySoftwareName(@Param("softwareName") String softwareName);

    /**
     * 根据SHA512哈希值查找软件包
     *
     * @param sha512 SHA512哈希值
     * @return 软件包对象
     */
    @Select("SELECT * FROM software_package WHERE sha512 = #{sha512}")
    SoftwarePackage findBySha512(@Param("sha512") String sha512);

    /**
     * 检查软件名称是否已存在
     *
     * @param softwareName 软件名称
     * @return 如果存在返回true，否则返回false
     */
    @Select("SELECT COUNT(*) > 0 FROM software_package WHERE software_name = #{softwareName}")
    boolean existsBySoftwareName(@Param("softwareName") String softwareName);

    /**
     * 检查SHA512哈希值是否已存在
     *
     * @param sha512 SHA512哈希值
     * @return 如果存在返回true，否则返回false
     */
    @Select("SELECT COUNT(*) > 0 FROM software_package WHERE sha512 = #{sha512}")
    boolean existsBySha512(@Param("sha512") String sha512);

    /**
     * 根据平台查找软件包列表（分页）
     *
     * @param platform 平台（android或ios）
     * @param pageNo 页码（从0开始）
     * @param pageSize 每页大小
     * @return 软件包列表
     */
    @Select("SELECT * FROM software_package WHERE platform = #{platform} ORDER BY created_time DESC LIMIT #{pageSize} OFFSET #{pageNo}")
    List<SoftwarePackage> findByPlatformOrderByCreatedTimeDesc(@Param("platform") String platform,
                                                              @Param("pageNo") int pageNo,
                                                              @Param("pageSize") int pageSize);

    /**
     * 根据创建者查找软件包列表（分页）
     *
     * @param creator 创建者
     * @param pageNo 页码（从0开始）
     * @param pageSize 每页大小
     * @return 软件包列表
     */
    @Select("SELECT * FROM software_package WHERE creator = #{creator} ORDER BY created_time DESC LIMIT #{pageSize} OFFSET #{pageNo}")
    List<SoftwarePackage> findByCreatorOrderByCreatedTimeDesc(@Param("creator") String creator,
                                                             @Param("pageNo") int pageNo,
                                                             @Param("pageSize") int pageSize);

    /**
     * 根据软件名称模糊查询软件包列表（分页）
     *
     * @param softwareName 软件名称（支持模糊查询）
     * @param pageNo 页码（从0开始）
     * @param pageSize 每页大小
     * @return 软件包列表
     */
    @Select("SELECT * FROM software_package WHERE LOWER(software_name) LIKE LOWER(CONCAT('%', #{softwareName}, '%')) ORDER BY created_time DESC LIMIT #{pageSize} OFFSET #{pageNo}")
    List<SoftwarePackage> findBySoftwareNameContainingIgnoreCaseOrderByCreatedTimeDesc(@Param("softwareName") String softwareName,
                                                                                       @Param("pageNo") int pageNo,
                                                                                       @Param("pageSize") int pageSize);

    /**
     * 根据平台和软件名称模糊查询软件包列表（分页）
     *
     * @param platform 平台
     * @param softwareName 软件名称（支持模糊查询）
     * @param pageNo 页码（从0开始）
     * @param pageSize 每页大小
     * @return 软件包列表
     */
    @Select("SELECT * FROM software_package WHERE platform = #{platform} AND LOWER(software_name) LIKE LOWER(CONCAT('%', #{softwareName}, '%')) ORDER BY created_time DESC LIMIT #{pageSize} OFFSET #{pageNo}")
    List<SoftwarePackage> findByPlatformAndSoftwareNameContainingIgnoreCaseOrderByCreatedTimeDesc(@Param("platform") String platform,
                                                                                                  @Param("softwareName") String softwareName,
                                                                                                  @Param("pageNo") int pageNo,
                                                                                                  @Param("pageSize") int pageSize);

    /**
     * 获取所有软件包列表（分页，按创建时间倒序）
     *
     * @param pageNo 页码（从0开始）
     * @param pageSize 每页大小
     * @return 软件包列表
     */
    @Select("SELECT * FROM software_package ORDER BY created_time DESC LIMIT #{pageSize} OFFSET #{pageNo}")
    List<SoftwarePackage> findAllByOrderByCreatedTimeDesc(@Param("pageNo") int pageNo, @Param("pageSize") int pageSize);

    /**
     * 根据多个条件查询软件包
     *
     * @param platform 平台（可选）
     * @param creator 创建者（可选）
     * @param softwareName 软件名称（可选，支持模糊查询）
     * @param pageNo 页码（从0开始）
     * @param pageSize 每页大小
     * @return 软件包列表
     */
    @SelectProvider(type = SoftwarePackageSqlProvider.class, method = "findByConditions")
    List<SoftwarePackage> findByConditions(@Param("platform") String platform,
                                          @Param("creator") String creator,
                                          @Param("softwareName") String softwareName,
                                          @Param("pageNo") int pageNo,
                                          @Param("pageSize") int pageSize);

    /**
     * 根据平台获取软件包数量
     *
     * @param platform 平台
     * @return 软件包数量
     */
    @Select("SELECT COUNT(*) FROM software_package WHERE platform = #{platform}")
    long countByPlatform(@Param("platform") String platform);

    /**
     * 根据创建者获取软件包数量
     *
     * @param creator 创建者
     * @return 软件包数量
     */
    @Select("SELECT COUNT(*) FROM software_package WHERE creator = #{creator}")
    long countByCreator(@Param("creator") String creator);

    /**
     * 根据条件统计软件包数量
     *
     * @param platform 平台（可选）
     * @param creator 创建者（可选）
     * @param softwareName 软件名称（可选，支持模糊查询）
     * @return 软件包数量
     */
    @SelectProvider(type = SoftwarePackageSqlProvider.class, method = "countByConditions")
    long countByConditions(@Param("platform") String platform,
                          @Param("creator") String creator,
                          @Param("softwareName") String softwareName);

    /**
     * 获取所有软件包数量
     *
     * @return 软件包总数量
     */
    @Select("SELECT COUNT(*) FROM software_package")
    long count();

    /**
     * 根据平台获取软件包列表（不分页）
     *
     * @param platform 平台
     * @return 软件包列表
     */
    @Select("SELECT * FROM software_package WHERE platform = #{platform} ORDER BY created_time DESC")
    List<SoftwarePackage> findByPlatformOrderByCreatedTimeDescNoPage(@Param("platform") String platform);

    /**
     * 根据创建者获取软件包列表（不分页）
     *
     * @param creator 创建者
     * @return 软件包列表
     */
    @Select("SELECT * FROM software_package WHERE creator = #{creator} ORDER BY created_time DESC")
    List<SoftwarePackage> findByCreatorOrderByCreatedTimeDescNoPage(@Param("creator") String creator);

    /**
     * 根据软件名称模糊查询软件包列表（不分页）
     *
     * @param softwareName 软件名称（支持模糊查询）
     * @return 软件包列表
     */
    @Select("SELECT * FROM software_package WHERE LOWER(software_name) LIKE LOWER(CONCAT('%', #{softwareName}, '%')) ORDER BY created_time DESC")
    List<SoftwarePackage> findBySoftwareNameContainingIgnoreCaseOrderByCreatedTimeDescNoPage(@Param("softwareName") String softwareName);

    /**
     * 插入软件包
     *
     * @param softwarePackage 软件包对象
     * @return 影响行数
     */
    @Insert("INSERT INTO software_package (software_name, platform, file_content, file_size, sha512, creator, created_time) " +
            "VALUES (#{softwareName}, #{platform}, #{fileContent}, #{fileSize}, #{sha512}, #{creator}, #{createdTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SoftwarePackage softwarePackage);

    /**
     * 更新软件包
     *
     * @param softwarePackage 软件包对象
     * @return 影响行数
     */
    @Update("UPDATE software_package SET software_name = #{softwareName}, platform = #{platform}, " +
            "file_content = #{fileContent}, file_size = #{fileSize}, sha512 = #{sha512}, " +
            "creator = #{creator} WHERE id = #{id}")
    int update(SoftwarePackage softwarePackage);

    /**
     * 根据ID删除软件包
     *
     * @param id 软件包ID
     * @return 影响行数
     */
    @Delete("DELETE FROM software_package WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}

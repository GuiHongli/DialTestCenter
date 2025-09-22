package com.huawei.cloududn.dialingtest.dao;

import com.huawei.cloududn.dialingtest.model.Role;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色定义DAO
 */
@Repository
@Mapper
public interface RoleDao {
    
    /**
     * 根据角色代码查找角色
     */
    @Select("SELECT id, code, name_zh, name_en, description_zh, description_en FROM roles WHERE code = #{code}")
    Role findByCode(@Param("code") String code);
    
    /**
     * 获取所有角色
     */
    @Select("SELECT id, code, name_zh, name_en, description_zh, description_en FROM roles ORDER BY id")
    List<Role> findAll();
    
    /**
     * 插入角色
     */
    @Insert("INSERT INTO roles (code, name_zh, name_en, description_zh, description_en) VALUES (#{code}, #{nameZh}, #{nameEn}, #{descriptionZh}, #{descriptionEn})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Role role);
}

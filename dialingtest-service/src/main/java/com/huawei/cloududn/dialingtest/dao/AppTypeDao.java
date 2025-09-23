package com.huawei.cloududn.dialingtest.dao;

import com.huawei.cloududn.dialingtest.model.AppType;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 应用类型数据访问对象
 * 
 * @author Generated
 */
@Mapper
public interface AppTypeDao {
    
    /**
     * 插入应用类型
     */
    @Insert("INSERT INTO app_type (business_category, app_name, description) VALUES (#{businessCategory}, #{appName}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AppType appType);
    
    /**
     * 根据业务大类和应用名称查询应用类型
     */
    @Select("SELECT * FROM app_type WHERE business_category = #{businessCategory} AND app_name = #{appName}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "businessCategory", column = "business_category"),
        @Result(property = "appName", column = "app_name"),
        @Result(property = "description", column = "description")
    })
    AppType findByBusinessCategoryAndAppName(@Param("businessCategory") String businessCategory, 
                                           @Param("appName") String appName);
    
    /**
     * 查询所有应用类型
     */
    @Select("SELECT * FROM app_type ORDER BY business_category, app_name")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "businessCategory", column = "business_category"),
        @Result(property = "appName", column = "app_name"),
        @Result(property = "description", column = "description")
    })
    List<AppType> findAll();
    
    /**
     * 根据业务大类查询应用类型
     */
    @Select("SELECT * FROM app_type WHERE business_category = #{businessCategory} ORDER BY app_name")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "businessCategory", column = "business_category"),
        @Result(property = "appName", column = "app_name"),
        @Result(property = "description", column = "description")
    })
    List<AppType> findByBusinessCategory(String businessCategory);
    
    /**
     * 检查业务大类和应用名称是否存在
     */
    @Select("SELECT COUNT(*) FROM app_type WHERE business_category = #{businessCategory} AND app_name = #{appName}")
    int existsByBusinessCategoryAndAppName(@Param("businessCategory") String businessCategory, 
                                         @Param("appName") String appName);
    
    /**
     * 更新应用类型描述
     */
    @Update("UPDATE app_type SET description = #{description} WHERE business_category = #{businessCategory} AND app_name = #{appName}")
    int updateDescription(@Param("businessCategory") String businessCategory, 
                         @Param("appName") String appName, 
                         @Param("description") String description);
    
    /**
     * 删除应用类型
     */
    @Delete("DELETE FROM app_type WHERE business_category = #{businessCategory} AND app_name = #{appName}")
    int deleteByBusinessCategoryAndAppName(@Param("businessCategory") String businessCategory, 
                                         @Param("appName") String appName);
}

/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis配置类
 * 配置SqlSessionFactory和Mapper扫描
 *
 * @author g00940940
 * @since 2025-09-09
 */
@Configuration
@MapperScan("com.huawei.dialtest.center.mapper")
public class MyBatisConfig {

    @Autowired
    private DataSource dataSource;

    /**
     * 配置SqlSessionFactory
     *
     * @return SqlSessionFactory
     * @throws Exception 配置异常
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        // 设置MyBatis配置文件
        sessionFactory.setConfigLocation(
                new PathMatchingResourcePatternResolver().getResource("classpath:mybatis-config.xml"));
        
        // 设置Mapper XML文件位置
        sessionFactory.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
        
        return sessionFactory.getObject();
    }
}

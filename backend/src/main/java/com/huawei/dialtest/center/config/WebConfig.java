/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Web MVC配置类
 * 配置静态资源处理和前端路由
 *
 * @author g00940940
 * @since 2025-01-27
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源处理器
     *
     * @param registry ResourceHandlerRegistry对象
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        return requestedResource.exists() && requestedResource.isReadable() 
                            ? requestedResource 
                            : new ClassPathResource("/static/index.html");
                    }
                });
    }

    /**
     * 配置视图控制器
     * 处理前端路由，所有非API请求都返回index.html
     *
     * @param registry ViewControllerRegistry对象
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 处理前端路由，所有非API请求都返回index.html
        registry.addViewController("/users")
                .setViewName("forward:/index.html");
        
        registry.addViewController("/user-roles")
                .setViewName("forward:/index.html");
        
        registry.addViewController("/test-case-sets")
                .setViewName("forward:/index.html");
        
        registry.addViewController("/software-packages")
                .setViewName("forward:/index.html");
        
        registry.addViewController("/operation-logs")
                .setViewName("forward:/index.html");
        
        registry.addViewController("/dashboard")
                .setViewName("forward:/index.html");
    }
}

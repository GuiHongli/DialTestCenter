package com.huawei.cloududn.dialingtest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Web配置类
 * 配置静态资源处理和SPA路由支持
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源处理器
     * 支持前端SPA应用的路由回退
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        
                        // 如果请求的资源存在，直接返回
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        
                        // 对于SPA应用，所有非API请求都返回index.html
                        if (!resourcePath.startsWith("api/") && !resourcePath.startsWith("dialingtest/api/")) {
                            return new ClassPathResource("/static/index.html");
                        }
                        
                        return null;
                    }
                });
    }

    /**
     * 配置默认视图控制器
     * 将根路径重定向到前端应用
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 根路径重定向到前端应用
        registry.addRedirectViewController("/", "/index.html");
    }
}
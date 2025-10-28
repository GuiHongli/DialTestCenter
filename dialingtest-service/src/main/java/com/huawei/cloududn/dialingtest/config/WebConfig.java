package com.huawei.cloududn.dialingtest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 配置静态资源处理和SPA路由支持
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源处理器
     * HashRouter 模式下，hash 后的路径不会被发送到服务器，所以简化处理逻辑
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(false);
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
package com.huawei.cloududn.dialingtest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 配置跨域访问等Web相关设置
 * 
 * @author DialTestCenter
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置跨域访问
     * 
     * @param registry CORS注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "https://localhost:3000",
                    "https://localhost:5173", 
                    "https://localhost:4396",
                    "http://localhost:3000",
                    "http://localhost:5173",
                    "http://localhost:4396"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

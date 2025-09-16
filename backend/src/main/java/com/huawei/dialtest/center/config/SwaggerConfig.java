/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger 2配置类
 * 提供API文档生成和访问功能
 *
 * @author g00940940
 * @since 2025-01-27
 */
// @Configuration
// @EnableSwagger2
public class SwaggerConfig {

    /**
     * 创建API文档Docket
     *
     * @return Docket对象
     */
    // @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.huawei.dialtest.center.controller"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    /**
     * 配置API信息
     *
     * @return API信息
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Dial Test Center API")
                .description("拨测控制中心API文档")
                .version("1.0.0")
                .contact(new Contact("g00940940", "", "g00940940@huawei.com"))
                .build();
    }
}

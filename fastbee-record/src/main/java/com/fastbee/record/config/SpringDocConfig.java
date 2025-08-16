package com.fastbee.record.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lin
 */
@Configuration
public class SpringDocConfig {

    @Value("${doc.enabled: true}")
    private boolean enable;

    @Bean
    public OpenAPI springShopOpenApi() {
        Contact contact = new Contact();

        return new OpenAPI()
                .info(new Info().title("ZLM录像助手 接口文档")
                        .contact(contact)
                        .description("录像助手，补充ZLM功能")
                        .version("v2.0")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }

    /**
     * 添加分组
     * @return
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("1. 全部")
                .packagesToScan("com.fastbee.record")
                .build();
    }
}

package com.jhl.silver.union.biz.config;

import com.jhl.silver.union.commons.web.handler.GlobalExceptionHandler;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import jakarta.annotation.PostConstruct;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author tangyingkai
 */
@Configuration
@Profile({ "default", "dev", "test", "sit", "uat" })
public class OpenApiConfig {
    /**
     * 使用swagger时， 需要扫描的基本包名。（定义的包名及其所有子包均被扫描）
     */
    private static final String SWAGGER_BASE_PACKAGE = "com.jhl.silver.union.web";

    @PostConstruct
    public void preprocess() {
        //在返回错误时信息时， 返回明细信息
        GlobalExceptionHandler.RETURN_ADDITIONAL_INFO = true;
    }

    @Bean
    public GroupedOpenApi publicApi() {

        return GroupedOpenApi.builder()
                .group("public-api")       // 分组名称
                // .pathsToMatch("/*")   // 匹配的接口路径
                .packagesToScan(SWAGGER_BASE_PACKAGE)
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("silver-union API 文档")
                        .version("1.0")
                        .description("用于WEB前端调用")
                        .contact(new Contact()
                                .name("技术支持")
                                .email("support@example.com")))
                ;
    }
}

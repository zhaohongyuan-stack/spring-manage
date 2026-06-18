package com.fengrui.frmanage.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 配置：使用相对路径作为 server，避免 Swagger 经 Nginx 代理时跨域请求 8080。
 */
@Configuration
public class OpenApiConfig {

    /**
     * 自定义 OpenAPI 文档元信息与服务地址。
     *
     * @return OpenAPI 实例
     */
    @Bean
    public OpenAPI frManageOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FrManage 酒店进销存 API")
                        .description("基础信息、采购、入库、领用、库存、字典等接口")
                        .version("v1"))
                .servers(List.of(
                        new Server().url("/").description("当前访问地址（推荐 Nginx http://localhost:8081 或直连 http://localhost:8080）")
                ));
    }
}

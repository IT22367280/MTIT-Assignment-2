package com.foodordering.orderservice;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderServiceOpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .version("v1")
                        .description("Manage customer orders and track their lifecycle statuses."))
                .servers(List.of(
                        new Server()
                                .url("http://127.0.0.1:8083")
                                .description("Direct order service access"),
                        new Server()
                                .url("http://127.0.0.1:8080/api")
                                .description("Order service via API gateway")
                ));
    }
}

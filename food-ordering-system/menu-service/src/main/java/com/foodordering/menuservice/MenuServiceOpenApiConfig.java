package com.foodordering.menuservice;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MenuServiceOpenApiConfig {

    @Bean
    public OpenAPI menuServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Menu Service API")
                        .version("v1")
                        .description("Manage food and beverage items that can be ordered by customers."))
                .servers(List.of(
                        new Server()
                                .url("http://127.0.0.1:8082")
                                .description("Direct menu service access"),
                        new Server()
                                .url("http://127.0.0.1:8080/api/menu")
                                .description("Menu service via API gateway")
                ));
    }
}

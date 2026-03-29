package com.foodordering.customerservice;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerServiceOpenApiConfig {

    @Bean
    public OpenAPI customerServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Customer Service API")
                        .version("v1")
                        .description("Manage customer profiles, contact information, and delivery addresses."))
                .servers(List.of(
                        new Server()
                                .url("http://127.0.0.1:8081")
                                .description("Direct customer service access"),
                        new Server()
                                .url("http://127.0.0.1:8080/api")
                                .description("Customer service via API gateway")
                ));
    }
}

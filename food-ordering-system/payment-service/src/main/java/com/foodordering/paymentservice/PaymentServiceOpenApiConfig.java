package com.foodordering.paymentservice;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentServiceOpenApiConfig {

    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payment Service API")
                        .version("v1")
                        .description("Manage payment records and settlement status for placed orders."))
                .servers(List.of(
                        new Server()
                                .url("http://127.0.0.1:8084")
                                .description("Direct payment service access"),
                        new Server()
                                .url("http://127.0.0.1:8080/api")
                                .description("Payment service via API gateway")
                ));
    }
}

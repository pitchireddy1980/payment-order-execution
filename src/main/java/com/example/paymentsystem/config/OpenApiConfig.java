package com.example.paymentsystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentSystemOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080/api");
        devServer.setDescription("Development Server");

        Contact contact = new Contact();
        contact.setEmail("support@paymentsystem.com");
        contact.setName("Payment System Support");
        contact.setUrl("https://www.paymentsystem.com");

        License license = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Payment Order and Execution System API")
                .version("1.0.0")
                .contact(contact)
                .description("RESTful API for managing payment orders and executions")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
}

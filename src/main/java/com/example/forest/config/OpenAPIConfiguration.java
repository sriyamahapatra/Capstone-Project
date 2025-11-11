package com.example.forest.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPIConfiguration.java
 *
 * Configures Swagger/OpenAPI documentation for the Forest application.
 * This setup helps generate interactive API documentation via Swagger UI.
 */
@OpenAPIDefinition
@Configuration
public class OpenAPIConfiguration {

    /**
     * Defines the OpenAPI bean configuration.
     * This bean customizes the metadata displayed in the Swagger UI,
     * including title, version, description, and external documentation links.
     *
     * @return an OpenAPI object containing project documentation metadata
     */
    @Bean
    public OpenAPI forestAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Capstone Project")
                        .description("API for Forest Application")
                        .version("v1.0")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Sourabh Jain, sourabh.jain13112002@gmail.com")
                        .url("https://www.bento.me/sourabh-jain"));
    }
}

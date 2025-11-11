package com.example.forest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig.java
 *
 * Configures CORS (Cross-Origin Resource Sharing) and resource handling.
 * This class enables the frontend (e.g., Angular on localhost:4200)
 * to communicate with the Spring Boot backend, and also ensures
 * that Swagger UI and WebJARs resources are served correctly.
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    /**
     * Defines global CORS configuration.
     * Allows requests from the frontend domain to access backend APIs.
     */
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:4200") // Frontend URL
                .allowedMethods("GET", "POST", "PUT", "DELETE") // Allowed HTTP methods
                .maxAge(3600L) // Cache CORS preflight for 1 hour
                .allowedHeaders("*") // Allow all headers
                .exposedHeaders("Authorization") // Expose JWT tokens in response headers
                .allowCredentials(true); // Allow cookies and authorization headers
    }

    /**
     * Maps resource locations for Swagger UI and WebJARs.
     * This enables serving the Swagger documentation interface.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Handler for the Swagger UI HTML file
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        // Handler for static resources required by Swagger (CSS, JS)
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}

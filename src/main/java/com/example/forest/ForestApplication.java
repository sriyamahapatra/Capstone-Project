package com.example.forest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Forest Spring Boot application.
 *
 * <p>
 * This class enables asynchronous execution and scheduled tasks,
 * allowing the application to run background jobs like:
 * - Sending emails asynchronously
 * - Cleaning up expired tokens daily
 * </p>
 */
@SpringBootApplication
@EnableAsync       // Enables @Async for asynchronous method execution
@EnableScheduling  // Enables @Scheduled for background jobs
public class ForestApplication {

	public static void main(String[] args) {
		SpringApplication.run(ForestApplication.class, args);
		System.out.println("🌲 Forest Application started successfully!");
	}
}

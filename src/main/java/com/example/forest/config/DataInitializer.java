package com.example.forest.config;

import com.example.forest.document.MongoUserDocument;
import com.example.forest.model.Role;
import com.example.forest.repository.mongodb.MongoUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * DataInitializer.java
 *
 * This component runs automatically when the Spring Boot application starts.
 * It checks if an admin user exists in MongoDB — if not, it creates one.
 * This ensures that there is always a default administrator account available.
 *
 */
@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {

    // Repository for interacting with MongoDB user collection
    private final MongoUserRepository userRepository;

    // Password encoder used to securely hash the default admin password
    private final PasswordEncoder passwordEncoder;

    /**
     * The run() method is automatically executed at application startup.
     * It checks for the existence of an "admin" user in MongoDB,
     * and if missing, creates a default admin account.
     */
    @Override
    public void run(String... args) {

        // Load admin username from environment variables, with a fallback to "admin"
        String adminUsername = System.getenv().getOrDefault("DEFAULT_ADMIN_USERNAME", "admin");

        // Find all users with the admin username
        List<MongoUserDocument> adminUsers = userRepository.findAllByUsername(adminUsername);

        if (adminUsers.isEmpty()) {
            // If no admin user exists, create one
            String email = System.getenv().getOrDefault("DEFAULT_ADMIN_EMAIL", "admin@forest.com");
            String password = System.getenv().getOrDefault("DEFAULT_ADMIN_PASSWORD", "admin");

            MongoUserDocument newAdmin = new MongoUserDocument();
            newAdmin.setUsername(adminUsername);
            newAdmin.setEmail(email);
            newAdmin.setPassword(passwordEncoder.encode(password));
            newAdmin.setCreated(Instant.now());
            newAdmin.setRole(Role.ADMIN);
            newAdmin.setEnabled(true);

            userRepository.save(newAdmin);
        } else {
            // If one or more admin users exist, use the first one and delete the rest
            MongoUserDocument admin = adminUsers.get(0);
            boolean needsUpdate = false;

            if (!admin.isEnabled()) {
                admin.setEnabled(true);
                needsUpdate = true;
            }
            if (admin.getRole() != Role.ADMIN) {
                admin.setRole(Role.ADMIN);
                needsUpdate = true;
            }
            if (needsUpdate) {
                userRepository.save(admin);
            }

            // Delete any extra admin users
            if (adminUsers.size() > 1) {
                for (int i = 1; i < adminUsers.size(); i++) {
                    userRepository.delete(adminUsers.get(i));
                }
            }
        }
    }
}

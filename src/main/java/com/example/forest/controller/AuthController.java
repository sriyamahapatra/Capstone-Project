package com.example.forest.controller;

import com.example.forest.Exceptions.ValidationExceptions;
import com.example.forest.dto.*;
import com.example.forest.repository.mongodb.MongoUserRepository;
import com.example.forest.service.RefreshTokenService;
import com.example.forest.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

import static org.springframework.http.HttpStatus.OK;

/**
 * AuthController.java
 *
 * Handles all authentication-related operations, including:
 * registration, login, logout, token refresh, password management,
 * and user interests handling.
 *
 * Base endpoint: /api/v1/auth
 */
@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final MongoUserRepository userRepository;

    /**
     * Registers a new user in the system.
     * Validates input data, checks for existing username/email,
     * and delegates user creation to AuthService.
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest,
                                           BindingResult bindingResult) {

        // Check for validation errors in request body
        Optional<String> validationErrors = ValidationExceptions.processValidationErrors(bindingResult);
        if (validationErrors.isPresent()) {
            return new ResponseEntity<>(validationErrors.get(), HttpStatus.BAD_REQUEST);
        }

        // Check for existing username or email
        boolean usernamePresent = userRepository.findByUsername(registerRequest.getUsername()).isPresent();
        boolean emailPresent = userRepository.findByEmail(registerRequest.getEmail()).isPresent();

        if (usernamePresent || emailPresent) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    usernamePresent
                            ? "Username " + registerRequest.getUsername() + " is already taken."
                            : registerRequest.getEmail() + " is taken."
            );
        }

        // Register the new user
        authService.register(registerRequest);
        return new ResponseEntity<>("User Registration Successful", HttpStatus.OK);
    }

    /**
     * Verifies a newly registered user's account using a token.
     * Redirects the user to the frontend login page upon success.
     */
    @GetMapping("accountVerification/{token}")
    public ResponseEntity<String> verifyAccount(@PathVariable String token) {
        authService.verifyAccount(token);

        // Redirect user to frontend login page after verification
        String frontendLoginUrl = "http://localhost:4200/login";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", frontendLoginUrl);
        headers.add("X-Redirection-Flag", "true");

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /**
     * Authenticates a user using credentials (username/password).
     * Returns JWT access and refresh tokens upon success.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    /**
     * Refreshes the access token using a valid refresh token.
     */
    @PostMapping("/refresh/token")
    public AuthenticationResponse refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        System.out.println("Refresh token triggered: " + refreshTokenRequest);
        return authService.refreshToken(refreshTokenRequest);
    }

    /**
     * Logs the user out by deleting the refresh token from the database.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        refreshTokenService.deleteRefreshToken(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.status(OK).body("Refresh Token Deleted Successfully!!");
    }

    /**
     * Initiates a password reset process by sending a reset link to the user's email.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody String email) {
        authService.forgotPassword(email);
        return new ResponseEntity<>("Password reset link sent to your email.", HttpStatus.OK);
    }

    /**
     * Resets the user's password using a valid token and a new password.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestBody String newPassword) {
        authService.resetPassword(token, newPassword);
        return new ResponseEntity<>("Password reset successfully.", HttpStatus.OK);
    }

    /**
     * Saves or updates the user's selected interests for personalization.
     */
    @PostMapping("/interests")
    public ResponseEntity<String> saveInterests(@RequestBody Set<String> interests) {
        authService.saveInterests(interests);
        return new ResponseEntity<>("Interests saved successfully.", HttpStatus.OK);
    }
}

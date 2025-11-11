package com.example.forest.service;

import com.example.forest.Exceptions.CustomException;
import com.example.forest.document.MongoUserDocument;
import com.example.forest.document.MongoVerificationTokenDocument;
import com.example.forest.dto.*;
import com.example.forest.model.NotificationEmail;
import com.example.forest.model.Role;
import com.example.forest.repository.mongodb.MongoRefreshTokenRepository;
import com.example.forest.repository.mongodb.MongoUserRepository;
import com.example.forest.repository.mongodb.MongoVerificationTokenRepository;
import com.example.forest.security.JwtService;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * AuthService.java
 *
 * Service responsible for handling authentication, registration, token management,
 * and user account lifecycle actions (activation, password reset, interest updates, etc.).
 * <p>
 * This service integrates with:
 * <ul>
 *   <li>{@link JwtService} — for generating and validating JWT tokens.</li>
 *   <li>{@link RefreshTokenService} — for managing refresh tokens.</li>
 *   <li>{@link MailService} — for sending account-related emails.</li>
 *   <li>Repositories — for persistence of user and verification data.</li>
 * </ul>
 */
@Service
@AllArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final MongoUserRepository userRepository;
    private final MongoVerificationTokenRepository verificationTokenRepository;
    private final MongoRefreshTokenRepository refreshTokenRepository;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /**
     * Periodically cleans up expired refresh tokens from the database.
     * <p>
     * This method runs every 24 hours to maintain database hygiene and prevent
     * token misuse.
     */
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        refreshTokenRepository.findAll().stream()
                .filter(token -> token.getExpiryDate().isBefore(now))
                .forEach(refreshTokenRepository::delete);
    }

    /**
     * Registers a new user by saving their details and sending a verification email.
     *
     * @param registerRequest the registration data submitted by the user.
     */
    @Transactional
    public void register(RegisterRequest registerRequest) {
        var user = new MongoUserDocument();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreated(Instant.now());
        user.setRole(Role.USER);
        user.setEnabled(false);
        user.setInterests(registerRequest.getInterests());

        userRepository.save(user);

        String token = generateVerificationToken(user);
        mailService.sendMail(new NotificationEmail(
                "Please activate your account",
                user.getEmail(),
                user.getUsername(),
                "Thank you for signing up with our platform. Please click the link below to activate your account: " +
                        "http://localhost:8080/api/v1/auth/accountVerification/" + token
        ));
    }

    /**
     * Generates a unique verification token for user account activation or password reset.
     *
     * @param user the user for whom to create the verification token.
     * @return the generated token string.
     */
    @Transactional
    public String generateVerificationToken(MongoUserDocument user) {
        String token = UUID.randomUUID().toString();
        MongoVerificationTokenDocument verificationToken = new MongoVerificationTokenDocument();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(Instant.now().plus(Duration.ofDays(1)));

        verificationTokenRepository.save(verificationToken);
        return token;
    }

    /**
     * Verifies a user’s account using a verification token.
     * <p>
     * If the token is valid, the corresponding user is enabled and the token is removed.
     *
     * @param token the token string to verify.
     */
    @Transactional
    public void verifyAccount(String token) {
        Optional<MongoVerificationTokenDocument> verificationTokenOptional =
                verificationTokenRepository.findByToken(token);

        verificationTokenOptional.orElseThrow(() -> new CustomException("Invalid Token"));
        enableUser(verificationTokenOptional.get());
        verificationTokenRepository.deleteByToken(token);
    }

    /**
     * Enables a user account after successful verification.
     *
     * @param verificationToken the verified token linked to the user.
     */
    @Transactional
    public void enableUser(MongoVerificationTokenDocument verificationToken) {
        @NotBlank(message = "Username is required")
        String username = verificationToken.getUser().getUsername();

        MongoUserDocument user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User with username: " + username + " not found!"));

        user.setEnabled(true);
        userRepository.save(user);
    }

    /**
     * Authenticates a user and generates a JWT + refresh token pair.
     *
     * @param loginRequest the credentials provided by the user.
     * @return a {@link ResponseEntity} containing {@link AuthenticationResponse}.
     */
    public ResponseEntity<AuthenticationResponse> login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String authToken = jwtService.generateToken(authentication);
            String refreshToken = refreshTokenService.generateRefreshToken(authentication.getName()).getToken();

            AuthenticationResponse response = AuthenticationResponse.builder()
                    .authenticationToken(authToken)
                    .refreshToken(refreshToken)
                    .expirationDate(Instant.now().plusMillis(jwtService.getJwtExpirationInMillis()))
                    .username(getCurrentUser().getUsername())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during login: {}", e.getMessage());
            AuthenticationResponse errorResponse = AuthenticationResponse.builder()
                    .username("Login failed. Please check your credentials and try again.")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * @param refreshTokenRequest contains the current refresh token and username.
     * @return a new {@link AuthenticationResponse} containing refreshed tokens.
     */
    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken());

        String newAuthToken = jwtService.generateTokenWithUserName(refreshTokenRequest.getUsername());
        String newRefreshToken = refreshTokenService.generateRefreshToken(refreshTokenRequest.getUsername()).getToken();

        refreshTokenService.deleteRefreshToken(refreshTokenRequest.getRefreshToken());

        return AuthenticationResponse.builder()
                .authenticationToken(newAuthToken)
                .refreshToken(newRefreshToken)
                .expirationDate(Instant.now().plusMillis(jwtService.getJwtExpirationInMillis()))
                .username(refreshTokenRequest.getUsername())
                .build();
    }

    /**
     * Checks if a user is currently authenticated in the security context.
     *
     * @return {@code true} if the user is logged in, otherwise {@code false}.
     */
    public boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     *
     * @return the {@link MongoUserDocument} representing the logged-in user, or {@code null} if none.
     */
    @Transactional(readOnly = true)
    public MongoUserDocument getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }

    /**
     * Sends a password reset email to the user with a unique verification token.
     *
     * @param email the email address of the user requesting password reset.
     */
    @Transactional
    public void forgotPassword(String email) {
        MongoUserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User with email " + email + " not found"));

        String token = generateVerificationToken(user);
        mailService.sendMail(new NotificationEmail(
                "Password Reset Request",
                user.getEmail(),
                user.getUsername(),
                "You have requested to reset your password. " +
                        "Please click the link below to reset it: " +
                        "http://localhost:4200/reset-password/" + token
        ));
    }

    /**
     * Resets a user’s password after verifying their reset token.
     *
     * @param token       the verification token used for resetting the password.
     * @param newPassword the new password to be set.
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        MongoVerificationTokenDocument verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new CustomException("Invalid Token"));

        MongoUserDocument user = verificationToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);
    }

    /**
     * Updates the current user’s interest topics.
     *
     * @param interests a set of interest strings selected by the user.
     */
    @Transactional
    public void saveInterests(Set<String> interests) {
        MongoUserDocument currentUser = getCurrentUser();
        currentUser.setInterests(interests);
        userRepository.save(currentUser);
    }
}

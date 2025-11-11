package com.example.forest.config;

import com.example.forest.document.MongoUserDocument;
import com.example.forest.repository.mongodb.MongoUserRepository;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.singletonList;

/**
 * 🌲 ApplicationConfig — Main Spring Security configuration class.
 * Loads JWT RSA keys from the `keys/` folder (paths defined in .env)
 * and configures authentication, password encoding, and JWT handling.
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    // ==========================
    // 🔐 JWT Key Paths from .env
    // ==========================
    @Value("${JWT_PUBLIC_KEY_PATH}")
    private String publicKeyPath;

    @Value("${JWT_PRIVATE_KEY_PATH}")
    private String privateKeyPath;

    private final MongoUserRepository userRepository;

    // ==========================
    // 👤 USER DETAILS SERVICE
    // ==========================

    @Bean
    public UserDetailsService userDetailsService() {
        return this::loadUserByUsername;
    }

    private UserDetails loadUserByUsername(String username) {
        Optional<MongoUserDocument> userOptional = userRepository.findByUsername(username);
        MongoUserDocument user = userOptional.orElseThrow(() ->
                new UsernameNotFoundException("User " + username + " not found."));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                getAuthorities(user.getRole().name())
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return singletonList(new SimpleGrantedAuthority(role));
    }

    // ==========================
    // 🔑 KEY LOADING UTILITIES
    // ==========================

    private RSAPublicKey loadPublicKey() throws Exception {
        String key = Files.readString(Paths.get(publicKeyPath))
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(spec);
    }

    private RSAPrivateKey loadPrivateKey() throws Exception {
        String key = Files.readString(Paths.get(privateKeyPath))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(spec);
    }

    private KeyPair loadKeyPair() throws Exception {
        return new KeyPair(loadPublicKey(), loadPrivateKey());
    }

    // ==========================
    // 🔒 JWT ENCODER / DECODER
    // ==========================

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        return NimbusJwtDecoder.withPublicKey(loadPublicKey()).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() throws Exception {
        KeyPair keyPair = loadKeyPair();
        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(jwkSource);
    }

    // ==========================
    // 🧩 AUTHENTICATION CONFIG
    // ==========================

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ==========================
    // ✅ VALIDATOR BEAN
    // ==========================

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
}

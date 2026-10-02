package com.fivesense.api.auth.infra;

import com.fivesense.api.users.domain.UserStatus;
import com.fivesense.api.users.infra.UserRepository;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.time.Instant;
import java.util.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http, Converter<Jwt, AbstractAuthenticationToken> converter) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a.requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/password-reset/**", "/api/v1/bootstrap/admin", "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**").permitAll().anyRequest().authenticated())
                .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(converter)))
                .build();
    }

    @Bean
    Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            boolean passwordChange = Boolean.TRUE.equals(jwt.getClaim("pwd_change"));
            String role = jwt.getClaimAsString("role");
            var authority = new SimpleGrantedAuthority(passwordChange ? "ROLE_PASSWORD_CHANGE" : "ROLE_" + role);
            return new JwtAuthenticationToken(jwt, List.of(authority), jwt.getSubject());
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    KeyPair jwtKeyPair(@Value("${security.jwt.private-key:}") String privatePem,
                       @Value("${security.jwt.public-key:}") String publicPem,
                       @Value("${spring.profiles.active:local}") String profile) {
        try {
            if (privatePem.isBlank() && publicPem.isBlank()) {
                if (profile.contains("prod")) throw new IllegalStateException("JWT RSA key pair is required in production");
                var generator = KeyPairGenerator.getInstance("RSA"); generator.initialize(2048); return generator.generateKeyPair();
            }
            var factory = KeyFactory.getInstance("RSA");
            var privateBytes = Base64.getDecoder().decode(cleanPem(privatePem));
            var publicBytes = Base64.getDecoder().decode(cleanPem(publicPem));
            PrivateKey privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
            PublicKey publicKey = factory.generatePublic(new X509EncodedKeySpec(publicBytes));
            return new KeyPair(publicKey, privateKey);
        } catch (GeneralSecurityException ex) { throw new IllegalStateException("Invalid JWT RSA key configuration", ex); }
    }

    @Bean
    JwtEncoder jwtEncoder(KeyPair keyPair) {
        com.nimbusds.jose.jwk.RSAKey jwk = new com.nimbusds.jose.jwk.RSAKey.Builder((RSAPublicKey) keyPair.getPublic()).privateKey((RSAPrivateKey) keyPair.getPrivate()).keyID("fivesense-rs256").build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
    }

    @Bean
    JwtDecoder jwtDecoder(KeyPair keyPair, AuthSessionRepository sessions, UserRepository users) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();
        OAuth2TokenValidator<Jwt> timestamps = JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> session = jwt -> {
            try {
                UUID sessionId = UUID.fromString(jwt.getClaimAsString("sid"));
                UUID userId = UUID.fromString(jwt.getSubject());
                var found = sessions.findById(sessionId);
                var user = users.findById(userId);
                Instant now = Instant.now();
                boolean valid = found.isPresent() && user.isPresent() && found.get().getUserId().equals(userId)
                        && found.get().getRevokedAt() == null && found.get().getExpiresAt().isAfter(now)
                        && !found.get().getLastUsedAt().plusSeconds(8 * 60 * 60).isBefore(now)
                        && user.get().getAuthVersion() == ((Number) jwt.getClaim("auth_version")).intValue()
                        && (user.get().getStatus() == UserStatus.ACTIVE || Boolean.TRUE.equals(jwt.getClaim("pwd_change")) && user.get().getStatus() == UserStatus.FIRST_ACCESS);
                return valid ? OAuth2TokenValidatorResult.success() : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Session is expired or revoked", null));
            } catch (RuntimeException ex) { return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid session claim", null)); }
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(timestamps, session));
        return decoder;
    }

    private static String cleanPem(String pem) { return pem.replace("\\n", "").replaceAll("-----BEGIN [^-]+-----", "").replaceAll("-----END [^-]+-----", "").replaceAll("\\s", ""); }
}

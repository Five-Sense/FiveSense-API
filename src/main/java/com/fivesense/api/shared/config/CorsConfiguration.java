package com.fivesense.api.shared.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 * Libera o CORS da API para o cliente de desenvolvimento (React Native/Expo web em localhost:8081).
 * Origens adicionais, como o IP da rede local, entram por {@code CORS_ALLOWED_ORIGINS} separadas por virgula.
 */
@Configuration
public class CorsConfiguration implements WebMvcConfigurer {
    private final String[] allowedOrigins;
    public CorsConfiguration(@Value("${app.cors.allowed-origins}") String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}

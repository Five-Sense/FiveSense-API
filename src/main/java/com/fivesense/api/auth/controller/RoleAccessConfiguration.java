package com.fivesense.api.auth.controller;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class RoleAccessConfiguration implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RoleAccessInterceptor()).addPathPatterns("/api/v1/**").excludePathPatterns("/api/v1/auth/**");
    }
}

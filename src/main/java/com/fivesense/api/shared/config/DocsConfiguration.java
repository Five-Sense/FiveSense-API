package com.fivesense.api.shared.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/** Abre a documentacao estatica (docs/site/index.html, empacotada em static/docs) em /docs. */
@Configuration
public class DocsConfiguration implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/docs", "/docs/index.html");
        registry.addRedirectViewController("/docs/", "/docs/index.html");
        registry.addRedirectViewController("/api/docs", "/docs/index.html");
    }
}

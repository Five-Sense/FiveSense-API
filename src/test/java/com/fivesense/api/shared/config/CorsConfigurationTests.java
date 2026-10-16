package com.fivesense.api.shared.config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = CorsConfigurationTests.TestConfig.class)
class CorsConfigurationTests {
    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context).build();
    }
    @Test
    void preflightFromAllowedOriginIsAcceptedWithCustomRoleHeader() throws Exception {
        mockMvc().perform(options("/api/v1/ping")
                        .header("Origin", "http://localhost:8081")
                        .header("Access-Control-Request-Method", "PATCH")
                        .header("Access-Control-Request-Headers", "x-user-role,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"));
    }
    @Test
    void simpleRequestFromAllowedOriginReceivesCorsHeader() throws Exception {
        mockMvc().perform(get("/api/v1/ping").header("Origin", "http://localhost:8081"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"));
    }
    @Test
    void requestFromOtherOriginIsRejected() throws Exception {
        mockMvc().perform(get("/api/v1/ping").header("Origin", "http://evil.example"))
                .andExpect(status().isForbidden());
    }
    @Configuration
    @EnableWebMvc
    static class TestConfig {
        @Bean
        CorsConfiguration corsConfiguration() {
            return new CorsConfiguration(new String[] {"http://localhost:8081"});
        }
        @Bean
        PingEndpoint pingEndpoint() {
            return new PingEndpoint();
        }
    }
    @RestController
    static class PingEndpoint {
        @GetMapping("/api/v1/ping")
        String ping() { return "pong"; }
    }
}

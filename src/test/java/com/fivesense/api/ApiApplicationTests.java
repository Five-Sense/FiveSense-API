package com.fivesense.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.springframework.mail.javamail.JavaMailSender;
import java.util.*;
import java.util.regex.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
class ApiApplicationTests {
    private static final String BOOTSTRAP_SECRET="integration-test-bootstrap-secret-32-chars";
    @Container static final PostgreSQLContainer DB=new PostgreSQLContainer("postgres:18-alpine");
    @DynamicPropertySource static void database(DynamicPropertyRegistry properties){
        properties.add("spring.datasource.url",DB::getJdbcUrl);
        properties.add("spring.datasource.username",DB::getUsername);
        properties.add("spring.datasource.password",DB::getPassword);
        properties.add("security.bootstrap.secret",()->BOOTSTRAP_SECRET);
        properties.add("security.bootstrap.initial-admin.email",()->"admin@example.com");
        properties.add("security.bootstrap.initial-admin.name",()->"Admin");
        properties.add("spring.mail.host",()->"localhost");
        properties.add("spring.mail.port",()->"25");
    }
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean JavaMailSender mailSender;

    @Test
    void apiSupportsForcedPasswordChangeRoleBoundariesAndOccurrenceWithoutStockMovement() throws Exception {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        String bootstrap=mvc.perform(post("/api/v1/bootstrap/admin").header("X-Bootstrap-Secret",BOOTSTRAP_SECRET))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String adminTemporary=json.readTree(bootstrap).get("initialPassword").asText();
        mvc.perform(post("/api/v1/bootstrap/admin").header("X-Bootstrap-Secret",BOOTSTRAP_SECRET)).andExpect(status().isConflict());

        String adminChallenge=login("admin@example.com",adminTemporary,true);
        String admin=changePassword(adminChallenge,"AdminStrong#1");
        mvc.perform(post("/api/v1/users").header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"second-admin@example.com\",\"name\":\"Second Admin\",\"role\":\"ADMIN\"}")).andExpect(status().isForbidden());
        createUser(admin,"manager@example.com","Manager","MANAGER");
        String managerInitial=sentPassword("manager@example.com");
        String managerChallenge=login("manager@example.com",managerInitial,true);
        String manager=changePassword(managerChallenge,"ManagerStrong#1");

        createUser(manager,"viewer@example.com","Totem","VIEWER");
        String viewerInitial=sentPassword("viewer@example.com");
        String viewerChallenge=login("viewer@example.com",viewerInitial,true);
        String viewer=changePassword(viewerChallenge,"ViewerStrong#1");

        String teamId=postJson(manager,"/api/v1/teams","{\"name\":\"Equipe A\",\"representatives\":\"Ana e Bia\",\"schedule\":\"Turno manhã\"}").get("id").asText();
        String problemId=postJson(manager,"/api/v1/problems","{\"name\":\"Limpeza\",\"relatedEmail\":\"quality@example.com\",\"defaultResponse\":\"Ocorrência recebida\",\"active\":true}").get("id").asText();
        String materialId=postJson(manager,"/api/v1/materials","{\"name\":\"Pano\",\"stockQuantity\":8,\"minimumStock\":2,\"active\":true}").get("id").asText();

        mvc.perform(get("/api/v1/teams").header("Authorization","Bearer "+viewer)).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].representatives").value("Ana e Bia"));
        mvc.perform(get("/api/v1/problems").header("Authorization","Bearer "+viewer)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/problems/options").header("Authorization","Bearer "+viewer)).andExpect(status().isOk());
        mvc.perform(patch("/api/v1/teams/"+teamId+"/status").header("Authorization","Bearer "+viewer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"DOING_5S\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DOING_5S"));
        mvc.perform(post("/api/v1/occurrences").header("Authorization","Bearer "+viewer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"problemId\":\""+problemId+"\",\"materialId\":\""+materialId+"\",\"affectedQuantity\":4}")).andExpect(status().isCreated());
        mvc.perform(get("/api/v1/materials/stock").header("Authorization","Bearer "+viewer)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stockQuantity").value(8));
        mvc.perform(get("/api/v1/occurrences").header("Authorization","Bearer "+manager)).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        String refreshToken=json.readTree(mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"viewer@example.com\",\"password\":\"ViewerStrong#1\"}")).andReturn().getResponse().getContentAsString()).get("refreshToken").asText();
        String rotated=json.readTree(mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("refreshToken",refreshToken)))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("refreshToken").asText();
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("refreshToken",refreshToken)))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("refreshToken",rotated)))).andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("select stock_quantity from material where id = ?",Integer.class,UUID.fromString(materialId))).isEqualTo(8);
        verify(mailSender,atLeast(4)).send(any(SimpleMailMessage.class));
    }

    private String login(String email,String password,boolean mustChange) throws Exception {
        String response=mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email",email,"password",password)))).andExpect(status().isOk())
                .andExpect(jsonPath("$.passwordChangeRequired").value(mustChange)).andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("accessToken").asText();
    }
    private String changePassword(String challenge,String password) throws Exception {
        String response=mvc.perform(post("/api/v1/auth/change-password").header("Authorization","Bearer "+challenge).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("newPassword",password,"confirmation",password)))).andExpect(status().isOk())
                .andExpect(jsonPath("$.passwordChangeRequired").value(false)).andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("accessToken").asText();
    }
    private void createUser(String token,String email,String name,String role) throws Exception {
        mvc.perform(post("/api/v1/users").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email",email,"name",name,"role",role)))).andExpect(status().isOk());
    }
    private String sentPassword(String address) {
        var captor=org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender,atLeastOnce()).send(captor.capture());
        return captor.getAllValues().stream().filter(message->Arrays.asList(message.getTo()).contains(address)).map(SimpleMailMessage::getText)
                .map(text->Pattern.compile("senha temporária é: ([^\\r\\n]+)").matcher(text)).filter(Matcher::find).map(matcher->matcher.group(1)).reduce((first,last)->last).orElseThrow();
    }
    private JsonNode postJson(String token,String path,String body) throws Exception {
        String response=mvc.perform(post(path).header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();return json.readTree(response);
    }
}

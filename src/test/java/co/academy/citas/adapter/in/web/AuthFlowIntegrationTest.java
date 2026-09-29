package co.academy.citas.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.academy.citas.application.port.out.JwtTokenPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthFlowIntegrationTest {
    private static final String PASSWORD = "synthetic-password";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired JwtTokenPort jwtTokenPort;
    @Autowired EntityManager entityManager;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:citas;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("JWT_SECRET", () -> "a".repeat(32));
        registry.add("CORS_ALLOWED_ORIGINS", () -> "http://ui.example.test");
    }

    @Test
    void registersUserWithOnlyUserRoleAndNeverReturnsOrPersistsRawPassword() throws Exception {
        MvcResult result = register("ana@example.test", "100");

        result.getResponse().getContentAsString();
        assertThat(result.getResponse().getContentAsString()).doesNotContain(PASSWORD);
        assertThat(jdbcTemplate.queryForObject("select password_hash from user_account where email = ?", String.class,
                "ana@example.test")).startsWith("$2").isNotEqualTo(PASSWORD);
        assertThat(jdbcTemplate.queryForObject("select count(*) from user_account_role", Integer.class)).isEqualTo(1);
    }

    @Test
    void rejectsDuplicateEmailAndDocumentWithoutCreatingAnotherAccount() throws Exception {
        register("ana@example.test", "100");
        mockMvc.perform(registerRequest("ana@example.test", "200"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("email_already_registered"));
        mockMvc.perform(registerRequest("other@example.test", "100"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("document_already_registered"));
        assertThat(jdbcTemplate.queryForObject("select count(*) from user_account", Integer.class)).isEqualTo(1);
    }

    @Test
    void loginEmitsSeparateTokensAndRejectsInvalidCredentials() throws Exception {
        register("ana@example.test", "100");
        JsonNode session = login("ana@example.test", PASSWORD);

        assertThat(session.path("accessToken").asText()).isNotBlank().isNotEqualTo(session.path("refreshToken").asText());
        assertThat(session.path("accessTokenExpiresInSeconds").asLong()).isBetween(899L, 900L);
        assertThat(session.path("refreshTokenExpiresInSeconds").asLong()).isBetween(604799L, 604800L);
        assertThat(jwtTokenPort.parseAccess(session.path("accessToken").asText()).roles()).hasSize(1);
        mockMvc.perform(loginRequest("ana@example.test", "incorrect"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("invalid_credentials"));
    }

    @Test
    void rotatesRefreshRejectsInvalidExpiredRevokedAndReusedTokensAndKeepsOtherSessionActive() throws Exception {
        register("ana@example.test", "100");
        JsonNode firstSession = login("ana@example.test", PASSWORD);
        JsonNode otherSession = login("ana@example.test", PASSWORD);
        String firstRefresh = firstSession.path("refreshToken").asText();
        JsonNode rotated = refresh(firstRefresh);
        String rotatedRefresh = rotated.path("refreshToken").asText();

        assertThat(rotatedRefresh).isNotEqualTo(firstRefresh);
        mockMvc.perform(refreshRequest(firstRefresh)).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("invalid_refresh_token"));
        mockMvc.perform(refreshRequest("not-a-jwt")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("invalid_refresh_token"));
        mockMvc.perform(refreshRequest(rotatedRefresh)).andExpect(status().isOk());
        mockMvc.perform(refreshRequest(otherSession.path("refreshToken").asText())).andExpect(status().isOk());
    }

    @Test
    void logoutRevokesOnlyPresentedSessionAndExpiredSessionIsRejected() throws Exception {
        register("ana@example.test", "100");
        JsonNode firstSession = login("ana@example.test", PASSWORD);
        JsonNode otherSession = login("ana@example.test", PASSWORD);
        String firstRefresh = firstSession.path("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + firstRefresh + "\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(refreshRequest(firstRefresh)).andExpect(status().isUnauthorized());
        mockMvc.perform(refreshRequest(otherSession.path("refreshToken").asText())).andExpect(status().isOk());

        JsonNode expiring = login("ana@example.test", PASSWORD);
        jdbcTemplate.update("update auth_session set expires_at = ?", Timestamp.from(Instant.now().minusSeconds(1)));
        entityManager.clear();
        mockMvc.perform(refreshRequest(expiring.path("refreshToken").asText())).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("invalid_refresh_token"));
    }

    @Test
    void exposesExplicitCorsAndRequiresAuthenticationByDefault() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", "http://ui.example.test")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://ui.example.test"));
        mockMvc.perform(get("/api/v1/future-protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("unauthorized"));
    }

    private MvcResult register(String email, String document) throws Exception {
        return mockMvc.perform(registerRequest(email, document))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andReturn();
    }

    private JsonNode login(String email, String password) throws Exception {
        return objectMapper.readTree(mockMvc.perform(loginRequest(email, password))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    private JsonNode refresh(String refreshToken) throws Exception {
        return objectMapper.readTree(mockMvc.perform(refreshRequest(refreshToken))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder registerRequest(String email, String document) {
        return post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"givenNames\":\"Ana\",\"familyNames\":\"Prueba\",\"documentType\":\"CC\",\"documentNumber\":\""
                        + document + "\",\"email\":\"" + email + "\",\"phone\":\"3000000000\",\"password\":\"" + PASSWORD + "\"}");
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder loginRequest(String email, String password) {
        return post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}");
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder refreshRequest(String refreshToken) {
        return post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}");
    }
}

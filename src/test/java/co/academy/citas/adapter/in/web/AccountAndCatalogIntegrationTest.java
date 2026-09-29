package co.academy.citas.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** HU-003 recuperación de contraseña y HU-005..HU-010 perfil, afiliación y catálogos configurables. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AccountAndCatalogIntegrationTest {
    private static final String INITIAL_CREDENTIAL = "synthetic-" + "credential";
    private static final String UPDATED_CREDENTIAL = "synthetic-" + "credential-2";

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;

    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:account-catalog;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.username", () -> "sa"); registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("JWT_SECRET", () -> "a".repeat(32)); registry.add("CORS_ALLOWED_ORIGINS", () -> "http://ui.example.test");
        registry.add("app.password-reset.expose-token", () -> "true");
    }

    // --- HU-003 ---

    @Test void passwordResetTokenIsSingleUseHashedAndReplacesPassword() throws Exception {
        register("reset@example.test", "900");
        mvc.perform(post("/api/v1/auth/password-reset-requests").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"nobody@example.test\"}"))
                .andExpect(status().isAccepted()).andExpect(jsonPath("$.debugToken").doesNotExist());
        String token = requestReset("reset@example.test");
        assertThat(jdbc.queryForObject("select count(*) from password_reset_token where token_hash = ?", Integer.class, token)).isZero();
        mvc.perform(post("/api/v1/auth/password-resets").contentType(MediaType.APPLICATION_JSON).content(resetBody(token))).andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/auth/password-resets").contentType(MediaType.APPLICATION_JSON).content(resetBody(token)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("invalid_password_reset_token"));
        mvc.perform(login("reset@example.test", INITIAL_CREDENTIAL)).andExpect(status().isUnauthorized());
        mvc.perform(login("reset@example.test", UPDATED_CREDENTIAL)).andExpect(status().isOk());
    }

    @Test void expiredPasswordResetTokenIsRejected() throws Exception {
        register("expired@example.test", "901");
        String token = requestReset("expired@example.test");
        jdbc.update("update password_reset_token set expires_at = timestampadd(MINUTE, -1, current_timestamp)");
        mvc.perform(post("/api/v1/auth/password-resets").contentType(MediaType.APPLICATION_JSON).content(resetBody(token)))
                .andExpect(status().isBadRequest());
        mvc.perform(login("expired@example.test", INITIAL_CREDENTIAL)).andExpect(status().isOk());
    }

    // --- HU-005 ---

    @Test void userReadsAndUpdatesOnlyOwnProfile() throws Exception {
        String id = register("profile@example.test", "902");
        RequestPostProcessor self = user(id).roles("USER");
        mvc.perform(get("/api/v1/auth/me").with(self)).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("profile@example.test")).andExpect(jsonPath("$.passwordHash").doesNotExist());
        mvc.perform(put("/api/v1/auth/me").with(self).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"givenNames\":\"Ana María\",\"familyNames\":\"Prueba\",\"email\":\"profile@example.test\",\"phone\":\"3110000000\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.givenNames").value("Ana María")).andExpect(jsonPath("$.phone").value("3110000000"));
        mvc.perform(put("/api/v1/auth/me").with(self).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"givenNames\":\"\",\"familyNames\":\"Prueba\",\"email\":\"no-es-correo\",\"phone\":\"311\"}"))
                .andExpect(status().isBadRequest());
    }

    // --- HU-007 ---

    @Test void fixedCatalogsArePreloadedAndReadOnly() throws Exception {
        RequestPostProcessor anyUser = user(UUID.randomUUID().toString()).roles("USER");
        mvc.perform(get("/api/v1/regimes").with(anyUser)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'CONTRIBUTIVO')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.code == 'SUBSIDIADO')]").isNotEmpty());
        mvc.perform(get("/api/v1/catalogs/locations").with(anyUser)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'HIC')]").isNotEmpty()).andExpect(jsonPath("$[?(@.code == 'ICV')]").isNotEmpty());
        mvc.perform(post("/api/v1/admin/regimes").with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"NUEVO\",\"name\":\"Nuevo\"}")).andExpect(status().isMethodNotAllowed());
        mvc.perform(patch("/api/v1/admin/regimes/CONTRIBUTIVO/active").with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":false}")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/admin/regimes/CONTRIBUTIVO").with(admin())).andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("select active from regime_catalog where code = 'CONTRIBUTIVO'", Boolean.class)).isTrue();
    }

    // --- HU-006/007/008/009 ---

    @Test void adminManagesEpsAndPlansAndUserAffiliationOnlyAcceptsActiveConsistentData() throws Exception {
        String userId = register("affiliation@example.test", "903");
        RequestPostProcessor self = user(userId).roles("USER");
        long eps = id(mvc.perform(post("/api/v1/admin/eps").with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"EPS-SINT\",\"name\":\"EPS Sintética\"}")).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        long otherEps = id(mvc.perform(post("/api/v1/admin/eps").with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"EPS-OTRA\",\"name\":\"EPS Otra\"}")).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        long plan = id(mvc.perform(post("/api/v1/admin/eps/{id}/plans", eps).with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"PLAN-A\",\"name\":\"Plan A\"}")).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        mvc.perform(post("/api/v1/admin/eps").with(self).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"EPS-X\",\"name\":\"EPS X\"}")).andExpect(status().isForbidden());
        String regime = json.readTree(mvc.perform(get("/api/v1/regimes").with(self)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).get(0).get("code").asText();

        mvc.perform(put("/api/v1/profile/affiliation").with(self).contentType(MediaType.APPLICATION_JSON)
                .content(affiliation(otherEps, plan, regime))).andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/profile/affiliation").with(self).contentType(MediaType.APPLICATION_JSON)
                .content(affiliation(eps, plan, regime))).andExpect(status().isOk()).andExpect(jsonPath("$.planName").value("Plan A"));
        mvc.perform(get("/api/v1/profile/affiliation").with(self)).andExpect(status().isOk()).andExpect(jsonPath("$.epsId").value(eps));

        mvc.perform(delete("/api/v1/admin/plans/{id}", plan).with(admin())).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/eps/{id}/plans", eps).with(self)).andExpect(jsonPath("$.length()").value(0));
        assertThat(jdbc.queryForObject("select count(*) from eps_plan where id = ?", Integer.class, plan)).isEqualTo(1);
        mvc.perform(put("/api/v1/profile/affiliation").with(self).contentType(MediaType.APPLICATION_JSON)
                .content(affiliation(eps, plan, regime))).andExpect(status().isBadRequest());

        mvc.perform(patch("/api/v1/admin/eps/{id}/active", eps).with(admin()).contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
        mvc.perform(get("/api/v1/eps").with(self)).andExpect(jsonPath("$[?(@.id == %d)]".formatted(eps)).isEmpty());
        mvc.perform(get("/api/v1/admin/eps").with(admin())).andExpect(jsonPath("$[?(@.id == %d)]".formatted(eps)).isNotEmpty());
    }

    @Test void updatingMissingCatalogItemReturnsNotFound() throws Exception {
        mvc.perform(put("/api/v1/admin/eps/{id}", 999999).with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"NOPE\",\"name\":\"No existe\"}")).andExpect(status().isNotFound());
        mvc.perform(put("/api/v1/admin/specialties/{id}", 999999).with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"NOPE\",\"name\":\"No existe\",\"durationMinutes\":30}")).andExpect(status().isNotFound());
    }

    // --- HU-010 ---

    @Test void adminManagesSpecialtiesWithDurationRuleAndLogicalDeactivation() throws Exception {
        mvc.perform(post("/api/v1/admin/specialties").with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"DERMA\",\"name\":\"Dermatología\",\"durationMinutes\":45}")).andExpect(status().isBadRequest());
        long specialty = id(mvc.perform(post("/api/v1/admin/specialties").with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"DERMA\",\"name\":\"Dermatología\",\"durationMinutes\":60}")).andExpect(status().isCreated())
                .andExpect(jsonPath("$.durationMinutes").value(60)).andReturn().getResponse().getContentAsString());
        mvc.perform(delete("/api/v1/admin/specialties/{id}", specialty).with(admin())).andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("select active from specialty_catalog where id = ?", Boolean.class, specialty)).isFalse();
        mvc.perform(get("/api/v1/specialties").with(user(UUID.randomUUID().toString()).roles("USER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id == %d)]".formatted(specialty)).isEmpty());
        mvc.perform(post("/api/v1/admin/specialties").with(user(UUID.randomUUID().toString()).roles("PROFESSIONAL")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"X\",\"name\":\"X\",\"durationMinutes\":30}")).andExpect(status().isForbidden());
    }

    // --- utilidades ---

    private String register(String email, String document) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"givenNames\":\"Ana\",\"familyNames\":\"Prueba\",\"documentType\":\"CC\",\"documentNumber\":\"" + document
                                + "\",\"email\":\"" + email + "\",\"phone\":\"3000000000\",\"password\":\"" + INITIAL_CREDENTIAL + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("id").asText();
    }
    private String requestReset(String email) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/password-reset-requests").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        String token = json.readTree(body).get("debugToken").asText();
        assertThat(token).isNotBlank();
        return token;
    }
    private static String resetBody(String token) { return "{\"token\":\"" + token + "\",\"newPassword\":\"" + UPDATED_CREDENTIAL + "\"}"; }
    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder login(String email, String password) {
        return post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}");
    }
    private static String affiliation(long eps, long plan, String regime) { return "{\"epsId\":%d,\"planId\":%d,\"regimeCode\":\"%s\"}".formatted(eps, plan, regime); }
    private long id(String body) throws Exception { return json.readTree(body).get("id").asLong(); }
    private static RequestPostProcessor admin() { return user(UUID.randomUUID().toString()).roles("ADMIN"); }
}

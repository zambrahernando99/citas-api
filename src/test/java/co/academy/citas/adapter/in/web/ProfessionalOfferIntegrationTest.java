package co.academy.citas.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProfessionalOfferIntegrationTest {
    private static final String TEMPORARY_PASSWORD = "synthetic-temporary-password";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:professional-offer;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("JWT_SECRET", () -> "a".repeat(32));
        registry.add("CORS_ALLOWED_ORIGINS", () -> "http://ui.example.test");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCreatesSyntheticProfessionalWithBcryptPasswordThatIsNeverReturned() throws Exception {
        String response = mockMvc.perform(post("/api/v1/admin/professionals")
                        .contentType(MediaType.APPLICATION_JSON).content(createPayload("PRO-100", "LIC-100")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.professionalCode").value("PRO-100"))
                .andExpect(jsonPath("$.licenseNumber").value("LIC-100"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn().getResponse().getContentAsString();

        assertThat(response).doesNotContain(TEMPORARY_PASSWORD);
        assertThat(jdbcTemplate.queryForObject("select password_hash from user_account where email = ?", String.class,
                "prof.pro-100@example.test")).startsWith("$2").isNotEqualTo(TEMPORARY_PASSWORD);
        assertThat(jdbcTemplate.queryForObject("""
                select count(*) from user_account_role ur join role_catalog r on r.id = ur.role_id
                where r.code = 'PROFESSIONAL'
                """, Integer.class)).isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsDuplicatedProfessionalCodeAndLicenseNumber() throws Exception {
        mockMvc.perform(post("/api/v1/admin/professionals").contentType(MediaType.APPLICATION_JSON)
                .content(createPayload("PRO-101", "LIC-101"))).andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/admin/professionals").contentType(MediaType.APPLICATION_JSON)
                .content(createPayload("PRO-101", "LIC-102", "SECOND-CODE"))).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("professional_code_already_registered"));
        mockMvc.perform(post("/api/v1/admin/professionals").contentType(MediaType.APPLICATION_JSON)
                .content(createPayload("PRO-102", "LIC-101"))).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("license_number_already_registered"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void replacesOfferWithActiveSpecialtiesOnePrimaryAndOneOrBothFixedLocations() throws Exception {
        UUID professionalId = create("PRO-103", "LIC-103");
        long firstSpecialtyId = id("select id from specialty_catalog where code = 'MEDICINA_GENERAL'");
        long secondSpecialtyId = id("select id from specialty_catalog where code = 'CARDIOLOGIA'");
        long firstLocationId = id("select id from clinic_location where code = 'HIC'");
        long secondLocationId = id("select id from clinic_location where code = 'ICV'");

        mockMvc.perform(put("/api/v1/admin/professionals/{id}/assignments", professionalId)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"specialtyIds":[%d,%d],"primarySpecialtyId":%d,"locationIds":[%d,%d]}
                                """.formatted(firstSpecialtyId, secondSpecialtyId, secondSpecialtyId, firstLocationId, secondLocationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialties.length()").value(2))
                .andExpect(jsonPath("$.specialties[0].primary").value(true))
                .andExpect(jsonPath("$.specialties[0].specialty.id").value(secondSpecialtyId))
                .andExpect(jsonPath("$.locations.length()").value(2));

        assertThat(jdbcTemplate.queryForObject("select count(*) from professional_specialty where is_primary = true",
                Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from professional_location", Integer.class)).isEqualTo(2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsInactiveOrUnassignedPrimarySpecialtiesAndInvalidLocations() throws Exception {
        UUID professionalId = create("PRO-104", "LIC-104");
        long inactiveSpecialtyId = id("select id from specialty_catalog where code = 'PEDIATRIA'");
        long locationId = id("select id from clinic_location where code = 'HIC'");
        jdbcTemplate.update("update specialty_catalog set active = false where id = ?", inactiveSpecialtyId);

        mockMvc.perform(put("/api/v1/admin/professionals/{id}/assignments", professionalId)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"specialtyIds":[%d],"primarySpecialtyId":%d,"locationIds":[%d]}
                                """.formatted(inactiveSpecialtyId, inactiveSpecialtyId, locationId)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("invalid_professional_offer"));
        mockMvc.perform(put("/api/v1/admin/professionals/{id}/assignments", professionalId)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"specialtyIds":[1],"primarySpecialtyId":999,"locationIds":[%d]}
                                """.formatted(locationId)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("invalid_professional_offer"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivationPreservesProfileAndAssignments() throws Exception {
        UUID professionalId = create("PRO-105", "LIC-105");
        long specialtyId = id("select id from specialty_catalog where code = 'MEDICINA_GENERAL'");
        long locationId = id("select id from clinic_location where code = 'HIC'");
        mockMvc.perform(put("/api/v1/admin/professionals/{id}/assignments", professionalId)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"specialtyIds":[%d],"primarySpecialtyId":%d,"locationIds":[%d]}
                        """.formatted(specialtyId, specialtyId, locationId))).andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/admin/professionals/{id}/active", professionalId)
                        .contentType(MediaType.APPLICATION_JSON).content("{" + "\"active\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.specialties.length()").value(1)).andExpect(jsonPath("$.locations.length()").value(1));
        assertThat(jdbcTemplate.queryForObject("select count(*) from professional_profile", Integer.class)).isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "USER")
    void rejectsNonAdminMutationsWhileAuthenticatedUsersCanReadCatalogs() throws Exception {
        mockMvc.perform(post("/api/v1/admin/professionals").contentType(MediaType.APPLICATION_JSON)
                .content(createPayload("PRO-106", "LIC-106"))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/specialties")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
        mockMvc.perform(get("/api/v1/catalogs/locations")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    private UUID create(String code, String license) throws Exception {
        JsonNode response = objectMapper.readTree(mockMvc.perform(post("/api/v1/admin/professionals")
                .contentType(MediaType.APPLICATION_JSON).content(createPayload(code, license))).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        return UUID.fromString(response.path("id").asText());
    }

    private long id(String sql) { return jdbcTemplate.queryForObject(sql, Long.class); }

    private String createPayload(String code, String license) {
        return createPayload(code, license, code);
    }

    private String createPayload(String code, String license, String identity) {
        return """
                {"givenNames":"Profesional","familyNames":"Sintético","documentType":"CC",
                 "documentNumber":"DOC-%s","email":"prof.%s@example.test","phone":"3000000000",
                 "temporaryPassword":"%s","professionalCode":"%s","licenseNumber":"%s"}
                """.formatted(identity, identity.toLowerCase(), TEMPORARY_PASSWORD, code, license);
    }
}

package co.academy.citas.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** S5 / WF-001: contrato de recordatorios consumido por n8n con API key de máquina (DEC-013..016). */
@SpringBootTest
@AutoConfigureMockMvc
class AutomationReminderIntegrationTest {
    static final String KEY = "synthetic-automation-key-for-tests-0123456789";
    static final String HEADER = "X-Automation-Key";
    static final String DUE = "/api/v1/automation/reminders/due";
    static final UUID PROFESSIONAL_USER = UUID.fromString("20000000-0000-0000-0000-000000000001");
    static final UUID PROFESSIONAL = UUID.fromString("20000000-0000-0000-0000-000000000002");
    static final UUID PATIENT = UUID.fromString("20000000-0000-0000-0000-000000000003");
    static final long GENERAL = 1, LOCATION = 1;

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:automation-reminders;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.username", () -> "sa"); registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("JWT_SECRET", () -> "a".repeat(32)); registry.add("CORS_ALLOWED_ORIGINS", () -> "http://ui.example.test");
        registry.add("AUTOMATION_API_KEY", () -> KEY);
    }

    @BeforeEach void setup() {
        for (String table : new String[]{"automation_event_outbox", "appointment_reminder_delivery", "appointment_status_history", "professional_slot", "appointment_reschedule",
                "availability_block", "appointment_record", "professional_specialty", "professional_location", "professional_profile",
                "user_account_role", "user_account"})
            jdbc.update("delete from " + table);
        account(PROFESSIONAL_USER, "Pro");
        account(PATIENT, "Paciente");
        jdbc.update("insert into professional_profile (id,user_id,professional_code,license_number,active) values (?,?,?,?,true)",
                bytes(PROFESSIONAL), bytes(PROFESSIONAL_USER), "PRO-S5", "LIC-PRO-S5");
    }

    // --- Autenticación y privilegio mínimo ---

    @Test void automationEndpointsRejectMissingOrWrongKey() throws Exception {
        mvc.perform(get(DUE)).andExpect(status().isUnauthorized());
        mvc.perform(get(DUE).header(HEADER, "wrong-key-wrong-key-wrong-key-wrong")).andExpect(status().isUnauthorized());
    }

    @Test void userJwtRolesCannotUseAutomationEndpoints() throws Exception {
        mvc.perform(get(DUE).with(user(UUID.randomUUID().toString()).roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(get(DUE).with(user(PATIENT.toString()).roles("USER"))).andExpect(status().isForbidden());
    }

    @Test void automationKeyDoesNotOpenAnyOtherRoute() throws Exception {
        mvc.perform(get("/api/v1/appointments/mine").header(HEADER, KEY)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/admin/inbox").header(HEADER, KEY)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/auth/me").header(HEADER, KEY)).andExpect(status().isUnauthorized());
    }

    // --- Selección de citas ---

    @Test void onlyApprovedAppointmentsInsideTheWindowAreDue() throws Exception {
        UUID due = appointment("APPROVED", hoursFromNow(3));
        appointment("REQUESTED", hoursFromNow(3));
        appointment("REJECTED", hoursFromNow(4));
        appointment("CANCELLED", hoursFromNow(5));
        appointment("COMPLETED", hoursFromNow(6));
        appointment("NO_SHOW", hoursFromNow(7));
        appointment("APPROVED", hoursFromNow(-2));
        appointment("APPROVED", hoursFromNow(30));
        UUID wide = appointment("APPROVED", hoursFromNow(47));

        mvc.perform(automation(get(DUE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.windowCode").value("H24"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].appointmentId").value(due.toString()))
                .andExpect(jsonPath("$.items[0].patientEmail").value(PATIENT + "@example.test"))
                .andExpect(jsonPath("$.items[0].patientFirstName").value("Paciente"))
                .andExpect(jsonPath("$.items[0].professionalName").value("Pro Synthetic"))
                .andExpect(jsonPath("$.items[0].locationName").isNotEmpty())
                .andExpect(jsonPath("$.items[0].specialtyName").isNotEmpty());
        mvc.perform(automation(get(DUE).param("windowHours", "48")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.windowCode").value("H48"))
                .andExpect(jsonPath("$.items[*].appointmentId").value(containsInAnyOrder(due.toString(), appointmentIdAt(30), wide.toString())));
    }

    @Test void responseExposesOnlyMinimalFields() throws Exception {
        appointment("APPROVED", hoursFromNow(2));
        String body = mvc.perform(automation(get(DUE))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(body).doesNotContain("document", "phone", "3000000000", "reason", "affiliation", "password");
    }

    @Test void windowOutsideAllowedRangeIsRejected() throws Exception {
        mvc.perform(automation(get(DUE).param("windowHours", "0"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid_reminder_request"));
        mvc.perform(automation(get(DUE).param("windowHours", "73"))).andExpect(status().isBadRequest());
    }

    // --- Idempotencia y reintentos ---

    @Test void sentDeliveryIsIdempotentAndRemovesTheAppointmentFromDue() throws Exception {
        UUID id = appointment("APPROVED", hoursFromNow(5));
        for (int i = 0; i < 2; i++)
            mvc.perform(automation(post(deliveries(id))).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"windowCode\":\"H24\",\"status\":\"SENT\",\"channel\":\"GMAIL\",\"providerMessageId\":\"msg-1\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SENT"))
                    .andExpect(jsonPath("$.attemptCount").value(1));
        assertThat(count("select count(*) from appointment_reminder_delivery")).isEqualTo(1);
        mvc.perform(automation(get(DUE))).andExpect(jsonPath("$.items.length()").value(0));
        // Un FAILED tardío no degrada un SENT ya registrado
        mvc.perform(automation(post(deliveries(id))).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"windowCode\":\"H24\",\"status\":\"FAILED\",\"errorCode\":\"late\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test void failedDeliveryIsRetriedUntilMaxAttempts() throws Exception {
        UUID id = appointment("APPROVED", hoursFromNow(5));
        for (int attempt = 1; attempt <= 3; attempt++) {
            mvc.perform(automation(get(DUE))).andExpect(jsonPath("$.items.length()").value(1));
            mvc.perform(automation(post(deliveries(id))).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"windowCode\":\"H24\",\"status\":\"FAILED\",\"errorCode\":\"gmail_error\"}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.attemptCount").value(attempt));
        }
        mvc.perform(automation(get(DUE))).andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test void deliveryForAppointmentNoLongerApprovedConflicts() throws Exception {
        UUID id = appointment("CANCELLED", hoursFromNow(5));
        mvc.perform(automation(post(deliveries(id))).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"windowCode\":\"H24\",\"status\":\"SENT\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("reminder_not_applicable"));
        mvc.perform(automation(post(deliveries(UUID.randomUUID()))).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"windowCode\":\"H24\",\"status\":\"SENT\"}"))
                .andExpect(status().isNotFound());
    }

    @Test void deliveryPayloadIsValidated() throws Exception {
        UUID id = appointment("APPROVED", hoursFromNow(5));
        mvc.perform(automation(post(deliveries(id))).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"windowCode\":\"H24\",\"status\":\"DELIVERED\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(automation(post(deliveries(id))).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"windowCode\":\"<script>\",\"status\":\"SENT\"}"))
                .andExpect(status().isBadRequest());
        assertThat(count("select count(*) from appointment_reminder_delivery")).isZero();
    }

    private static MockHttpServletRequestBuilder automation(MockHttpServletRequestBuilder request) { return request.header(HEADER, KEY); }
    private static String deliveries(UUID id) { return "/api/v1/automation/reminders/" + id + "/deliveries"; }
    private static LocalDateTime hoursFromNow(int hours) { return LocalDateTime.now(ZoneOffset.UTC).plusHours(hours).withSecond(0).withNano(0); }
    private String appointmentIdAt(int hours) {
        LocalDateTime start = hoursFromNow(hours);
        byte[] raw = jdbc.queryForObject("select id from appointment_record where scheduled_start_at = ?", byte[].class, start);
        ByteBuffer buffer = ByteBuffer.wrap(raw);
        return new UUID(buffer.getLong(), buffer.getLong()).toString();
    }
    private UUID appointment(String status, LocalDateTime start) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into appointment_record (id,patient_user_id,professional_id,location_id,specialty_id,status_code,scheduled_start_at,scheduled_end_at,reason) values (?,?,?,?,?,?,?,?,?)",
                bytes(id), bytes(PATIENT), bytes(PROFESSIONAL), LOCATION, GENERAL, status, start, start.plusMinutes(30), "Motivo sintético");
        return id;
    }
    private void account(UUID id, String givenNames) {
        jdbc.update("insert into user_account (id,given_names,family_names,document_type,document_number,email,phone,password_hash) values (?,?,?,?,?,?,?,?)",
                bytes(id), givenNames, "Synthetic", "CC", id.toString(), id + "@example.test", "3000000000", "hash");
    }
    private int count(String sql) { return jdbc.queryForObject(sql, Integer.class); }
    private static byte[] bytes(UUID v) { return ByteBuffer.allocate(16).putLong(v.getMostSignificantBits()).putLong(v.getLeastSignificantBits()).array(); }
}

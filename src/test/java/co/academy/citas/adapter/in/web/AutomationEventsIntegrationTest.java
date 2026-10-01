package co.academy.citas.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** S6 / WF-002 (eventos de cambio de estado en outbox) y WF-003 (citas del día sin datos personales). */
@SpringBootTest
@AutoConfigureMockMvc
class AutomationEventsIntegrationTest {
    static final String KEY = "synthetic-automation-key-for-tests-0123456789";
    static final UUID PROFESSIONAL_USER = UUID.fromString("30000000-0000-0000-0000-000000000001");
    static final UUID PROFESSIONAL = UUID.fromString("30000000-0000-0000-0000-000000000002");
    static final UUID PATIENT = UUID.fromString("30000000-0000-0000-0000-000000000003");
    static final UUID ADMIN = UUID.fromString("30000000-0000-0000-0000-000000000004");
    static final long GENERAL = 1, CARDIOLOGY = 2, LOCATION = 1;

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;

    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:automation-events;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.username", () -> "sa"); registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("JWT_SECRET", () -> "a".repeat(32)); registry.add("CORS_ALLOWED_ORIGINS", () -> "http://ui.example.test");
        registry.add("AUTOMATION_API_KEY", () -> KEY);
    }

    @BeforeEach void setup() {
        for (String table : new String[]{"automation_event_outbox", "appointment_reminder_delivery", "appointment_status_history", "professional_slot",
                "appointment_reschedule", "availability_block", "appointment_record", "professional_specialty", "professional_location",
                "professional_profile", "user_account_role", "user_account"})
            jdbc.update("delete from " + table);
        account(PROFESSIONAL_USER); account(PATIENT); account(ADMIN);
        jdbc.update("insert into professional_profile (id,user_id,professional_code,license_number,active) values (?,?,?,?,true)", bytes(PROFESSIONAL), bytes(PROFESSIONAL_USER), "PRO-S6", "LIC-PRO-S6");
        jdbc.update("insert into professional_location (professional_id,location_id) values (?,?)", bytes(PROFESSIONAL), LOCATION);
        jdbc.update("insert into professional_specialty (professional_id,specialty_id,is_primary) values (?,?,true),(?,?,false)", bytes(PROFESSIONAL), GENERAL, bytes(PROFESSIONAL), CARDIOLOGY);
    }

    // --- WF-002: el cambio de estado y el evento se guardan en la misma transacción ---

    @Test void specializedDecisionsEnqueueOneEventEach() throws Exception {
        LocalDateTime start = at(3, 9, 0); block(start, 3);
        String approved = reserve(CARDIOLOGY, start);
        String rejected = reserve(CARDIOLOGY, start.plusHours(1));
        decide(approved, "{\"approve\":true}");
        decide(rejected, "{\"approve\":false,\"reason\":\"Sin cupo\"}");
        assertThat(events()).containsExactlyInAnyOrder(
                event("SPECIALIZED_DECISION", "APPROVED", approved),
                event("SPECIALIZED_DECISION", "REJECTED", rejected));
        assertThat(count("select count(*) from automation_event_outbox where delivery_status = 'PENDING' and attempt_count = 0")).isEqualTo(2);
    }

    @Test void cancellationEnqueuesEvent() throws Exception {
        LocalDateTime start = at(4, 9, 0); block(start, 1);
        String id = reserve(GENERAL, start);
        mvc.perform(delete("/api/v1/appointments/{id}", id).with(patient()).contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Viaje\"}"))
                .andExpect(status().isNoContent());
        assertThat(events()).containsExactly(event("CANCELLATION", "CANCELLED", id));
    }

    @Test void rescheduleDecisionsEnqueueEventsWithTheRequest() throws Exception {
        LocalDateTime start = at(5, 9, 0); block(start, 2);
        String id = reserve(GENERAL, start);
        String first = requestReschedule(id, start.plusMinutes(30));
        mvc.perform(post("/api/v1/admin/reschedules/{id}/decision", first).with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"approve\":false,\"reason\":\"Sin cupo\"}")).andExpect(status().isOk());
        String second = requestReschedule(id, start.plusMinutes(60));
        mvc.perform(post("/api/v1/admin/reschedules/{id}/decision", second).with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"approve\":true}")).andExpect(status().isOk());
        assertThat(events()).containsExactlyInAnyOrder(
                event("RESCHEDULE_DECISION", "REJECTED", id),
                event("RESCHEDULE_DECISION", "APPROVED", id));
        assertThat(count("select count(*) from automation_event_outbox where reschedule_id is not null")).isEqualTo(2);
    }

    @Test void generalBookingAndClosingDoNotEnqueueEvents() throws Exception {
        LocalDateTime start = at(6, 9, 0); block(start, 1);
        reserve(GENERAL, start);
        LocalDateTime past = LocalDateTime.now(ZoneOffset.UTC).minusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        UUID closed = rawAppointment("APPROVED", past);
        mvc.perform(patch("/api/v1/professional/appointments/{id}/completion", closed).with(professional()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\"}")).andExpect(status().isNoContent());
        assertThat(count("select count(*) from automation_event_outbox")).isZero();
    }

    @Test void failedDecisionDoesNotEnqueueEvent() throws Exception {
        LocalDateTime start = at(7, 9, 0); block(start, 1);
        String id = reserve(CARDIOLOGY, start);
        decide(id, "{\"approve\":true}");
        mvc.perform(post("/api/v1/admin/appointments/{id}/decision", id).with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"approve\":true}")).andExpect(status().isNotFound());
        assertThat(count("select count(*) from automation_event_outbox")).isEqualTo(1);
    }

    // --- WF-003: citas del día para el resumen operativo ---

    @Test void dailyAppointmentsRequireTheAutomationKey() throws Exception {
        mvc.perform(get("/api/v1/automation/appointments/daily")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/automation/appointments/daily").with(admin())).andExpect(status().isForbidden());
    }

    @Test void dailyAppointmentsUseTheBogotaDayAndExposeNoPersonalData() throws Exception {
        LocalDate day = LocalDate.of(2031, 3, 10);
        ZoneId bogota = ZoneId.of("America/Bogota");
        // 00:30 y 23:30 hora Bogotá caen dentro del día; 23:30 del día anterior no
        rawAppointment("APPROVED", day.atTime(0, 30).atZone(bogota).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
        rawAppointment("NO_SHOW", day.atTime(23, 30).atZone(bogota).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
        rawAppointment("CANCELLED", day.minusDays(1).atTime(23, 30).atZone(bogota).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());

        String body = mvc.perform(get("/api/v1/automation/appointments/daily").param("date", day.toString()).header("X-Automation-Key", KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2031-03-10"))
                .andExpect(jsonPath("$.timezone").value("America/Bogota"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].status").value("APPROVED"))
                .andExpect(jsonPath("$.items[0].locationName").isNotEmpty())
                .andExpect(jsonPath("$.items[0].specialtyName").isNotEmpty())
                .andExpect(jsonPath("$.items[1].status").value("NO_SHOW"))
                .andReturn().getResponse().getContentAsString();
        assertThat(body).doesNotContain("@example.test", "Synthetic", "patient", "professional", "reason", "3000000000");
    }

    @Test void dailyAppointmentsRejectInvalidDate() throws Exception {
        mvc.perform(get("/api/v1/automation/appointments/daily").param("date", "10-03-2031").header("X-Automation-Key", KEY))
                .andExpect(status().isBadRequest());
    }

    // --- helpers ---

    private List<Map<String, Object>> events() {
        return jdbc.query("select event_type, new_status, appointment_id from automation_event_outbox", (rs, row) -> Map.of(
                "type", rs.getString(1), "status", rs.getString(2), "appointment", uuid(rs.getBytes(3)).toString()));
    }
    private static Map<String, Object> event(String type, String status, String appointmentId) { return Map.of("type", type, "status", status, "appointment", appointmentId); }
    private void decide(String id, String body) throws Exception {
        mvc.perform(post("/api/v1/admin/appointments/{id}/decision", id).with(admin()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
    }
    private String reserve(long specialtyId, LocalDateTime start) throws Exception {
        String body = "{\"professionalId\":\"%s\",\"locationId\":1,\"specialtyId\":%d,\"startsAt\":\"%s\",\"reason\":\"Sintético\"}".formatted(PROFESSIONAL, specialtyId, start);
        String response = mvc.perform(post("/api/v1/appointments").with(patient()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("id").asText();
    }
    private String requestReschedule(String id, LocalDateTime start) throws Exception {
        String response = mvc.perform(post("/api/v1/appointments/{id}/reschedules", id).with(patient()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"startsAt\":\"%s\",\"reason\":\"Cambio\"}".formatted(start)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("id").asText();
    }
    private void block(LocalDateTime start, int hours) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into availability_block (id,professional_id,location_id,starts_at,ends_at) values (?,?,?,?,?)", bytes(id), bytes(PROFESSIONAL), LOCATION, start, start.plusHours(hours));
        for (int i = 0; i < hours * 2; i++)
            jdbc.update("insert into professional_slot (availability_block_id,professional_id,location_id,starts_at,ends_at) values (?,?,?,?,?)",
                    bytes(id), bytes(PROFESSIONAL), LOCATION, start.plusMinutes(30L * i), start.plusMinutes(30L * (i + 1)));
    }
    private UUID rawAppointment(String status, LocalDateTime start) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into appointment_record (id,patient_user_id,professional_id,location_id,specialty_id,status_code,scheduled_start_at,scheduled_end_at,reason) values (?,?,?,?,?,?,?,?,?)",
                bytes(id), bytes(PATIENT), bytes(PROFESSIONAL), LOCATION, GENERAL, status, start, start.plusMinutes(30), "Motivo sintético");
        return id;
    }
    private static LocalDateTime at(int days, int hour, int minute) { return LocalDateTime.now().plusDays(days).withHour(hour).withMinute(minute).withSecond(0).withNano(0); }
    private void account(UUID id) {
        jdbc.update("insert into user_account (id,given_names,family_names,document_type,document_number,email,phone,password_hash) values (?,?,?,?,?,?,?,?)",
                bytes(id), "Synthetic", "User", "CC", id.toString(), id + "@example.test", "3000000000", "hash");
    }
    private int count(String sql) { return jdbc.queryForObject(sql, Integer.class); }
    private static RequestPostProcessor patient() { return user(PATIENT.toString()).roles("USER"); }
    private static RequestPostProcessor professional() { return user(PROFESSIONAL_USER.toString()).roles("PROFESSIONAL"); }
    private static RequestPostProcessor admin() { return user(ADMIN.toString()).roles("ADMIN"); }
    private static byte[] bytes(UUID v) { return ByteBuffer.allocate(16).putLong(v.getMostSignificantBits()).putLong(v.getLeastSignificantBits()).array(); }
    private static UUID uuid(byte[] value) { ByteBuffer b = ByteBuffer.wrap(value); return new UUID(b.getLong(), b.getLong()); }
}

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
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
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

/** Ciclo de vida completo de citas (HU-017..HU-028) ejercido por la API con varios actores. */
@SpringBootTest
@AutoConfigureMockMvc
class AppointmentLifecycleIntegrationTest {
    static final UUID PROFESSIONAL_USER = UUID.fromString("10000000-0000-0000-0000-000000000001");
    static final UUID PROFESSIONAL = UUID.fromString("10000000-0000-0000-0000-000000000002");
    static final UUID PATIENT = UUID.fromString("10000000-0000-0000-0000-000000000003");
    static final UUID ADMIN = UUID.fromString("10000000-0000-0000-0000-000000000004");
    static final UUID OTHER_PATIENT = UUID.fromString("10000000-0000-0000-0000-000000000005");
    static final UUID OTHER_PROFESSIONAL_USER = UUID.fromString("10000000-0000-0000-0000-000000000006");
    static final UUID OTHER_PROFESSIONAL = UUID.fromString("10000000-0000-0000-0000-000000000007");
    static final long GENERAL = 1, CARDIOLOGY = 2, LOCATION = 1;

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;

    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:appointment-lifecycle;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.username", () -> "sa"); registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("JWT_SECRET", () -> "a".repeat(32)); registry.add("CORS_ALLOWED_ORIGINS", () -> "http://ui.example.test");
    }

    @BeforeEach void setup() {
        for (String table : new String[]{"automation_event_outbox", "appointment_status_history", "professional_slot", "appointment_reschedule", "availability_block",
                "appointment_record", "professional_specialty", "professional_location", "professional_profile", "user_account_role", "user_account"})
            jdbc.update("delete from " + table);
        for (UUID id : new UUID[]{PROFESSIONAL_USER, PATIENT, ADMIN, OTHER_PATIENT, OTHER_PROFESSIONAL_USER}) account(id);
        professional(PROFESSIONAL, PROFESSIONAL_USER, "PRO-LC-1");
        professional(OTHER_PROFESSIONAL, OTHER_PROFESSIONAL_USER, "PRO-LC-2");
    }

    // --- HU-017/018/019: reglas de slots 30/60 y doble reserva ---

    @Test void generalAppointmentIsApprovedAndSameSlotCannotBeBookedTwice() throws Exception {
        LocalDateTime start = at(2, 9, 0); block(PROFESSIONAL, start, 2);
        mvc.perform(post("/api/v1/appointments").with(patient()).contentType(MediaType.APPLICATION_JSON).content(reservation(GENERAL, start)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("APPROVED")).andExpect(jsonPath("$.durationMinutes").value(30));
        mvc.perform(post("/api/v1/appointments").with(otherPatient()).contentType(MediaType.APPLICATION_JSON).content(reservation(GENERAL, start)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("slot_unavailable"));
        assertThat(count("select count(*) from appointment_record")).isEqualTo(1);
    }

    @Test void sixtyMinuteSpecialtyNeedsTwoConsecutiveFreeSlots() throws Exception {
        LocalDateTime start = at(3, 8, 0); block(PROFESSIONAL, start, 1); block(PROFESSIONAL, start.plusMinutes(90), 1);
        // Slots 8:00, 8:30, 9:30 y 10:00: solo 8:00 y 9:30 tienen sucesor libre; 8:30 queda aislado por el hueco de 9:00
        mvc.perform(get("/api/v1/availability").with(patient()).param("locationId", "1").param("specialtyId", "2")
                        .param("professionalId", PROFESSIONAL.toString()).param("date", start.toLocalDate().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/v1/availability").with(patient()).param("locationId", "1").param("specialtyId", "1")
                        .param("professionalId", PROFESSIONAL.toString()).param("date", start.toLocalDate().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4));
        mvc.perform(post("/api/v1/appointments").with(patient()).contentType(MediaType.APPLICATION_JSON).content(reservation(CARDIOLOGY, start.plusMinutes(30))))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/v1/appointments").with(patient()).contentType(MediaType.APPLICATION_JSON).content(reservation(CARDIOLOGY, start)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("REQUESTED")).andExpect(jsonPath("$.durationMinutes").value(60));
        assertThat(count("select count(*) from professional_slot where appointment_id is not null")).isEqualTo(2);
    }

    @Test void slotsHeldByRequestedSpecializedAppointmentCannotBeBooked() throws Exception {
        LocalDateTime start = at(3, 10, 0); block(PROFESSIONAL, start, 2);
        reserve(patient(), CARDIOLOGY, start);
        mvc.perform(post("/api/v1/appointments").with(otherPatient()).contentType(MediaType.APPLICATION_JSON).content(reservation(GENERAL, start.plusMinutes(30))))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("slot_unavailable"));
    }

    @Test void pastSlotsCannotBeBooked() throws Exception {
        mvc.perform(post("/api/v1/appointments").with(patient()).contentType(MediaType.APPLICATION_JSON).content(reservation(GENERAL, at(-1, 9, 0))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("invalid_appointment"));
    }

    // --- HU-015: bloques comprometidos ---

    @Test void professionalCannotEditOrDeleteCommittedBlockOrOverlapBlocks() throws Exception {
        LocalDateTime start = at(4, 9, 0);
        mvc.perform(post("/api/v1/professional/availability-blocks").with(professional()).contentType(MediaType.APPLICATION_JSON)
                .content(blockBody(start, start.plusHours(1)))).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/professional/availability-blocks").with(professional()).contentType(MediaType.APPLICATION_JSON)
                .content(blockBody(start.plusMinutes(30), start.plusHours(2)))).andExpect(status().isBadRequest());
        String blockId = json.readTree(mvc.perform(get("/api/v1/professional/availability-blocks").with(professional()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get(0).get("id").asText();
        reserve(patient(), GENERAL, start);
        mvc.perform(delete("/api/v1/professional/availability-blocks/{id}", blockId).with(professional())).andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/professional/availability-blocks/{id}", blockId).with(professional()).contentType(MediaType.APPLICATION_JSON)
                .content(blockBody(start.plusHours(3), start.plusHours(4)))).andExpect(status().isBadRequest());
        mvc.perform(delete("/api/v1/professional/availability-blocks/{id}", blockId).with(otherProfessional())).andExpect(status().isBadRequest());
    }

    // --- HU-021: cancelación ---

    @Test void onlyOwnerCancelsFutureAppointmentAndSlotsAreReleasedWithAudit() throws Exception {
        LocalDateTime start = at(5, 9, 0); block(PROFESSIONAL, start, 1);
        String id = reserve(patient(), GENERAL, start);
        mvc.perform(delete("/api/v1/appointments/{id}", id).with(otherPatient())).andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/appointments/{id}", id).with(patient()).contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Viaje\"}"))
                .andExpect(status().isNoContent());
        assertThat(statusOf(id)).isEqualTo("CANCELLED");
        assertThat(count("select count(*) from professional_slot where appointment_id is not null")).isZero();
        assertThat(count("select count(*) from appointment_status_history where status_code = 'CANCELLED' and change_source = 'USER' and reason = 'Viaje'")).isEqualTo(1);
        mvc.perform(delete("/api/v1/appointments/{id}", id).with(patient())).andExpect(status().isNotFound());
    }

    @Test void pastAppointmentCannotBeCancelled() throws Exception {
        UUID id = pastAppointment("APPROVED");
        mvc.perform(delete("/api/v1/appointments/{id}", id).with(patient())).andExpect(status().isNotFound());
    }

    // --- HU-022/024: reprogramación ---

    @Test void rescheduleKeepsOriginalWhilePendingRejectReleasesNewAndApproveSwaps() throws Exception {
        LocalDateTime start = at(6, 9, 0); block(PROFESSIONAL, start, 2);
        String id = reserve(patient(), GENERAL, start);
        mvc.perform(post("/api/v1/appointments/{id}/reschedules", id).with(otherPatient()).contentType(MediaType.APPLICATION_JSON)
                .content(rescheduleBody(start.plusMinutes(30)))).andExpect(status().isNotFound());
        String first = requestReschedule(id, start.plusMinutes(30));
        assertThat(count("select count(*) from professional_slot where appointment_id is not null")).isEqualTo(1);
        assertThat(count("select count(*) from professional_slot where reschedule_request_id is not null")).isEqualTo(1);
        mvc.perform(post("/api/v1/appointments/{id}/reschedules", id).with(patient()).contentType(MediaType.APPLICATION_JSON)
                .content(rescheduleBody(start.plusMinutes(60)))).andExpect(status().isConflict());
        mvc.perform(post("/api/v1/admin/reschedules/{id}/decision", first).with(patient()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"approve\":true}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/reschedules/{id}/decision", first).with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"approve\":false}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/admin/reschedules/{id}/decision", first).with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"approve\":false,\"reason\":\"Sin cupo\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJECTED"));
        assertThat(count("select count(*) from professional_slot where reschedule_request_id is not null")).isZero();
        assertThat(scheduledStart(id)).isEqualTo(start);

        String second = requestReschedule(id, start.plusMinutes(60));
        mvc.perform(post("/api/v1/admin/reschedules/{id}/decision", second).with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"approve\":true}")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));
        assertThat(scheduledStart(id)).isEqualTo(start.plusMinutes(60));
        assertThat(statusOf(id)).isEqualTo("APPROVED");
        assertThat(jdbc.queryForObject("select starts_at from professional_slot where appointment_id = ?", LocalDateTime.class, bytes(UUID.fromString(id))))
                .isEqualTo(start.plusMinutes(60));
        assertThat(count("select count(*) from professional_slot where reschedule_request_id is not null")).isZero();
        assertThat(count("select count(*) from appointment_status_history where change_source = 'ADMIN' and reason like 'Reprogramación aprobada%'")).isEqualTo(1);
    }

    // --- HU-026: cierre de atención ---

    @Test void onlyAssignedProfessionalClosesFinishedAppointmentAsCompletedOrNoShow() throws Exception {
        UUID past = pastAppointment("APPROVED");
        mvc.perform(patch("/api/v1/professional/appointments/{id}/completion", past).with(professional()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"CANCELLED\"}")).andExpect(status().isBadRequest());
        mvc.perform(patch("/api/v1/professional/appointments/{id}/completion", past).with(otherProfessional()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\"}")).andExpect(status().isNotFound());
        mvc.perform(patch("/api/v1/professional/appointments/{id}/completion", past).with(patient()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\"}")).andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/professional/appointments/{id}/completion", past).with(professional()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"NO_SHOW\"}")).andExpect(status().isNoContent());
        assertThat(statusOf(past.toString())).isEqualTo("NO_SHOW");
        mvc.perform(patch("/api/v1/professional/appointments/{id}/completion", past).with(professional()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\"}")).andExpect(status().isNotFound());

        LocalDateTime future = at(7, 9, 0); block(PROFESSIONAL, future, 1);
        String upcoming = reserve(patient(), GENERAL, future);
        mvc.perform(patch("/api/v1/professional/appointments/{id}/completion", upcoming).with(professional()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\"}")).andExpect(status().isNotFound());
    }

    @Test void professionalAgendaOnlyListsOwnApprovedAppointments() throws Exception {
        LocalDateTime start = at(8, 9, 0); block(PROFESSIONAL, start, 2);
        reserve(patient(), GENERAL, start);
        reserve(otherPatient(), CARDIOLOGY, start.plusMinutes(30));
        mvc.perform(get("/api/v1/professional/appointments").with(professional())).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].status").value("APPROVED"));
        mvc.perform(get("/api/v1/professional/appointments").with(otherProfessional())).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- HU-004: autorización por rol ---

    @Test void rolesAreEnforcedOnProtectedRoutes() throws Exception {
        mvc.perform(get("/api/v1/appointments/mine")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/admin/appointments/requested").with(patient())).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/appointments/requested").with(professional())).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/inbox").with(patient())).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/professional/availability-blocks").with(patient()).contentType(MediaType.APPLICATION_JSON)
                .content(blockBody(at(2, 9, 0), at(2, 10, 0)))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/professional/appointments").with(admin())).andExpect(status().isForbidden());
    }

    @Test void myAppointmentsOnlyReturnsOwnAppointmentsWithReferenceIds() throws Exception {
        LocalDateTime start = at(9, 9, 0); block(PROFESSIONAL, start, 2);
        reserve(patient(), GENERAL, start);
        reserve(otherPatient(), GENERAL, start.plusMinutes(30));
        mvc.perform(get("/api/v1/appointments/mine").with(patient())).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].professionalId").value(PROFESSIONAL.toString()))
                .andExpect(jsonPath("$[0].specialtyId").value(GENERAL))
                .andExpect(jsonPath("$[0].locationId").value(LOCATION));
    }

    // --- HU-028: historial inmutable y legible solo por actores autorizados ---

    @Test void statusHistoryRecordsEveryTransitionAndIsReadableOnlyByAuthorizedActors() throws Exception {
        LocalDateTime start = at(10, 9, 0); block(PROFESSIONAL, start, 2);
        String id = reserve(patient(), CARDIOLOGY, start);
        mvc.perform(post("/api/v1/admin/appointments/{id}/decision", id).with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"approve\":true}")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/appointments/{id}/history", id).with(patient())).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("REQUESTED")).andExpect(jsonPath("$[0].source").value("USER"))
                .andExpect(jsonPath("$[0].changedAt").exists())
                .andExpect(jsonPath("$[1].status").value("APPROVED")).andExpect(jsonPath("$[1].source").value("ADMIN"))
                .andExpect(jsonPath("$[1].changedBy").doesNotExist());
        mvc.perform(get("/api/v1/appointments/{id}/history", id).with(admin())).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/v1/appointments/{id}/history", id).with(professional())).andExpect(status().isOk());
        mvc.perform(get("/api/v1/appointments/{id}/history", id).with(otherPatient())).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/appointments/{id}/history", id).with(otherProfessional())).andExpect(status().isNotFound());
        mvc.perform(put("/api/v1/appointments/{id}/history", id).with(admin()).contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(delete("/api/v1/appointments/{id}/history", id).with(admin())).andExpect(status().isMethodNotAllowed());
    }

    // --- HU-027: bandeja administrativa unificada ---

    @Test void adminInboxCombinesRequestedAndPendingReschedulesWithFiltersAndNoSideEffects() throws Exception {
        LocalDateTime start = at(11, 9, 0); block(PROFESSIONAL, start, 4);
        String requested = reserve(patient(), CARDIOLOGY, start);
        String general = reserve(otherPatient(), GENERAL, start.plusMinutes(60));
        requestReschedule(general, start.plusMinutes(90), otherPatient());
        mvc.perform(get("/api/v1/admin/inbox").with(admin())).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.type == 'APPOINTMENT')].appointmentId").value(requested))
                .andExpect(jsonPath("$[?(@.type == 'RESCHEDULE')].appointmentId").value(general));
        mvc.perform(get("/api/v1/admin/inbox").with(admin()).param("type", "RESCHEDULE")).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/v1/admin/inbox").with(admin()).param("specialtyId", "2")).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/v1/admin/inbox").with(admin()).param("professionalId", OTHER_PROFESSIONAL.toString())).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/v1/admin/inbox").with(admin()).param("locationId", "2")).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/v1/admin/inbox").with(admin()).param("from", start.toLocalDate().plusDays(1).toString())).andExpect(jsonPath("$.length()").value(0));
        assertThat(statusOf(requested)).isEqualTo("REQUESTED");
        assertThat(count("select count(*) from appointment_reschedule where status_code = 'PENDING'")).isEqualTo(1);
    }

    // --- utilidades ---

    private String reserve(RequestPostProcessor actor, long specialtyId, LocalDateTime start) throws Exception {
        String body = mvc.perform(post("/api/v1/appointments").with(actor).contentType(MediaType.APPLICATION_JSON).content(reservation(specialtyId, start)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("id").asText();
    }
    private String requestReschedule(String appointmentId, LocalDateTime start) throws Exception { return requestReschedule(appointmentId, start, patient()); }
    private String requestReschedule(String appointmentId, LocalDateTime start, RequestPostProcessor actor) throws Exception {
        String body = mvc.perform(post("/api/v1/appointments/{id}/reschedules", appointmentId).with(actor).contentType(MediaType.APPLICATION_JSON)
                .content(rescheduleBody(start))).andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("id").asText();
    }
    private UUID pastAppointment(String status) {
        UUID id = UUID.randomUUID(); LocalDateTime start = at(-1, 9, 0);
        jdbc.update("insert into appointment_record (id,patient_user_id,professional_id,location_id,specialty_id,status_code,scheduled_start_at,scheduled_end_at) values (?,?,?,?,?,?,?,?)",
                bytes(id), bytes(PATIENT), bytes(PROFESSIONAL), LOCATION, GENERAL, status, start, start.plusMinutes(30));
        return id;
    }
    private void block(UUID professional, LocalDateTime start, int hours) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into availability_block (id,professional_id,location_id,starts_at,ends_at) values (?,?,?,?,?)", bytes(id), bytes(professional), LOCATION, start, start.plusHours(hours));
        for (int i = 0; i < hours * 2; i++)
            jdbc.update("insert into professional_slot (availability_block_id,professional_id,location_id,starts_at,ends_at) values (?,?,?,?,?)",
                    bytes(id), bytes(professional), LOCATION, start.plusMinutes(30L * i), start.plusMinutes(30L * (i + 1)));
    }
    private String reservation(long specialtyId, LocalDateTime start) {
        return "{\"professionalId\":\"%s\",\"locationId\":1,\"specialtyId\":%d,\"startsAt\":\"%s\",\"reason\":\"Sintético\"}".formatted(PROFESSIONAL, specialtyId, start);
    }
    private static String blockBody(LocalDateTime start, LocalDateTime end) { return "{\"locationId\":1,\"startsAt\":\"%s\",\"endsAt\":\"%s\"}".formatted(start, end); }
    private static String rescheduleBody(LocalDateTime start) { return "{\"startsAt\":\"%s\",\"reason\":\"Cambio de agenda\"}".formatted(start); }
    private static LocalDateTime at(int days, int hour, int minute) { return LocalDateTime.now().plusDays(days).withHour(hour).withMinute(minute).withSecond(0).withNano(0); }
    private String statusOf(String id) { return jdbc.queryForObject("select status_code from appointment_record where id = ?", String.class, bytes(UUID.fromString(id))); }
    private LocalDateTime scheduledStart(String id) { return jdbc.queryForObject("select scheduled_start_at from appointment_record where id = ?", LocalDateTime.class, bytes(UUID.fromString(id))); }
    private int count(String sql) { return jdbc.queryForObject(sql, Integer.class); }
    private void account(UUID id) {
        jdbc.update("insert into user_account (id,given_names,family_names,document_type,document_number,email,phone,password_hash) values (?,?,?,?,?,?,?,?)",
                bytes(id), "Synthetic", "User", "CC", id.toString(), id + "@example.test", "3000000000", "hash");
    }
    private void professional(UUID id, UUID userId, String code) {
        jdbc.update("insert into professional_profile (id,user_id,professional_code,license_number,active) values (?,?,?,?,true)", bytes(id), bytes(userId), code, "LIC-" + code);
        jdbc.update("insert into professional_location (professional_id,location_id) values (?,?)", bytes(id), LOCATION);
        jdbc.update("insert into professional_specialty (professional_id,specialty_id,is_primary) values (?,?,true),(?,?,false)", bytes(id), GENERAL, bytes(id), CARDIOLOGY);
    }
    private static RequestPostProcessor patient() { return user(PATIENT.toString()).roles("USER"); }
    private static RequestPostProcessor otherPatient() { return user(OTHER_PATIENT.toString()).roles("USER"); }
    private static RequestPostProcessor professional() { return user(PROFESSIONAL_USER.toString()).roles("PROFESSIONAL"); }
    private static RequestPostProcessor otherProfessional() { return user(OTHER_PROFESSIONAL_USER.toString()).roles("PROFESSIONAL"); }
    private static RequestPostProcessor admin() { return user(ADMIN.toString()).roles("ADMIN"); }
    private static byte[] bytes(UUID v) { return ByteBuffer.allocate(16).putLong(v.getMostSignificantBits()).putLong(v.getLeastSignificantBits()).array(); }
}

package co.academy.citas.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AppointmentFlowIntegrationTest {
    static final UUID PROFESSIONAL_USER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    static final UUID PROFESSIONAL = UUID.fromString("00000000-0000-0000-0000-000000000002");
    static final UUID PATIENT = UUID.fromString("00000000-0000-0000-0000-000000000003");
    static final UUID ADMIN = UUID.fromString("00000000-0000-0000-0000-000000000004");
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:appointment-flow;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.username", () -> "sa"); registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("JWT_SECRET", () -> "a".repeat(32)); registry.add("CORS_ALLOWED_ORIGINS", () -> "http://ui.example.test");
    }
    @BeforeEach void setup() {
        jdbc.update("delete from appointment_status_history"); jdbc.update("delete from professional_slot"); jdbc.update("delete from availability_block"); jdbc.update("delete from appointment_record");
        jdbc.update("delete from professional_specialty"); jdbc.update("delete from professional_location"); jdbc.update("delete from professional_profile"); jdbc.update("delete from user_account_role"); jdbc.update("delete from user_account");
        user(PROFESSIONAL_USER, "pro@example.test"); user(PATIENT, "user@example.test"); user(ADMIN, "admin@example.test");
        jdbc.update("insert into professional_profile (id,user_id,professional_code,license_number,active) values (?,?,?,?,true)", bytes(PROFESSIONAL),bytes(PROFESSIONAL_USER),"PRO-TEST","LIC-TEST");
        jdbc.update("insert into professional_location (professional_id,location_id) values (?,1)",bytes(PROFESSIONAL));
        jdbc.update("insert into professional_specialty (professional_id,specialty_id,is_primary) values (?,1,true),(?,2,false)",bytes(PROFESSIONAL),bytes(PROFESSIONAL));
    }
    @Test @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "PROFESSIONAL")
    void professionalCreatesBlocksAndGeneralAppointmentIsApprovedWithoutDoubleBooking() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0);
        mvc.perform(post("/api/v1/professional/availability-blocks").contentType(MediaType.APPLICATION_JSON).content("{\"professionalId\":\"%s\",\"locationId\":1,\"startsAt\":\"%s\",\"endsAt\":\"%s\"}".formatted(PROFESSIONAL,start,start.plusHours(2)))).andExpect(status().isCreated());
        mvc.perform(get("/api/v1/availability").param("locationId","1").param("specialtyId","1").param("professionalId",PROFESSIONAL.toString()).param("date",start.toLocalDate().toString())).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4));
        // The reservation is exercised in the next test with a USER principal.
    }
    @Test @WithMockUser(username = "00000000-0000-0000-0000-000000000003", roles = "USER")
    void specializedRequestRetainsTwoSlotsAndRejectingItFreesThem() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0).withSecond(0).withNano(0);
        block(start);
        String body = "{\"professionalId\":\"%s\",\"locationId\":1,\"specialtyId\":2,\"startsAt\":\"%s\",\"reason\":\"Sintético\"}".formatted(PROFESSIONAL,start);
        String id = mvc.perform(post("/api/v1/appointments").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("REQUESTED")).andReturn().getResponse().getContentAsString().replaceAll(".*\\\"id\\\":\\\"([^\\\"]+).*", "$1");
        mvc.perform(post("/api/v1/appointments").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("slot_unavailable"));
        jdbc.update("update appointment_record set status_code = 'REQUESTED' where id = ?", bytes(UUID.fromString(id)));
        assertThatRequestedSlots(id, 2);
    }
    @Test @WithMockUser(username = "00000000-0000-0000-0000-000000000004", roles = "ADMIN")
    void adminRejectsRequestedAppointmentOnlyWithReasonAndReleasesSlots() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(4).withHour(10).withMinute(0).withSecond(0).withNano(0);
        block(start); UUID appointment = UUID.randomUUID();
        jdbc.update("insert into appointment_record (id,patient_user_id,professional_id,location_id,specialty_id,status_code,scheduled_start_at,scheduled_end_at) values (?,?,?,?,?,'REQUESTED',?,?)",bytes(appointment),bytes(PATIENT),bytes(PROFESSIONAL),1,2,start,start.plusHours(1));
        jdbc.update("update professional_slot set appointment_id = ? where professional_id = ? and starts_at >= ? and ends_at <= ?",bytes(appointment),bytes(PROFESSIONAL),start,start.plusHours(1));
        mvc.perform(post("/api/v1/admin/appointments/{id}/decision", appointment).contentType(MediaType.APPLICATION_JSON).content("{\"approve\":false}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("invalid_appointment"));
        mvc.perform(post("/api/v1/admin/appointments/{id}/decision", appointment).contentType(MediaType.APPLICATION_JSON).content("{\"approve\":false,\"reason\":\"Información incompleta\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJECTED"));
        org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject("select count(*) from professional_slot where appointment_id = ?",Integer.class,bytes(appointment))).isZero();
    }
    private void block(LocalDateTime start) { UUID id=UUID.randomUUID(); jdbc.update("insert into availability_block (id,professional_id,location_id,starts_at,ends_at) values (?,?,?,?,?)",bytes(id),bytes(PROFESSIONAL),1,start,start.plusHours(2)); for (int i=0;i<4;i++) jdbc.update("insert into professional_slot (availability_block_id,professional_id,location_id,starts_at,ends_at) values (?,?,?,?,?)",bytes(id),bytes(PROFESSIONAL),1,start.plusMinutes(30L*i),start.plusMinutes(30L*(i+1))); }
    private void assertThatRequestedSlots(String id, int expected) { org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject("select count(*) from professional_slot where appointment_id = ?",Integer.class,bytes(UUID.fromString(id)))).isEqualTo(expected); }
    private void user(UUID id,String email) { jdbc.update("insert into user_account (id,given_names,family_names,document_type,document_number,email,phone,password_hash) values (?,?,?,?,?,?,?,?)",bytes(id),"Synthetic","User","CC",id.toString(),email,"3000000000","hash"); }
    private static byte[] bytes(UUID v) { return ByteBuffer.allocate(16).putLong(v.getMostSignificantBits()).putLong(v.getLeastSignificantBits()).array(); }
}

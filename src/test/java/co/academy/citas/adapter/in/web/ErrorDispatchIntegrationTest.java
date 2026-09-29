package co.academy.citas.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Servidor real (no MockMvc): los errores despachados a /error conservan su estado en vez de volverse 401. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ErrorDispatchIntegrationTest {
    private static final String CREDENTIAL = "synthetic-" + "credential";

    @LocalServerPort int port;
    @Autowired ObjectMapper json;
    private final HttpClient client = HttpClient.newHttpClient();

    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:error-dispatch;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.username", () -> "sa"); registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("JWT_SECRET", () -> "a".repeat(32)); registry.add("CORS_ALLOWED_ORIGINS", () -> "http://ui.example.test");
    }

    @Test void authenticatedUserGetsRealErrorStatusInsteadOf401() throws Exception {
        send("POST", "/api/v1/auth/register", null, "{\"givenNames\":\"Ana\",\"familyNames\":\"Prueba\",\"documentType\":\"CC\",\"documentNumber\":\"err-1\","
                + "\"email\":\"error-dispatch@example.test\",\"phone\":\"3000000000\",\"password\":\"" + CREDENTIAL + "\"}");
        JsonNode tokens = json.readTree(send("POST", "/api/v1/auth/login", null,
                "{\"email\":\"error-dispatch@example.test\",\"password\":\"" + CREDENTIAL + "\"}").body());
        String access = tokens.get("accessToken").asText();

        assertThat(send("PATCH", "/api/v1/regimes", access, "{}").statusCode()).isEqualTo(405);
        assertThat(send("GET", "/api/v1/does-not-exist", access, null).statusCode()).isEqualTo(404);
        assertThat(send("GET", "/api/v1/does-not-exist", null, null).statusCode()).isEqualTo(401);
    }

    private HttpResponse<String> send(String method, String path, String token, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json").header("Accept", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}

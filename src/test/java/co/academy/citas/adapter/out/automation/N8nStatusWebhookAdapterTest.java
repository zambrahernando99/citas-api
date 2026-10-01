package co.academy.citas.adapter.out.automation;

import static org.assertj.core.api.Assertions.assertThat;

import co.academy.citas.adapter.in.config.AutomationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Contrato HTTP real del envío WF-002 contra un servidor local. */
class N8nStatusWebhookAdapterTest {
    static final String SECRET = "synthetic-webhook-secret-0123456789abcdef";
    HttpServer server;
    final AtomicReference<String> secret = new AtomicReference<>(), eventId = new AtomicReference<>(), body = new AtomicReference<>();
    int responseStatus = 200;

    @BeforeEach void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/webhook/citas-status", exchange -> {
            secret.set(exchange.getRequestHeaders().getFirst(N8nStatusWebhookAdapter.SECRET_HEADER));
            eventId.set(exchange.getRequestHeaders().getFirst(N8nStatusWebhookAdapter.EVENT_HEADER));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(responseStatus, -1);
            exchange.close();
        });
        server.start();
    }

    @AfterEach void stop() { server.stop(0); }

    @Test void sendsSecretEventIdAndJsonBody() {
        var adapter = adapter("http://127.0.0.1:" + server.getAddress().getPort() + "/webhook/citas-status", SECRET);
        assertThat(adapter.enabled()).isTrue();
        assertThat(adapter.send("evt-1", Map.of("status", "APPROVED"))).isEqualTo(200);
        assertThat(secret.get()).isEqualTo(SECRET);
        assertThat(eventId.get()).isEqualTo("evt-1");
        assertThat(body.get()).isEqualTo("{\"status\":\"APPROVED\"}");
    }

    @Test void returnsTheWebhookStatusAndMinusOneWhenUnreachable() {
        responseStatus = 503;
        assertThat(adapter("http://127.0.0.1:" + server.getAddress().getPort() + "/webhook/citas-status", SECRET).send("evt-2", Map.of())).isEqualTo(503);
        server.stop(0);
        assertThat(adapter("http://127.0.0.1:" + server.getAddress().getPort() + "/webhook/citas-status", SECRET).send("evt-3", Map.of())).isEqualTo(-1);
    }

    @Test void isDisabledWithoutHttpsOrStrongSecret() {
        assertThat(adapter("", SECRET).enabled()).isFalse();
        assertThat(adapter("http://n8n.example.test/webhook", SECRET).enabled()).isFalse();
        assertThat(adapter("https://n8n.example.test/webhook", "short").enabled()).isFalse();
        assertThat(adapter("https://n8n.example.test/webhook", SECRET).enabled()).isTrue();
    }

    private static N8nStatusWebhookAdapter adapter(String url, String secret) {
        AutomationProperties properties = new AutomationProperties();
        properties.setStatusWebhookUrl(url); properties.setStatusWebhookSecret(secret);
        return new N8nStatusWebhookAdapter(properties, new ObjectMapper());
    }
}

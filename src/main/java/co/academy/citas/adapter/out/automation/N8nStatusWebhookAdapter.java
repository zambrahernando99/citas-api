package co.academy.citas.adapter.out.automation;

import co.academy.citas.adapter.in.config.AutomationProperties;
import co.academy.citas.application.port.out.StatusWebhookPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * POST del evento WF-002 al webhook de n8n con X-Citas-Webhook-Secret (credencial Header Auth en n8n) y
 * X-Citas-Event-Id para idempotencia. Solo HTTPS, salvo localhost para pruebas. Sin URL o secreto válido queda deshabilitado.
 */
@Component
public class N8nStatusWebhookAdapter implements StatusWebhookPort {
    public static final String SECRET_HEADER = "X-Citas-Webhook-Secret";
    public static final String EVENT_HEADER = "X-Citas-Event-Id";
    private static final Logger log = LoggerFactory.getLogger(N8nStatusWebhookAdapter.class);
    private static final int MIN_SECRET_LENGTH = 32;

    private final AutomationProperties properties;
    private final ObjectMapper json;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public N8nStatusWebhookAdapter(AutomationProperties properties, ObjectMapper json) { this.properties = properties; this.json = json; }

    @Override public boolean enabled() {
        String url = properties.getStatusWebhookUrl();
        boolean allowedScheme = url.startsWith("https://") || url.startsWith("http://localhost:") || url.startsWith("http://127.0.0.1:");
        return allowedScheme && properties.getStatusWebhookSecret().length() >= MIN_SECRET_LENGTH;
    }

    @Override public int send(String eventId, Map<String, Object> payload) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(properties.getStatusWebhookUrl()))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header(SECRET_HEADER, properties.getStatusWebhookSecret())
                    .header(EVENT_HEADER, eventId)
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload)))
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
        } catch (JsonProcessingException | IllegalArgumentException invalid) {
            log.warn("WF-002 event {} could not be serialized or addressed", eventId);
            return -1;
        } catch (IOException unavailable) {
            log.warn("WF-002 webhook unavailable for event {}: {}", eventId, unavailable.getClass().getSimpleName());
            return -1;
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return -1;
        }
    }
}

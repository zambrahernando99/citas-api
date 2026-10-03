package co.academy.citas.application.port.out;

import java.util.Map;

/** Envío del evento al webhook de n8n (WF-002). Devuelve el código HTTP; -1 si no hubo respuesta. */
public interface StatusWebhookPort {
    boolean enabled();
    int send(String eventId, Map<String, Object> payload);
}

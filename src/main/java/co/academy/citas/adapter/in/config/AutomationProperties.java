package co.academy.citas.adapter.in.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Integración con n8n (DEC-013, DEC-018). Claves y URLs llegan por entorno y nunca se registran en logs. */
@ConfigurationProperties(prefix = "app.automation")
public class AutomationProperties {
    private String apiKey = "";
    private int defaultWindowHours = 24;
    private int maxAttempts = 3;
    private String zone = "America/Bogota";
    private String statusWebhookUrl = "";
    private String statusWebhookSecret = "";
    private int eventMaxAttempts = 5;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey == null ? "" : apiKey.trim(); }
    public int getDefaultWindowHours() { return defaultWindowHours; }
    public void setDefaultWindowHours(int defaultWindowHours) { this.defaultWindowHours = defaultWindowHours; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
    public String getStatusWebhookUrl() { return statusWebhookUrl; }
    public void setStatusWebhookUrl(String statusWebhookUrl) { this.statusWebhookUrl = statusWebhookUrl == null ? "" : statusWebhookUrl.trim(); }
    public String getStatusWebhookSecret() { return statusWebhookSecret; }
    public void setStatusWebhookSecret(String statusWebhookSecret) { this.statusWebhookSecret = statusWebhookSecret == null ? "" : statusWebhookSecret.trim(); }
    public int getEventMaxAttempts() { return eventMaxAttempts; }
    public void setEventMaxAttempts(int eventMaxAttempts) { this.eventMaxAttempts = eventMaxAttempts; }
}

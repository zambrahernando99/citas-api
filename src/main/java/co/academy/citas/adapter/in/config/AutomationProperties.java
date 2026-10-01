package co.academy.citas.adapter.in.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Identidad de máquina para n8n (DEC-013). La clave llega por entorno y nunca se registra en logs. */
@ConfigurationProperties(prefix = "app.automation")
public class AutomationProperties {
    private String apiKey = "";
    private int defaultWindowHours = 24;
    private int maxAttempts = 3;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey == null ? "" : apiKey.trim(); }
    public int getDefaultWindowHours() { return defaultWindowHours; }
    public void setDefaultWindowHours(int defaultWindowHours) { this.defaultWindowHours = defaultWindowHours; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
}

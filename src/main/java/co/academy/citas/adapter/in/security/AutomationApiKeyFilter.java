package co.academy.citas.adapter.in.security;

import co.academy.citas.adapter.in.config.AutomationProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Autentica a n8n con X-Automation-Key solo en /api/v1/automation/** y le concede únicamente ROLE_AUTOMATION (DEC-013).
 * Sin clave configurada (o con menos de 32 caracteres) nadie se autentica: falla cerrado.
 */
@Component
public class AutomationApiKeyFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Automation-Key";
    static final String PATH_PREFIX = "/api/v1/automation/";
    private static final int MIN_KEY_LENGTH = 32;
    private final AutomationProperties properties;

    public AutomationApiKeyFilter(AutomationProperties properties) { this.properties = properties; }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(request.getContextPath() + PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String presented = request.getHeader(HEADER);
        String expected = properties.getApiKey();
        if (presented != null && expected.length() >= MIN_KEY_LENGTH && matches(presented, expected)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    "automation:n8n", null, List.of(new SimpleGrantedAuthority("ROLE_AUTOMATION"))));
        }
        filterChain.doFilter(request, response);
    }

    /** Compara resúmenes SHA-256 en tiempo constante para no filtrar longitud ni prefijos. */
    private static boolean matches(String presented, String expected) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] a = digest.digest(presented.getBytes(StandardCharsets.UTF_8));
            byte[] b = digest.digest(expected.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(a, b);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}

package co.academy.citas.adapter.in.security;

import co.academy.citas.application.exception.TokenValidationException;
import co.academy.citas.application.port.out.AccessTokenClaims;
import co.academy.citas.application.port.out.JwtTokenPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenPort jwtTokenPort;

    public JwtAuthenticationFilter(JwtTokenPort jwtTokenPort) {
        this.jwtTokenPort = jwtTokenPort;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                AccessTokenClaims claims = jwtTokenPort.parseAccess(authorization.substring(7));
                var authorities = claims.roles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .toList();
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(claims.userId().toString(), null, authorities));
            } catch (TokenValidationException ignored) {
                // The authorization layer returns the contract's generic 401 response for protected routes.
            }
        }
        filterChain.doFilter(request, response);
    }
}

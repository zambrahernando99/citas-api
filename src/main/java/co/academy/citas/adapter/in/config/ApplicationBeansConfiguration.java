package co.academy.citas.adapter.in.config;

import co.academy.citas.application.port.out.AuthSessionPort;
import co.academy.citas.application.port.out.JwtTokenPort;
import co.academy.citas.application.port.out.PasswordHashingPort;
import co.academy.citas.application.port.out.TokenFingerprintPort;
import co.academy.citas.application.port.out.UserAccountPort;
import co.academy.citas.application.service.AuthenticationService;
import co.academy.citas.application.service.RegistrationService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationBeansConfiguration {
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    RegistrationService registrationService(UserAccountPort userAccountPort, PasswordHashingPort passwordHashingPort) {
        return new RegistrationService(userAccountPort, passwordHashingPort);
    }

    @Bean
    AuthenticationService authenticationService(UserAccountPort userAccountPort,
                                                PasswordHashingPort passwordHashingPort,
                                                JwtTokenPort jwtTokenPort,
                                                TokenFingerprintPort tokenFingerprintPort,
                                                AuthSessionPort authSessionPort,
                                                Clock clock) {
        return new AuthenticationService(userAccountPort, passwordHashingPort, jwtTokenPort,
                tokenFingerprintPort, authSessionPort, clock);
    }
}

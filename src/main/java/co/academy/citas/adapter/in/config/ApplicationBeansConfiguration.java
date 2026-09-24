package co.academy.citas.adapter.in.config;

import co.academy.citas.application.port.out.AuthSessionPort;
import co.academy.citas.application.port.out.JwtTokenPort;
import co.academy.citas.application.port.out.PasswordHashingPort;
import co.academy.citas.application.port.out.TokenFingerprintPort;
import co.academy.citas.application.port.out.UserAccountPort;
import co.academy.citas.application.service.AuthenticationService;
import co.academy.citas.application.service.AppointmentFlowService;
import co.academy.citas.application.service.ProfessionalCatalogService;
import co.academy.citas.application.service.ProfessionalOfferService;
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
    AppointmentFlowService appointmentFlowService(co.academy.citas.application.port.out.AppointmentFlowPort appointmentFlowPort,
                                                  Clock clock) {
        return new AppointmentFlowService(appointmentFlowPort, clock);
    }

    @Bean
    RegistrationService registrationService(UserAccountPort userAccountPort, PasswordHashingPort passwordHashingPort) {
        return new RegistrationService(userAccountPort, passwordHashingPort);
    }

    @Bean
    ProfessionalOfferService professionalOfferService(UserAccountPort userAccountPort,
                                                      co.academy.citas.application.port.out.ProfessionalOfferPort professionalOfferPort,
                                                      PasswordHashingPort passwordHashingPort) {
        return new ProfessionalOfferService(userAccountPort, professionalOfferPort, passwordHashingPort);
    }

    @Bean
    ProfessionalCatalogService professionalCatalogService(
            co.academy.citas.application.port.out.ProfessionalOfferPort professionalOfferPort) {
        return new ProfessionalCatalogService(professionalOfferPort);
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

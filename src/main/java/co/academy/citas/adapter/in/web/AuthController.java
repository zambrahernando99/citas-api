package co.academy.citas.adapter.in.web;

import co.academy.citas.application.port.in.LoginCommand;
import co.academy.citas.application.port.in.LogoutCommand;
import co.academy.citas.application.port.in.RefreshSessionCommand;
import co.academy.citas.application.port.in.RegisterUserCommand;
import co.academy.citas.application.port.in.RegisterUserUseCase;
import co.academy.citas.application.port.in.SessionUseCase;
import co.academy.citas.application.port.in.TokenPair;
import co.academy.citas.domain.account.UserAccount;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final RegisterUserUseCase registerUserUseCase;
    private final SessionUseCase sessionUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, SessionUseCase sessionUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.sessionUseCase = sessionUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisteredUserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        UserAccount user = registerUserUseCase.register(new RegisterUserCommand(request.givenNames(),
                request.familyNames(), request.documentType(), request.documentNumber(), request.email(),
                request.phone(), request.password()));
        return ResponseEntity.status(HttpStatus.CREATED).body(RegisteredUserResponse.from(user));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return TokenResponse.from(sessionUseCase.login(new LoginCommand(request.email(), request.password())));
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return TokenResponse.from(sessionUseCase.refresh(new RefreshSessionCommand(request.refreshToken())));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        sessionUseCase.logout(new LogoutCommand(request.refreshToken()));
        return ResponseEntity.noContent().build();
    }

    public record RegisterUserRequest(
            @NotBlank String givenNames,
            @NotBlank String familyNames,
            @NotBlank String documentType,
            @NotBlank String documentNumber,
            @NotBlank @Email String email,
            @NotBlank String phone,
            @NotBlank String password) {
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    public record RefreshTokenRequest(@NotBlank String refreshToken) {
    }

    public record RegisteredUserResponse(UUID id, String givenNames, String familyNames, String documentType,
                                         String documentNumber, String email, String phone, List<String> roles) {
        static RegisteredUserResponse from(UserAccount user) {
            return new RegisteredUserResponse(user.id(), user.givenNames(), user.familyNames(),
                    user.documentType(), user.documentNumber(), user.email(), user.phone(),
                    user.roles().stream().map(Enum::name).sorted(Comparator.naturalOrder()).toList());
        }
    }

    public record TokenResponse(String accessToken, String refreshToken, long accessTokenExpiresInSeconds,
                                long refreshTokenExpiresInSeconds) {
        static TokenResponse from(TokenPair tokenPair) {
            return new TokenResponse(tokenPair.accessToken(), tokenPair.refreshToken(),
                    tokenPair.accessTokenExpiresInSeconds(), tokenPair.refreshTokenExpiresInSeconds());
        }
    }
}

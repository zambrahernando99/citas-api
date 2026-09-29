package co.academy.citas.application.port.in;

public interface SessionUseCase {
    TokenPair login(LoginCommand command);
    TokenPair refresh(RefreshSessionCommand command);
    void logout(LogoutCommand command);
}

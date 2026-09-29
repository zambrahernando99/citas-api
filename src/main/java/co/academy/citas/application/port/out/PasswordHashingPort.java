package co.academy.citas.application.port.out;

public interface PasswordHashingPort {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String passwordHash);
}

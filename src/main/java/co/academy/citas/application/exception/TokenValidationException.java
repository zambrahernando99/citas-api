package co.academy.citas.application.exception;

public class TokenValidationException extends RuntimeException {
    public TokenValidationException() {
        super("Token validation failed");
    }
}

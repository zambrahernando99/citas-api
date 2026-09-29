package co.academy.citas.application.exception;

public class InvalidPasswordResetTokenException extends RuntimeException {
    public InvalidPasswordResetTokenException() { super("Password reset token is invalid"); }
}

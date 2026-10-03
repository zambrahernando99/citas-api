package co.academy.citas.application.exception;

public class InvalidReminderRequestException extends RuntimeException {
    public InvalidReminderRequestException(String message) { super(message); }
}

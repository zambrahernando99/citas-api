package co.academy.citas.application.exception;

public class AppointmentConflictException extends RuntimeException {
    public AppointmentConflictException() { super("The selected slots are no longer available"); }
}

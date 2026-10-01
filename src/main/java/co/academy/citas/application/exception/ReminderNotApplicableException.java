package co.academy.citas.application.exception;

public class ReminderNotApplicableException extends RuntimeException {
    public ReminderNotApplicableException() { super("Appointment is no longer approved"); }
}

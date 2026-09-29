package co.academy.citas.application.exception;

public class AppointmentNotFoundException extends RuntimeException {
    public AppointmentNotFoundException() { super("Appointment was not found"); }
}

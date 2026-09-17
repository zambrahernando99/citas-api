package co.academy.citas.application.exception;

public class DocumentAlreadyRegisteredException extends RuntimeException {
    public DocumentAlreadyRegisteredException() {
        super("Document already registered");
    }
}

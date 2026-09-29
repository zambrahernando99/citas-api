package co.academy.citas.application.port.in;

public record RegisterUserCommand(
        String givenNames,
        String familyNames,
        String documentType,
        String documentNumber,
        String email,
        String phone,
        String password) {
}

package co.academy.citas.application.port.in;

public record CreateProfessionalCommand(String givenNames, String familyNames, String documentType,
                                        String documentNumber, String email, String phone,
                                        String temporaryPassword, String professionalCode, String licenseNumber) {
}

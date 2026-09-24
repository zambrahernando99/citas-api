package co.academy.citas.adapter.in.web;

import co.academy.citas.application.exception.DocumentAlreadyRegisteredException;
import co.academy.citas.application.exception.EmailAlreadyRegisteredException;
import co.academy.citas.application.exception.InvalidCredentialsException;
import co.academy.citas.application.exception.InvalidRefreshTokenException;
import co.academy.citas.application.exception.LicenseNumberAlreadyRegisteredException;
import co.academy.citas.application.exception.ProfessionalCodeAlreadyRegisteredException;
import co.academy.citas.application.exception.ProfessionalNotFoundException;
import co.academy.citas.application.exception.AppointmentConflictException;
import co.academy.citas.application.exception.AppointmentNotFoundException;
import co.academy.citas.application.exception.InvalidAppointmentException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "validation_failed", "Request validation failed");
        problem.setProperty("fieldErrors", fieldErrors);
        return response(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> unreadableRequest() {
        return response(problem(HttpStatus.BAD_REQUEST, "validation_failed", "Request body is invalid"));
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    ResponseEntity<ProblemDetail> duplicateEmail() {
        return response(problem(HttpStatus.CONFLICT, "email_already_registered", "Email is already registered"));
    }

    @ExceptionHandler(DocumentAlreadyRegisteredException.class)
    ResponseEntity<ProblemDetail> duplicateDocument() {
        return response(problem(HttpStatus.CONFLICT, "document_already_registered", "Document is already registered"));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ProblemDetail> invalidCredentials() {
        return response(problem(HttpStatus.UNAUTHORIZED, "invalid_credentials", "Invalid credentials"));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    ResponseEntity<ProblemDetail> invalidRefresh() {
        return response(problem(HttpStatus.UNAUTHORIZED, "invalid_refresh_token", "Refresh token is invalid"));
    }

    @ExceptionHandler(ProfessionalCodeAlreadyRegisteredException.class)
    ResponseEntity<ProblemDetail> duplicateProfessionalCode() {
        return response(problem(HttpStatus.CONFLICT, "professional_code_already_registered",
                "Professional code is already registered"));
    }

    @ExceptionHandler(LicenseNumberAlreadyRegisteredException.class)
    ResponseEntity<ProblemDetail> duplicateLicenseNumber() {
        return response(problem(HttpStatus.CONFLICT, "license_number_already_registered",
                "License number is already registered"));
    }

    @ExceptionHandler(ProfessionalNotFoundException.class)
    ResponseEntity<ProblemDetail> professionalNotFound() {
        return response(problem(HttpStatus.NOT_FOUND, "professional_not_found", "Professional was not found"));
    }

    @ExceptionHandler(AppointmentConflictException.class)
    ResponseEntity<ProblemDetail> appointmentConflict() { return response(problem(HttpStatus.CONFLICT, "slot_unavailable", "The selected slots are no longer available")); }

    @ExceptionHandler(AppointmentNotFoundException.class)
    ResponseEntity<ProblemDetail> appointmentNotFound() { return response(problem(HttpStatus.NOT_FOUND, "appointment_not_found", "Appointment was not found or is no longer pending")); }

    @ExceptionHandler(InvalidAppointmentException.class)
    ResponseEntity<ProblemDetail> invalidAppointment(InvalidAppointmentException exception) { return response(problem(HttpStatus.BAD_REQUEST, "invalid_appointment", exception.getMessage())); }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ProblemDetail> invalidOffer() {
        return response(problem(HttpStatus.BAD_REQUEST, "invalid_professional_offer",
                "Professional specialties or locations are invalid"));
    }

    private ProblemDetail problem(HttpStatus status, String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty("code", code);
        return problem;
    }

    private ResponseEntity<ProblemDetail> response(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}

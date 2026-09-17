package co.academy.citas.adapter.in.web;

import co.academy.citas.application.exception.DocumentAlreadyRegisteredException;
import co.academy.citas.application.exception.EmailAlreadyRegisteredException;
import co.academy.citas.application.exception.InvalidCredentialsException;
import co.academy.citas.application.exception.InvalidRefreshTokenException;
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

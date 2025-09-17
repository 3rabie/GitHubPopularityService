package com.redcare.popularity.exception;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(GitHubApiException.class)
    public ResponseEntity<ErrorResponse> handleGitHub(GitHubApiException ex, HttpServletRequest req) {
        int upstream = ex.getStatusCode();
        HttpStatus status;
        if (upstream == 429) {
            status = HttpStatus.TOO_MANY_REQUESTS;
        } else if (upstream >= 400 && upstream < 500) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            status = HttpStatus.BAD_GATEWAY;
        }

        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            log.warn("Upstream rate limited (path={}): {}", req.getRequestURI(), ex.getMessage());
        } else if (status == HttpStatus.BAD_REQUEST) {
            log.warn("Upstream 4xx mapped to 400 (upstream={}, path={}): {}", upstream, req.getRequestURI(), ex.getMessage());
        } else {
            log.error("Upstream error (status={}, path={}): {}", upstream, req.getRequestURI(), ex.getMessage());
        }
        String message = (status == HttpStatus.TOO_MANY_REQUESTS)
                ? "GitHub rate limit reached. Try again later."
                : (status == HttpStatus.BAD_REQUEST ? "Invalid request. Please review your parameters." : "Temporary upstream issue. Please try again.");
        ErrorResponse body = new ErrorResponse(status.value(), status.getReasonPhrase(), message);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled error (path={}): {}", req.getRequestURI(), ex.getMessage());
        ErrorResponse body = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Something went wrong. Please try again.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorResponse> handleCircuitOpen(CallNotPermittedException ex, HttpServletRequest req) {
        log.error("Circuit open (path={}): {}", req.getRequestURI(), ex.getMessage());
        ErrorResponse body = new ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(), "Service temporarily unavailable. Please try again shortly.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest req) {
        log.warn("Bad request (path={}): {}", req.getRequestURI(), ex.getMessage());
        ErrorResponse body = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), "Invalid request. Please review your parameters.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(body);
    }
}

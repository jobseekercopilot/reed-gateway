package com.jobseekercopilot.reedgateway.exception;

import com.jobseekercopilot.reedgateway.client.ReedApiClient;
import com.jobseekercopilot.reedgateway.model.dto.ErrorResponse;
import com.jobseekercopilot.reedgateway.model.dto.ExternalSearchResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(org.springframework.web.bind.MethodArgumentNotValidException ex,
                                                                  org.springframework.http.HttpHeaders headers,
                                                                  org.springframework.http.HttpStatusCode status,
                                                                  org.springframework.web.context.request.WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        ErrorResponse error = new ErrorResponse("INVALID_REQUEST", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ReedApiClient.ReedApiException.class)
    public ResponseEntity<Object> handleReedApiException(ReedApiClient.ReedApiException ex) {
        String code = switch (ex.getStatus()) {
            case TOO_MANY_REQUESTS -> "RATE_LIMITED";
            case UNAUTHORIZED, FORBIDDEN -> "CONFIGURATION_ERROR";
            default -> "SERVICE_UNAVAILABLE";
        };
        String message = switch (ex.getStatus()) {
            case TOO_MANY_REQUESTS -> "Reed rate limit reached";
            case UNAUTHORIZED, FORBIDDEN -> "Reed configuration rejected";
            default -> "External job search service is temporarily unavailable";
        };
        ErrorResponse error = new ErrorResponse(code, message);
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        ErrorResponse error = new ErrorResponse("INVALID_REQUEST", "Malformed JSON request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}

package com.example.customer.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /** ------------------- Validation Errors ------------------- **/

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        return buildResponse(HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "Some fields are invalid",
                fieldErrors);
    }

    /** ------------------- Missing Params / Bad JSON ------------------- **/

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        return buildResponse(HttpStatus.BAD_REQUEST,
                "Missing Request Parameter",
                "Required parameter is missing: " + ex.getParameterName(),
                null);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        return buildResponse(HttpStatus.BAD_REQUEST,
                "Malformed JSON Request",
                ex.getMostSpecificCause().getMessage(),
                null);
    }

    /** ------------------- Auth & Security ------------------- **/

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED,
                "Invalid Credentials",
                ex.getMessage(),
                null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN,
                "Access Denied",
                ex.getMessage(),
                null);
    }

    /** ------------------- Database & Integrity ------------------- **/

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex) {
        return buildResponse(HttpStatus.CONFLICT,
                "Data Integrity Violation",
                ex.getMostSpecificCause().getMessage(),
                null);
    }

    /** ------------------- Unsupported Method ------------------- **/

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED,
                "Method Not Allowed",
                ex.getMessage(),
                null);
    }

    /** ------------------- Business Errors ------------------- **/

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArg(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Invalid Request",
                ex.getMessage(),
                null);
    }

    /** ------------------- Fallback Catch-All ------------------- **/

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected Error",
                ex.getMessage(),
                null);
    }

    /** ------------------- Common Builder ------------------- **/

    private ResponseEntity<Object> buildResponse(HttpStatus status, String error, String message, Object details) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        if (details != null) {
            body.put("details", details);
        }
        return new ResponseEntity<>(body, status);
    }
}

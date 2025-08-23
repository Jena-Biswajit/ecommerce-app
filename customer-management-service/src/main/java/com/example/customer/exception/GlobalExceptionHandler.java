package com.example.customer.exception;
//
//import com.example.exception.ResourceNotFoundException;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.context.request.WebRequest;
//import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@ControllerAdvice
//public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
//
//    // 1. Handle validation errors (@Valid DTOs)
////    @Override
//    protected ResponseEntity<Object> handleMethodArgumentNotValid(
//            MethodArgumentNotValidException ex,
//            HttpHeaders headers,
//            HttpStatus status,
//            WebRequest request) {
//
//        Map<String, Object> body = new HashMap<>();
//        body.put("timestamp", LocalDateTime.now());
//        body.put("status", HttpStatus.BAD_REQUEST.value());
//        body.put("error", "Validation Failed");
//
//        Map<String, String> fieldErrors = ex.getBindingResult()
//                .getFieldErrors()
//                .stream()
//                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
//
//        body.put("fieldErrors", fieldErrors);
//
//        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
//    }
//
//    // 2. Handle invalid path variables or query params
//    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
//    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
//        Map<String, Object> body = new HashMap<>();
//        body.put("timestamp", LocalDateTime.now());
//        body.put("status", HttpStatus.BAD_REQUEST.value());
//        body.put("error", "Invalid Parameter");
//        body.put("message", "Parameter '" + ex.getName() + "' must be of type " + ex.getRequiredType().getSimpleName());
//        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
//    }
//
//    // 3. Handle custom NotFoundException
//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex) {
//        Map<String, Object> body = new HashMap<>();
//        body.put("timestamp", LocalDateTime.now());
//        body.put("status", HttpStatus.NOT_FOUND.value());
//        body.put("error", "Resource Not Found");
//        body.put("message", ex.getMessage());
//        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
//    }
//
//    // 4. Handle IllegalArgument / Business Logic Errors
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
//        Map<String, Object> body = new HashMap<>();
//        body.put("timestamp", LocalDateTime.now());
//        body.put("status", HttpStatus.BAD_REQUEST.value());
//        body.put("error", "Invalid Request");
//        body.put("message", ex.getMessage());
//        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
//    }
//
//    // 5. Catch-all for unexpected exceptions
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Object> handleGenericException(Exception ex) {
//        Map<String, Object> body = new HashMap<>();
//        body.put("timestamp", LocalDateTime.now());
//        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
//        body.put("error", "Internal Server Error");
//        body.put("message", ex.getMessage());
//        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//}

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

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        return buildResponse(HttpStatus.BAD_REQUEST,
                "Validation Failed",
                errors.toString());
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
                "Required parameter is missing: " + ex.getParameterName());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        return buildResponse(HttpStatus.BAD_REQUEST,
                "Malformed JSON Request",
                ex.getMostSpecificCause().getMessage());
    }

    /** ------------------- Auth & Security ------------------- **/

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED,
                "Invalid Credentials",
                ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN,
                "Access Denied",
                ex.getMessage());
    }

    /** ------------------- Database & Integrity ------------------- **/

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex) {
        return buildResponse(HttpStatus.CONFLICT,
                "Data Integrity Violation",
                ex.getMostSpecificCause().getMessage());
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
                ex.getMessage());
    }

    /** ------------------- Fallback Catch-All ------------------- **/

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected Error",
                ex.getMessage());
    }

    /** ------------------- Common Builder ------------------- **/

    private ResponseEntity<Object> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}

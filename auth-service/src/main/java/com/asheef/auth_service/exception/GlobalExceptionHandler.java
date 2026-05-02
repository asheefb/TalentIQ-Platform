package com.asheef.auth_service.exception;

import com.asheef.auth_service.util.ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(
                new ResponseDto(Boolean.FALSE, HttpStatus.BAD_REQUEST.value(), msg)
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDto> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ResponseDto(Boolean.FALSE, HttpStatus.UNAUTHORIZED.value(), e.getMessage())
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ResponseDto> handleForbidden(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ResponseDto(Boolean.FALSE, HttpStatus.FORBIDDEN.value(), e.getMessage())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto> handleAny(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseDto(Boolean.FALSE, HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal server error")
        );
    }
}

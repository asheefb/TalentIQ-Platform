package com.asheef.resumeAnalyzer.exception;

import com.asheef.common.utils.ResponseDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ResponseDto> handleAiServiceException(
            AiServiceException e
    ) {

        return ResponseEntity
                .status(HttpStatus.valueOf(e.getStatus().value()))
                .body(new ResponseDto(Boolean.FALSE, e.getStatus().value(), e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ResponseDto(Boolean.FALSE, HttpStatus.CONFLICT.value(), ex.getMessage())
        );
    }
}

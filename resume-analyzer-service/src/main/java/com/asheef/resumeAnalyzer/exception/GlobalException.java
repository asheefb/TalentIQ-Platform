package com.asheef.resumeAnalyzer.exception;

import com.asheef.common.utils.ResponseDto;
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
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ResponseDto(Boolean.FALSE, HttpStatus.TOO_MANY_REQUESTS.value(), e.getMessage()));
    }
}

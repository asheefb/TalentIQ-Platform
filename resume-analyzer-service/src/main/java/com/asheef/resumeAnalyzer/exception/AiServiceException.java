package com.asheef.resumeAnalyzer.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class AiServiceException extends RuntimeException {

    private final HttpStatus status;


    public AiServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}

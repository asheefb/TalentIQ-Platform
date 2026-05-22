package com.asheef.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDto {

    private Boolean success;

    private Integer status;

    private String message;

    private Object data;

    private List<ErrorStructure> errors;

    private LocalDateTime timestamp;

    public ResponseDto(Boolean success, Integer status, String message) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ResponseDto(Boolean success, Integer status, List<ErrorStructure> errors) {
        this.success = success;
        this.status = status;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }

    public ResponseDto(Boolean success, Integer status, Object data, String message) {
        this.success = success;
        this.status = status;
        this.data = data;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}

package com.asheef.user_service.exception;

import com.asheef.common.utils.ErrorStructure;
import com.asheef.common.utils.ResponseDto;
import com.asheef.user_service.constants.Constant;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private List<ErrorStructure> mapFieldErrors(List<FieldError> fieldErrors) {
        List<ErrorStructure> errors = new ArrayList<>();

        fieldErrors.forEach(fieldError -> {
            Object rejectedValue = fieldError.getRejectedValue();

            errors.add(new ErrorStructure(
                    rejectedValue != null ? rejectedValue.toString() : null,
                    fieldError.getDefaultMessage(),
                    fieldError.getField()
            ));
        });
        return errors;
    }

    /**
     * Global Exception for RequestBody Dto Validation
     *
     * @param e
     * @return errors Structure
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto> handleMethodValidException(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(
                new ResponseDto(Boolean.FALSE, HttpStatus.BAD_REQUEST.value(), mapFieldErrors(e.getFieldErrors()))
        );
    }

    /**
     * This is for handle Exception the dto Validation if the method accepting @ModelAttribute
     *
     * @param e
     * @return error Structure
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ResponseDto> handleBindException(BindException e) {
        return ResponseEntity.badRequest().body(
                new ResponseDto(Boolean.FALSE, HttpStatus.BAD_REQUEST.value(), mapFieldErrors(e.getFieldErrors()))
        );
    }

    /**
     * This is for handle Exception for Constraint Violation
     *
     * @param ex
     * @return error Structure
     * @throws ConstraintViolationException
     */

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseDto> handleConstraintViolation(ConstraintViolationException ex) {
        var errors = new ArrayList<ErrorStructure>();

        ex.getConstraintViolations().forEach(violation -> {
            ErrorStructure errorStructure = new ErrorStructure(
                    String.valueOf(violation.getInvalidValue()),
                    violation.getMessage(),
                    violation.getPropertyPath().toString()
            );
            errors.add(errorStructure);
        });
        return ResponseEntity.badRequest().body(
                new ResponseDto(Boolean.FALSE, HttpStatus.BAD_REQUEST.value(), errors)
        );
    }

    /**
     * This if for handle Exception for Data Integrity Violation
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ResponseDto(Boolean.FALSE, HttpStatus.CONFLICT.value(), Constant.CONFLICT)
        );
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ResponseDto> handleNoSuchEx(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseDto(Boolean.FALSE, HttpStatus.NOT_FOUND.value(), e.getMessage())
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseDto> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseDto(Boolean.FALSE, HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage())
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDto> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ResponseDto(Boolean.FALSE, HttpStatus.BAD_REQUEST.value(), e.getMessage())
        );
    }


}

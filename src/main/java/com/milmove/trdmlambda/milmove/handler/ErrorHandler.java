package com.milmove.trdmlambda.milmove.handler;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.milmove.trdmlambda.milmove.model.ErrorResponse;
import com.milmove.trdmlambda.milmove.model.Errors;

import ch.qos.logback.classic.Logger;

@ControllerAdvice
public class ErrorHandler {
    private Logger logger = (Logger) LoggerFactory.getLogger(ErrorHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException exception) {
        List<Errors> errors = new ArrayList<>();
        for (FieldError err : exception.getBindingResult().getFieldErrors()) {
            Errors error = new Errors();
            error.setField(err.getField());
            error.setMessage(err.getDefaultMessage());
            errors.add(error);
        }
        logger.error("Validation error: {}", errors);
        return response(HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception exception) {
        List<Errors> errors = new ArrayList<>();
        Errors error = new Errors();
        error.setMessage(exception.getMessage());
        errors.add(error);
        logger.error("Unexpected error: {}", error.getMessage(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, errors);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handle(Throwable throwable) {
        List<Errors> errors = new ArrayList<>();
        Errors error = new Errors();
        error.setMessage(throwable.getMessage());
        errors.add(error);
        logger.error("Thrown error: {}", error.getMessage(), throwable);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, errors);
    }


    private ResponseEntity<ErrorResponse> response(HttpStatus status, List<Errors> errs) {
        ErrorResponse response = new ErrorResponse();
        response.setErrors(errs);
        return ResponseEntity.status(status).body(response);
    }
}

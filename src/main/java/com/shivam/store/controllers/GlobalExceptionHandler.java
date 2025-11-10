package com.shivam.store.controllers;

import com.shivam.store.dtos.ErrorDto;
import com.shivam.store.exceptions.IncorrectUserException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> handleUnreadableMessage() {
        return ResponseEntity.badRequest().body(new ErrorDto("Error: invalid type"));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationError(MethodArgumentNotValidException exception){
       var map = new HashMap<String, String>();

        exception.getBindingResult().getFieldErrors().forEach(error ->{map.put(error.getField(), error.getDefaultMessage());});

       return ResponseEntity.badRequest().body(map);

    }

    @ExceptionHandler(IncorrectUserException.class)
    public ResponseEntity<ErrorDto> handleIncorrectUser() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorDto("You do not have access to this cart"));
    }
    
}

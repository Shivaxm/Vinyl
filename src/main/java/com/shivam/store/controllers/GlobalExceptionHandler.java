package com.shivam.store.controllers;

import com.shivam.store.dtos.ErrorDto;
import com.shivam.store.exceptions.CartItemNotFoundException;
import com.shivam.store.exceptions.CartNotFoundException;
import com.shivam.store.exceptions.IncorrectUserException;
import com.shivam.store.payments.PaymentException;
import com.shivam.store.payments.WebhookSignatureException;
import com.shivam.store.exceptions.ProductNotFoundException;
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

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCartNotFound(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorDto> handleProductNotFound(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto("Product not found"));
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<ErrorDto> handleBadCartRequest(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorDto> handlePaymentException() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto("Error creating a checkout session"));
    }

    @ExceptionHandler(WebhookSignatureException.class)
    public ResponseEntity<ErrorDto> handleWebhookSignatureException(WebhookSignatureException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto(exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorDto(exception.getMessage()));
    }
}

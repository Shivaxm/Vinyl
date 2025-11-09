package com.shivam.store.controllers;

import com.shivam.store.dtos.OrderDto;
import com.shivam.store.exceptions.IncorrectUserException;
import com.shivam.store.exceptions.OrderNotFoundException;
import com.shivam.store.services.AuthService;
import com.shivam.store.services.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@AllArgsConstructor
public class OrdersController {

    private final OrderService orderService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<OrderDto>> getOrders() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var user = authService.getUser();
        return ResponseEntity.ok(orderService.findAll(user));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable(name = "orderId") BigInteger orderId) {

        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCartNotFound(){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error:", "order not found"));
    }

    @ExceptionHandler(IncorrectUserException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFound(){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error:", "Wrong user"));
    }
}

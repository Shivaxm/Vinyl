package com.shivam.store.controllers;

import com.shivam.store.dtos.JwtResponse;
import com.shivam.store.dtos.UserRequest;
import com.shivam.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody UserRequest userRequest){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userRequest.getEmail(), userRequest.getPassword()));

        var user = userRepository.findByEmail(userRequest.getEmail()).orElseThrow();




    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadLogin(Exception exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}

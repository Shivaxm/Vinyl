package com.shivam.store.controllers;

import com.shivam.store.config.JwtConfig;
import com.shivam.store.dtos.JwtResponse;
import com.shivam.store.dtos.UserDto;
import com.shivam.store.dtos.UserRequest;
import com.shivam.store.mappers.UserMapper;
import com.shivam.store.repositories.UserRepository;
import com.shivam.store.services.CartOwnershipService;
import com.shivam.store.services.JwtService;
import com.shivam.store.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final UserMapper userMapper;
    private final CartOwnershipService cartOwnershipService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody UserRequest userRequest, HttpServletResponse response,
                                             @CookieValue(value="guestToken", required=false) String guestToken) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userRequest.getEmail(), userRequest.getPassword()));

        var user = userRepository.findByEmail(userRequest.getEmail()).orElseThrow();
        var refreshToken = jwtService.generateRefreshToken(user);

        var cookie = new Cookie("refreshToken", refreshToken.toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(jwtConfig.getRefreshExpiration());
        cookie.setSecure(true);
        response.addCookie(cookie);

        cartOwnershipService.promoteToUser(guestToken, user);

        if (guestToken != null) {
            Cookie clearGuest = new Cookie("guestToken", "");
            clearGuest.setPath("/");
            clearGuest.setHttpOnly(true);
            clearGuest.setSecure(true);
            clearGuest.setMaxAge(0);
            response.addCookie(clearGuest);
        }

        return ResponseEntity.ok(new JwtResponse(jwtService.generateAccessToken(user).toString()));

    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var id = (Long)authentication.getPrincipal();
        var user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        var userDto = userMapper.toDto(user);
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@CookieValue(value = "refreshToken") String refreshToken) {
        var jwt = jwtService.parseToken(refreshToken);
        if(jwt == null || jwt.isExpired() || jwt.isGuest()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var userId = jwt.getUserId();
        var user = userRepository.findById(userId).orElseThrow();
        var accessToken = jwtService.generateAccessToken(user);

        return ResponseEntity.ok(new JwtResponse(accessToken.toString()));

    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadLogin(Exception exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}


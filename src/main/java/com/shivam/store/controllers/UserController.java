package com.shivam.store.controllers;

import com.shivam.store.dtos.ChangePasswordRequest;
import com.shivam.store.dtos.RegisterUserRequest;
import com.shivam.store.dtos.UpdateUserRequest;
import com.shivam.store.dtos.UserDto;
import com.shivam.store.entities.Role;
import com.shivam.store.mappers.UserMapper;
import com.shivam.store.repositories.UserRepository;
import com.shivam.store.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @GetMapping
    public List<UserDto> getAllUsers(@RequestParam(required = false, defaultValue = "", name = "sort")String sort) {
        var currentUser = authService.getUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }
        if(!Set.of("name", "email").contains(sort)){
            sort = "name";
        }
        
        return userRepository.findAll(Sort.by(sort)).stream().map(userMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        var currentUser = authService.getUser();
        // OWASP A01: users can only read their own profile unless they are admins.
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return userRepository.findById(id)
            .map(userMapper::toDto)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody RegisterUserRequest request, UriComponentsBuilder uriBuilder) {
        if(userRepository.existsByEmail(request.getEmail())){
            return ResponseEntity.badRequest().body(Map.of("email", "already registered"));
        }
        var u = userMapper.toEntity(request);
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        u.setRole(Role.USER);
        userRepository.save(u);
        var l = userMapper.toDto(u);
        var uri = uriBuilder.path("/users/{id}").buildAndExpand(l.getId()).toUri();
        return ResponseEntity.created(uri).body(l);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable(name = "id") long id, @Valid @RequestBody UpdateUserRequest request) {
        var currentUser = authService.getUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getId() != id) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var user = userRepository.findById(id).orElse(null);
        if(user == null){
            return ResponseEntity.notFound().build();
        }
        userMapper.update(request, user);
        userRepository.save(user);

        return ResponseEntity.ok(userMapper.toDto(user));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        var currentUser = authService.getUser();
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var user = userRepository.findById(id).orElse(null);
        if(user == null){
            return ResponseEntity.notFound().build();
        }
        userRepository.delete(user);
        return ResponseEntity.noContent().build();

    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordRequest request) {
        var currentUser = authService.getUser();
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var user = userRepository.findById(id).orElse(null);
        if(user == null){
            return ResponseEntity.notFound().build();
        }
        if(passwordEncoder.matches(request.getOldPassword(), user.getPassword())){
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            return ResponseEntity.noContent().build();
        } 
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Void> handleAccessDenied() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    
    
    
    
    
    
}

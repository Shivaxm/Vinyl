package com.shivam.store.services;

import com.shivam.store.entities.User;
import com.shivam.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public User getUser() {
        return findAuthenticatedUser()
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Optional<User> findAuthenticatedUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        var principal = authentication.getPrincipal();
        if (!(principal instanceof Long id)) {
            return Optional.empty();
        }
        return userRepository.findById(id);
    }
}

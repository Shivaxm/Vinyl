package com.shivam.store.services;

import com.shivam.store.entities.User;
import com.shivam.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public User getUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var id = (Long)authentication.getPrincipal();
        var user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        } else {
            return user;
        }
    }
}

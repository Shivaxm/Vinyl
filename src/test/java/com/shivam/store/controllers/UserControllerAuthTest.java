package com.shivam.store.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivam.store.entities.Role;
import com.shivam.store.entities.User;
import com.shivam.store.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserControllerAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void clean() {
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUsers_forbiddenForNonAdmin() throws Exception {
        var user = saveUser("user@example.com", Role.USER);
        authenticate(user.getId(), Role.USER);

        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsers_allowedForAdmin() throws Exception {
        var admin = saveUser("admin@example.com", Role.ADMIN);
        authenticate(admin.getId(), Role.ADMIN);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void updateOtherUser_forbiddenForNonAdmin() throws Exception {
        var user = saveUser("user@example.com", Role.USER);
        var other = saveUser("other@example.com", Role.USER);
        authenticate(user.getId(), Role.USER);

        var payload = objectMapper.writeValueAsString(new UpdateUserRequestBody("New Name", null));

        mockMvc.perform(put("/users/{id}", other.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateSelf_allowedForUser() throws Exception {
        var user = saveUser("user@example.com", Role.USER);
        authenticate(user.getId(), Role.USER);

        var payload = objectMapper.writeValueAsString(new UpdateUserRequestBody("New Name", null));

        mockMvc.perform(put("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        var updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("New Name");
    }

    @Test
    void deleteOtherUser_forbiddenForNonAdmin() throws Exception {
        var user = saveUser("user@example.com", Role.USER);
        var other = saveUser("other@example.com", Role.USER);
        authenticate(user.getId(), Role.USER);

        mockMvc.perform(delete("/users/{id}", other.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteSelf_allowedForUser() throws Exception {
        var user = saveUser("user@example.com", Role.USER);
        authenticate(user.getId(), Role.USER);

        mockMvc.perform(delete("/users/{id}", user.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    private User saveUser(String email, Role role) {
        var u = new User();
        u.setEmail(email);
        u.setName(email);
        u.setPassword("pw");
        u.setRole(role);
        return userRepository.save(u);
    }

    private void authenticate(Long userId, Role role) {
        var auth = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private record UpdateUserRequestBody(String name, String email) {}
}


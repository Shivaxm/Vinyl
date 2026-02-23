package com.shivam.store.dtos;

import com.shivam.store.validation.Lowercase;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {
    // OWASP A03: validate auth input before authentication processing.
    @NotBlank(message = "email required")
    @Email(message = "invalid email")
    @Lowercase
    private String email;

    @NotBlank(message = "password required")
    @Size(min = 6, max = 100, message = "password must be between 6 and 100 characters")
    private String password;
}


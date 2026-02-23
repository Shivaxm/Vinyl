package com.shivam.store.dtos;

import com.shivam.store.validation.Lowercase;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterUserRequest {
    
    @NotBlank(message ="name required")
    @Size(min = 2, max = 100, message = "name must be between 2 and 100 characters")
    @Pattern(regexp = "^[\\p{L}0-9 .,'-]+$", message = "name contains invalid characters")
    private String name;
    
    @NotBlank(message="email required")
    @Email
    @Lowercase
    @Size(max = 255, message = "email too long")
    private String email;

    @NotBlank(message = "password required")
    @Size(min = 6, max = 100, message = "password must between 6 and 100")
    private String password;
}

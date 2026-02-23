package com.shivam.store.dtos;

import com.shivam.store.validation.Lowercase;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    // OWASP A03: constrain profile fields to expected format/size.
    @Size(min = 2, max = 100, message = "name must be between 2 and 100 characters")
    @Pattern(regexp = "^[\\p{L}0-9 .,'-]+$", message = "name contains invalid characters")
    private String name;

    @Email(message = "invalid email")
    @Lowercase
    @Size(max = 255, message = "email too long")
    private String email;
}

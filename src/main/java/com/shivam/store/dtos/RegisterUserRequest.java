package com.shivam.store.dtos;

import com.shivam.store.validation.Lowercase;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterUserRequest {
    
    @NotBlank(message ="name required")
    @Size(max = 255)
    private String name;
    
    @NotBlank(message="email required")
    @Email
    @Lowercase
    private String email;

    @Size(min = 6, max = 25, message = "password must between 6 and 25")
    private String password;
}

package com.shivam.store.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    // OWASP A03: reject blank/weak password payloads at request boundary.
    @NotBlank(message = "old password required")
    private String oldPassword;

    @NotBlank(message = "new password required")
    @Size(min = 6, max = 100, message = "new password must be between 6 and 100 characters")
    private String newPassword;
}

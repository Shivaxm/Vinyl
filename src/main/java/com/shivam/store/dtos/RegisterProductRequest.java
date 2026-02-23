package com.shivam.store.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RegisterProductRequest {
    // OWASP A03: validate product management payloads to reduce malformed data and injection surfaces.
    @NotBlank(message = "name required")
    @Size(min = 2, max = 120, message = "name must be between 2 and 120 characters")
    private String name;

    @NotBlank(message = "description required")
    @Size(min = 3, max = 1000, message = "description must be between 3 and 1000 characters")
    private String description;

    @NotNull(message = "price required")
    @DecimalMin(value = "0.01", message = "price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "price format is invalid")
    private BigDecimal price;

    @NotNull(message = "categoryId required")
    @Min(value = 1, message = "invalid categoryId")
    @Max(value = 127, message = "invalid categoryId")
    private Byte categoryId;
}

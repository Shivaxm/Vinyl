package com.shivam.store.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequestDto {
    @NotNull(message = "cartItem Id required")
    private Long id;
}

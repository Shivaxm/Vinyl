package com.shivam.store.payments;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CartRequest {
    @NotNull(message = "cartId required")
    private UUID cartId;
}

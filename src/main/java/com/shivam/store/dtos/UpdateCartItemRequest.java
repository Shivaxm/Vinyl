package com.shivam.store.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartItemRequest {
    @NotNull
    @Min(value =1,message = "Not less than 1")
    @Max(value=5, message="Not greater than 5")
    private Integer quantity;
}

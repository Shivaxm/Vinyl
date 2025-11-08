package com.shivam.store.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDto {
    private CartProductDto product;
    private int quantity;
    private BigDecimal totalPrice;


}

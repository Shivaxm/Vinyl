package com.shivam.store.dtos;

import com.shivam.store.entities.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class OrderDto {
    private Long id;
    private List<OrderItemDto> items;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private BigDecimal totalPrice;
}

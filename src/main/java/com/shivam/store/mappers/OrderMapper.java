package com.shivam.store.mappers;


import com.shivam.store.dtos.OrderDto;
import com.shivam.store.dtos.OrderItemDto;
import com.shivam.store.entities.Order;
import com.shivam.store.entities.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ProductMapper.class)
public interface OrderMapper {
    @Mapping(target = "items", source="items")
    @Mapping(target = "totalPrice", expression="java(order.getTotalPrice())")
    OrderDto toDto(Order order);
    @Mapping(target = "totalPrice", expression = "java(orderItem.getTotalPrice())")
    OrderItemDto toCartItemDto(OrderItem orderItem);
}

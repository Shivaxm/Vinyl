package com.shivam.store.mappers;

import com.shivam.store.dtos.CartDto;
import com.shivam.store.dtos.CartItemDto;
import com.shivam.store.entities.Cart;
import com.shivam.store.entities.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ProductMapper.class)
public interface CartMapper {
    Cart toEntity(CartDto cartDto);
    @Mapping(target = "items", expression = "java(cart.getCartItems().stream()\n                .sorted(java.util.Comparator.comparing(ci -> ci.getProduct().getId()))\n                .map(this::toCartItemDto)\n                .collect(java.util.stream.Collectors.toList()))")
    @Mapping(target = "totalPrice", expression="java(cart.getTotalPrice())")
    CartDto toDto(Cart cart);
    @Mapping(target = "totalPrice", expression = "java(cart.getTotalPrice())")
    CartItemDto toCartItemDto(CartItem cart);
}

package com.shivam.store.controllers;

import com.shivam.store.carts.CartOwner;
import com.shivam.store.dtos.*;
import com.shivam.store.exceptions.CartItemNotFoundException;
import com.shivam.store.exceptions.CartNotFoundException;
import com.shivam.store.exceptions.ProductNotFoundException;
import com.shivam.store.services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@Tag(name="Carts")
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartDto> createCart(CartOwner owner, UriComponentsBuilder uriBuilder) {
        var cartDto = cartService.createCart(owner);
        var uri = uriBuilder.path("/carts/{id}").buildAndExpand(cartDto.getId()).toUri();
        return ResponseEntity.created(uri).body(cartDto);
    }

    @PostMapping("/current/items")
    @Operation(summary = "Adds item to cart")
    public ResponseEntity<CartItemDto> addProduct(
            CartOwner owner,
            @Valid @RequestBody CartItemRequestDto prod) {
        var cartItemDto = cartService.addProductToCurrentCart(prod.getId(), owner);
        return ResponseEntity.ok(cartItemDto);
    }

    @GetMapping("/current")
    public ResponseEntity<CartDto> getCurrentCart(CartOwner owner) {
        return ResponseEntity.ok(cartService.getCurrentCart(owner));
    }

    @PutMapping("/current/items/{productId}")
    public ResponseEntity<?> updateCartItem(
            CartOwner owner,
            @PathVariable(name = "productId") Long productId,
            @Valid @RequestBody UpdateCartItemRequest updateCartItemRequest) {

        return ResponseEntity.ok(
                cartService.updateCurrentCartItem(productId, updateCartItemRequest.getQuantity(), owner));
    }

    @DeleteMapping("/current/items/{productId}")
    public ResponseEntity<?> deleteProduct(
            CartOwner owner,
            @PathVariable(name = "productId") Long productId) {
        cartService.deleteProductFromCurrentCart(productId, owner);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/current/items")
    public ResponseEntity<Void> clearCart(CartOwner owner) {
        cartService.clearCurrentCart(owner);
        return ResponseEntity.noContent().build();
    }

}

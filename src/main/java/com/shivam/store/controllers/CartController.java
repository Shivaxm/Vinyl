package com.shivam.store.controllers;

import com.shivam.store.dtos.*;
import com.shivam.store.exceptions.CartItemNotFoundException;
import com.shivam.store.exceptions.CartNotFoundException;
import com.shivam.store.exceptions.ProductNotFoundException;
import com.shivam.store.mappers.CartMapper;
import com.shivam.store.repositories.CartRepository;
import com.shivam.store.repositories.ProductRepository;
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
import java.util.UUID;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@Tag(name="Carts")
public class CartController {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartDto> createCart(UriComponentsBuilder uriBuilder) {

        var cartDto = cartService.createCart();
        var uri = uriBuilder.path("/carts/{id}").buildAndExpand(cartDto.getId()).toUri();
        return ResponseEntity.created(uri).body(cartDto);
    }

    @PostMapping("/{cartId}/items")
    @Operation(summary = "Adds item to cart")
    public ResponseEntity<CartItemDto> addProduct(@Parameter(description = "ID of cart") @PathVariable(name = "cartId") UUID id,
            @RequestBody CartItemRequestDto prod) {
        var cartItemDto = cartService.addProductToCart(id, prod.getId());
        return ResponseEntity.ok(cartItemDto);
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartDto> getCart(@PathVariable(name = "cartId") UUID id) {

        return ResponseEntity.ok(cartService.getCart(id));
    }

    @PutMapping("/{cartId}/items/{productId}")
    public ResponseEntity<?> updateCartItem(@PathVariable(name = "cartId") UUID id,
            @PathVariable(name = "productId") Long productId,
            @Valid @RequestBody UpdateCartItemRequest updateCartItemRequest) {

        return ResponseEntity.ok(cartService.updateCartItem(id, productId, updateCartItemRequest.getQuantity()));
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable(name = "cartId") UUID id,
            @PathVariable(name = "productId") Long productId) {
        cartService.deleteProduct(id, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<Void> clearCart(@PathVariable(name = "cartId") UUID id) {
        cartService.clearCart(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCartNotFound(){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error:", "cart not found"));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFound(){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error:", "product not found"));
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCartItemNotFound(){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error:", "product not found"));
    }
}

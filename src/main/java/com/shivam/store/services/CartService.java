package com.shivam.store.services;

import com.shivam.store.dtos.CartDto;
import com.shivam.store.dtos.CartItemDto;
import com.shivam.store.entities.Cart;
import com.shivam.store.exceptions.CartItemNotFoundException;
import com.shivam.store.exceptions.CartNotFoundException;
import com.shivam.store.exceptions.ProductNotFoundException;
import com.shivam.store.mappers.CartMapper;
import com.shivam.store.repositories.CartRepository;
import com.shivam.store.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class CartService {
    private CartRepository cartRepository;
    private ProductRepository productRepository;
    private CartMapper cartMapper;


    public CartDto createCart() {
        var cart = new Cart();
        cartRepository.save(cart);
        return cartMapper.toDto(cart);
    }

    public CartItemDto addProductToCart(UUID id, Long productId) {
        var cart = cartRepository.findById(id).orElse(null);
        var product = productRepository.findById(productId).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException();
        }
        if (product == null) {
            throw new ProductNotFoundException();
        }
        var cartItem = cart.addItem(product);
        cartRepository.save(cart);
        return cartMapper.toCartItemDto(cartItem);
    }

    public CartDto getCart(UUID id) {
        var cart = cartRepository.findById(id).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException();
        }
        return cartMapper.toDto(cart);
    }

    public CartItemDto updateCartItem(UUID id, Long productId, Integer quantity) {
        var cart = cartRepository.findById(id).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException();
        }

        var cartItem = cart.getItem(productId);
        if (cartItem == null) {
            throw new CartItemNotFoundException();
        }
        cartItem.setQuantity(quantity);
        cartRepository.save(cart);
        return cartMapper.toCartItemDto(cartItem);
    }
    public void deleteProduct(UUID id, Long productId) {
        var cart = cartRepository.findById(id).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException();
        }
        cart.removeItem(productId);
        cartRepository.save(cart);
    }

    public void clearCart(UUID id) {
        var cart = cartRepository.findById(id).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException();
        }
        cart.clear();
        cartRepository.save(cart);
    }
}

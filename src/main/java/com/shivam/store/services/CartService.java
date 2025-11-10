package com.shivam.store.services;

import com.shivam.store.carts.CartOwner;
import com.shivam.store.dtos.CartDto;
import com.shivam.store.dtos.CartItemDto;
import com.shivam.store.entities.Cart;
import com.shivam.store.exceptions.CartItemNotFoundException;
import com.shivam.store.exceptions.CartNotFoundException;
import com.shivam.store.exceptions.IncorrectUserException;
import com.shivam.store.exceptions.ProductNotFoundException;
import com.shivam.store.mappers.CartMapper;
import com.shivam.store.repositories.CartRepository;
import com.shivam.store.repositories.ProductRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    public CartDto createCart(CartOwner owner) {
        var existing = owner.user()
                .flatMap(cartRepository::findByUser)
                .or(() -> owner.guestToken().flatMap(cartRepository::findByGuestToken));

        if (existing.isPresent()) {
            return cartMapper.toDto(existing.get());
        }

        var cart = new Cart();
        owner.user().ifPresent(cart::setUser);
        owner.guestToken().ifPresent(cart::setGuestToken);
        cartRepository.save(cart);
        return cartMapper.toDto(cart);
    }

    public CartItemDto addProductToCart(UUID id, Long productId, CartOwner owner) {
        var cart = requireOwnedCart(id, owner);
        var product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
        var cartItem = cart.addItem(product);
        cartRepository.save(cart);
        return cartMapper.toCartItemDto(cartItem);
    }

    public CartDto getCart(UUID id, CartOwner owner) {
        var cart = requireOwnedCart(id, owner);
        return cartMapper.toDto(cart);
    }

    public CartItemDto updateCartItem(UUID id, Long productId, Integer quantity, CartOwner owner) {
        var cart = requireOwnedCart(id, owner);
        var cartItem = cart.getItem(productId);
        if (cartItem == null) {
            throw new CartItemNotFoundException();
        }
        cartItem.setQuantity(quantity);
        cartRepository.save(cart);
        return cartMapper.toCartItemDto(cartItem);
    }

    public void deleteProduct(UUID id, Long productId, CartOwner owner) {
        var cart = requireOwnedCart(id, owner);
        cart.removeItem(productId);
        cartRepository.save(cart);
    }

    public void clearCart(UUID id, CartOwner owner) {
        var cart = requireOwnedCart(id, owner);
        cart.clear();
        cartRepository.save(cart);
    }

    public void clearCartAsOwner(UUID id, CartOwner owner) {
        clearCart(id, owner);
    }

    private Cart requireOwnedCart(UUID id, CartOwner owner) {
        var cart = cartRepository.findById(id).orElseThrow(CartNotFoundException::new);
        if (owner.hasUser()) {
            var userId = owner.user().map(u -> u.getId()).orElseThrow(IncorrectUserException::new);
            if (cart.getUser() == null || !cart.getUser().getId().equals(userId)) {
                throw new IncorrectUserException();
            }
            return cart;
        }

        if (owner.hasGuestToken()) {
            var token = owner.guestToken().orElseThrow(IncorrectUserException::new);
            if (cart.getGuestToken() == null || !cart.getGuestToken().equals(token)) {
                throw new IncorrectUserException();
            }
            return cart;
        }

        throw new IncorrectUserException();
    }
}

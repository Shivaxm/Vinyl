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
                .flatMap(cartRepository::findFirstByUserOrderByDateCreatedDesc);

        if (existing.isEmpty() && owner.hasGuestToken()) {
            existing = owner.guestToken().flatMap(cartRepository::findByGuestToken)
                    .map(cart -> attachUserIfPresent(cart, owner));
        }

        if (existing.isEmpty()) {
            var cart = new Cart();
            owner.user().ifPresent(cart::setUser);
            owner.guestToken().ifPresent(cart::setGuestToken);
            cartRepository.save(cart);
            return cartMapper.toDto(cart);
        }

        var resolved = attachUserIfPresent(existing.get(), owner);
        return cartMapper.toDto(resolved);
    }

    public CartItemDto addProductToCurrentCart(Long productId, CartOwner owner) {
        var cart = requireCurrentCart(owner);
        var product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
        var cartItem = cart.addItem(product);
        cartRepository.save(cart);
        return cartMapper.toCartItemDto(cartItem);
    }

    public CartDto getCurrentCart(CartOwner owner) {
        var cart = requireCurrentCart(owner);
        return cartMapper.toDto(cart);
    }

    public Cart getCurrentCartEntity(CartOwner owner) {
        return requireCurrentCart(owner);
    }

    public CartItemDto updateCurrentCartItem(Long productId, Integer quantity, CartOwner owner) {
        var cart = requireCurrentCart(owner);
        var cartItem = cart.getItem(productId);
        if (cartItem == null) {
            throw new CartItemNotFoundException();
        }
        cartItem.setQuantity(quantity);
        cartRepository.save(cart);
        return cartMapper.toCartItemDto(cartItem);
    }

    public void deleteProductFromCurrentCart(Long productId, CartOwner owner) {
        var cart = requireCurrentCart(owner);
        cart.removeItem(productId);
        cartRepository.save(cart);
    }

    public void clearCurrentCart(CartOwner owner) {
        var cart = requireCurrentCart(owner);
        cart.clear();
        cartRepository.save(cart);
    }

    private Cart requireOwnedCart(UUID id, CartOwner owner) {
        var cart = cartRepository.findById(id).orElseThrow(CartNotFoundException::new);
        
        if (owner.hasUser()) {
            var userId = owner.user().map(u -> u.getId()).orElseThrow(IncorrectUserException::new);
            if (cart.getUser() == null && matchesGuestToken(cart, owner)) {
                cart.setUser(owner.user().get());
                cart.setGuestToken(null);
                cartRepository.save(cart);
            }
            if (cart.getUser() == null || !cart.getUser().getId().equals(userId)) {
                throw new IncorrectUserException();
            }
            return cart;
        }

        if (owner.hasGuestToken()) {
            var token = owner.guestToken().orElseThrow(IncorrectUserException::new);
            if (!matchesGuestToken(cart, owner)) {
                throw new IncorrectUserException();
            }
            return cart;
        }
        throw new IncorrectUserException();
    }

    private Cart requireCurrentCart(CartOwner owner) {
        var cartDto = createCart(owner);
        return requireOwnedCart(cartDto.getId(), owner);
    }

    private boolean matchesGuestToken(Cart cart, CartOwner owner) {
        var token = owner.guestToken().orElse(null);
        return token != null && cart.getGuestToken() != null && cart.getGuestToken().equals(token);
    }

    private Cart attachUserIfPresent(Cart cart, CartOwner owner) {
        if (owner.hasUser() && cart.getUser() == null) {
            cart.setUser(owner.user().get());
            cart.setGuestToken(null);
            cartRepository.save(cart);
        }
        return cart;
    }
}

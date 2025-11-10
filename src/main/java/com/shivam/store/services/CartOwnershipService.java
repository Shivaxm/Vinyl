package com.shivam.store.services;

import com.shivam.store.entities.Cart;
import com.shivam.store.entities.User;
import com.shivam.store.repositories.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartOwnershipService {
    private final CartRepository cartRepository;

    public void promoteToUser(String guestToken, User user) {
        if (user == null || guestToken == null || guestToken.isBlank()) {
            return;
        }

        var guestCartOpt = cartRepository.findByGuestToken(guestToken);
        if (guestCartOpt.isEmpty()) {
            return;
        }

        var guestCart = guestCartOpt.get();
        var userCartOpt = cartRepository.findByUser(user);

        if (userCartOpt.isEmpty()) {
            guestCart.setUser(user);
            guestCart.setGuestToken(null);
            cartRepository.save(guestCart);
            return;
        }

        var userCart = userCartOpt.get();
        guestCart.getCartItems().forEach(guestItem -> {
            var existing = userCart.getItem(guestItem.getProduct().getId());
            if (existing == null) {
                var newItem = userCart.addItem(guestItem.getProduct());
                newItem.setQuantity(guestItem.getQuantity());
            } else {
                existing.setQuantity(existing.getQuantity() + guestItem.getQuantity());
            }
        });

        cartRepository.save(userCart);
        cartRepository.delete(guestCart);
    }
}

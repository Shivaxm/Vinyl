package com.shivam.store.carts;

import com.shivam.store.entities.User;
import java.util.Objects;
import java.util.Optional;


public record CartOwner(Optional<User> user, Optional<String> guestToken) {

    public CartOwner {
        user = user != null ? user : Optional.empty();
        guestToken = guestToken != null ? guestToken : Optional.empty();
    }

    public static CartOwner authenticated(User user) {
        return new CartOwner(Optional.of(Objects.requireNonNull(user)), Optional.empty());
    }

    public static CartOwner guest(String token) {
        return new CartOwner(Optional.empty(), Optional.of(Objects.requireNonNull(token)));
    }

    public boolean hasUser() {
        return user.isPresent();
    }

    public boolean hasGuestToken() {
        return guestToken.filter(token -> !token.isBlank()).isPresent();
    }
}

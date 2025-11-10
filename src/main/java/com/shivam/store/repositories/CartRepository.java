package com.shivam.store.repositories;

import com.shivam.store.entities.Cart;
import com.shivam.store.entities.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByUser(User user);

    Optional<Cart> findByGuestToken(String guestToken);
}

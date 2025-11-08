package com.shivam.store.repositories;

import com.shivam.store.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    
}

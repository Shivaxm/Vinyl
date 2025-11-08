package com.shivam.store.repositories;

import com.shivam.store.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface OrderItemRepository extends JpaRepository<OrderItem, BigInteger> {
}

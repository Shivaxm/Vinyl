package com.shivam.store.repositories;

import com.shivam.store.entities.Order;
import com.shivam.store.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, BigInteger> {

    @EntityGraph(attributePaths = "items.product")
    @Query("SELECT o from Order o WHERE o.customer = :customer")
    List<Order> getAllByCustomer(@Param("customer") User customer);

    @Query("SELECT o from Order o where o.id = :orderId")
    Optional<Order> getOrderWithItems(@Param("orderId") BigInteger orderId);
}

package com.shivam.store.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;


    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;


    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "order", cascade =
            {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Set<OrderItem> items = new LinkedHashSet<>();

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public BigDecimal getTotalPrice() {
        return this.items.stream().map(OrderItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static Order fromCart(Cart cart, User user) {
        var order = new Order();
        order.setCustomer(user);
        order.setStatus(OrderStatus.PENDING);
        var items = cart.getCartItems();
        for (var item : items) {
            var orderItem = new OrderItem(order, item.getProduct(), item.getQuantity(), item.getTotalPrice(),item.getProduct().getPrice());
            order.addItem(orderItem);
        }

        order.setTotalPrice(order.getTotalPrice());
        return order;

    }

    public boolean isPlacedBy(User user) {
        return this.customer.equals(user);
    }
}
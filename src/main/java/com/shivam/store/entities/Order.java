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

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade =
            {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Set<OrderItem> items = new LinkedHashSet<>();

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        recalculateTotal();
    }

    public void removeItem(OrderItem item) {
        if (items.remove(item)) {
            item.setOrder(null);
            recalculateTotal();
        }
    }

    @PrePersist
    @PreUpdate
    private void syncTotalPrice() {
        recalculateTotal();
    }

    private void recalculateTotal() {
        this.totalPrice = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static Order fromCart(Cart cart, User user) {
        var order = new Order();
        order.setCustomer(user);
        order.setStatus(OrderStatus.PENDING);
        for (var item : cart.getCartItems()) {
            var orderItem = new OrderItem(order, item.getProduct(), item.getQuantity(), item.getTotalPrice(),item.getProduct().getPrice());
            order.addItem(orderItem);
        }
        return order;

    }

    public boolean isPlacedBy(User user) {
        return this.customer.equals(user);
    }
}

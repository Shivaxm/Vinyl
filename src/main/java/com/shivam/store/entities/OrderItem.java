package com.shivam.store.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "order_items")
@NoArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;


    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;


    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;


    @Column(name = "quantity")
    private Integer quantity;


    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    public OrderItem(Order order, Product product, Integer quantity, BigDecimal totalPrice, BigDecimal price) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.unitPrice = price;
    }
}
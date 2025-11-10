package com.shivam.store.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import com.shivam.store.entities.CartItem;
import com.shivam.store.entities.Product;

@Getter
@Setter
@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;


    @Column(name = "date_created", insertable = false, updatable = false)
    private LocalDate dateCreated;

    @OneToMany(mappedBy = "cart", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Set<CartItem> cartItems = new LinkedHashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "guest_token")
    private String guestToken;


    public BigDecimal getTotalPrice() {
        return cartItems.stream().map(CartItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public CartItem getItem(Long productId) {
        return cartItems.stream().filter(cartItem -> cartItem.getProduct().getId().equals(productId)).findFirst().orElse(null);
    }

    public CartItem addItem(Product product) {
        var cartItem = this.getItem(product.getId());
        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(1);
            cartItem.setCart(this);
            cartItems.add(cartItem);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        }

        return cartItem;
    }

    public void removeItem(Long productId) {
        var item = getItem(productId);
        if (item == null) {
            return;
        }
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            return;
        }
        cartItems.remove(item);
        item.setCart(null);
    }
    public void clear() {
        cartItems.clear();
    }
}

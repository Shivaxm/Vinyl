package com.shivam.store.payments;

import com.shivam.store.carts.CartOwner;
import com.shivam.store.entities.Cart;
import com.shivam.store.entities.Order;
import com.shivam.store.entities.OrderItem;
import com.shivam.store.entities.OrderStatus;
import com.shivam.store.exceptions.CartNotFoundException;
import com.shivam.store.repositories.*;
import com.shivam.store.services.AuthService;
import com.shivam.store.services.CartService;
import java.util.LinkedHashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckoutService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final PaymentGateway paymentGateway;
    private final AuthService authService;

    @Transactional
    public OrderIdDto createOrder() {
        var user = authService.getUser();
        var cart = cartService.getCurrentCartEntity(CartOwner.authenticated(user));
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new CartNotFoundException();
        }

        var pendingOrder = orderRepository.findFirstByCustomerAndStatusOrderByCreatedAtDesc(user, OrderStatus.PENDING);
        var order = pendingOrder
                .map(existing -> refreshOrderFromCart(existing, cart))
                .orElseGet(() -> Order.fromCart(cart, user));
        var createdNewOrder = pendingOrder.isEmpty();
        orderRepository.save(order);
        try{
            var session = paymentGateway.createCheckoutSession(order);
            return new OrderIdDto(order.getId(), session.getCheckoutUrl());

        } catch (PaymentException e) {
            if (createdNewOrder) {
                orderRepository.delete(order);
            }
            throw e;
        }
    }

    public void handleWebhookEvent(WebhookRequest webhookRequest) {
       paymentGateway.processPayment(webhookRequest).ifPresent(paymentResult ->{
           var order = orderRepository.findById(paymentResult.getOrderId()).orElseThrow();
           var previousStatus = order.getStatus();
           order.setStatus(paymentResult.getPaymentStatus());
           orderRepository.save(order);
           if (paymentResult.getPaymentStatus() == OrderStatus.PAID && previousStatus != OrderStatus.PAID) {
               cartService.clearCurrentCart(CartOwner.authenticated(order.getCustomer()));
           }
       });

    }

    private Order refreshOrderFromCart(Order order, Cart cart) {
        for (var item : new LinkedHashSet<>(order.getItems())) {
            order.removeItem(item);
        }

        for (var cartItem : cart.getCartItems()) {
            var orderItem = new OrderItem(
                    order,
                    cartItem.getProduct(),
                    cartItem.getQuantity(),
                    cartItem.getTotalPrice(),
                    cartItem.getProduct().getPrice()
            );
            order.addItem(orderItem);
        }

        return order;
    }
}

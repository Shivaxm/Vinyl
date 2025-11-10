package com.shivam.store.payments;

import com.shivam.store.carts.CartOwner;
import com.shivam.store.entities.Order;
import com.shivam.store.exceptions.CartNotFoundException;
import com.shivam.store.repositories.*;
import com.shivam.store.services.AuthService;
import com.shivam.store.services.CartService;
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

        var order = Order.fromCart(cart, user);
        orderRepository.save(order);
        try{
            var session = paymentGateway.createCheckoutSession(order);
            cartService.clearCurrentCart(CartOwner.authenticated(user));
            return new OrderIdDto(order.getId(), session.getCheckoutUrl());

        } catch (PaymentException e) {

            orderRepository.delete(order);
            throw e;

        }

    }

    public void handleWebhookEvent(WebhookRequest webhookRequest) {
       paymentGateway.processPayment(webhookRequest).ifPresent(paymentResult ->{
           var order = orderRepository.findById(paymentResult.getOrderId()).orElseThrow();
           order.setStatus(paymentResult.getPaymentStatus());
           orderRepository.save(order);
       });

    }
}

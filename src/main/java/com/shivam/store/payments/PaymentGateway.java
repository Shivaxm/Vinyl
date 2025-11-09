package com.shivam.store.payments;

import com.shivam.store.entities.Order;

import java.util.Optional;

public interface PaymentGateway {
    CheckoutSession createCheckoutSession(Order order);

    Optional<PaymentResult> processPayment(WebhookRequest webhookRequest);

}

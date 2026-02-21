package com.shivam.store.payments;

import com.shivam.store.entities.Order;
import com.shivam.store.entities.OrderItem;
import com.shivam.store.entities.OrderStatus;
import com.stripe.exception.StripeException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

@Service
public class StripePaymentGateway implements PaymentGateway {
    @Value("${store.website-url}")
    private String webUrl;
    @Value("${stripe.webhookSecretKey}")
    private String webhookSecretKey;
    public CheckoutSession createCheckoutSession(Order order) {
        try{
            var orderId = order.getId().toString();
            var builder = SessionCreateParams.builder().setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(webUrl + "/checkout-success?orderId=" + order.getId())
                    .setCancelUrl(webUrl + "/checkout-cancel?orderId=" + order.getId())
                    .setClientReferenceId(orderId)
                    .putMetadata("order_id", orderId)
                    .setPaymentIntentData(
                            SessionCreateParams.PaymentIntentData.builder()
                                    .putMetadata("order_id", orderId)
                                    .build()
                    );


            order.getItems().forEach(orderItem -> {
                var lineItem = createLineItem(orderItem);
                builder.addLineItem(lineItem);});

            var session = Session.create(builder.build());

            return new  CheckoutSession(session.getUrl());

        } catch (StripeException e) {
            throw new PaymentException();
        }
}

    @Override
    public Optional<PaymentResult> processPayment(WebhookRequest webhookRequest) {
        var payload = webhookRequest.getPayload();
        var signature = webhookRequest.getStripeSignature();
        try {
            var event = Webhook.constructEvent(payload, signature, webhookSecretKey);

            var orderId = extractOrderId(event).orElse(null);
            if (orderId == null) return Optional.empty();
            switch (event.getType()) {
                case "checkout.session.completed" -> {
                    // For typical card payments, Checkout Session completion implies paid.
                    return Optional.of(new PaymentResult(orderId, OrderStatus.PAID));
                }
                case "payment_intent.succeeded" -> {
                    return Optional.of(new PaymentResult(orderId, OrderStatus.PAID));
                }
                case "payment_intent.payment_failed" -> {
                    return Optional.of(new PaymentResult(orderId, OrderStatus.FAILED));
                }
                    default -> {
                        return Optional.empty();
                    }



            }
        } catch (SignatureVerificationException e) {
            throw new WebhookSignatureException("Invalid Stripe webhook signature");
        }
    }

    private Optional<BigInteger> extractOrderId(Event event) {
        var stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
        if (stripeObject == null) return Optional.empty();

        if (stripeObject instanceof Session session) {
            var orderId = session.getClientReferenceId();
            if (orderId == null && session.getMetadata() != null) {
                orderId = session.getMetadata().get("order_id");
            }
            return orderId == null ? Optional.empty() : Optional.of(new BigInteger(orderId));
        }

        if (stripeObject instanceof PaymentIntent paymentIntent) {
            var metadata = paymentIntent.getMetadata();
            var orderId = metadata == null ? null : metadata.get("order_id");
            return orderId == null ? Optional.empty() : Optional.of(new BigInteger(orderId));
        }

        return Optional.empty();
    }
    private SessionCreateParams.LineItem createLineItem(OrderItem orderItem) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity(Long.valueOf(orderItem.getQuantity()))
                .setPriceData(createPriceData(orderItem)).build();

    }

    private SessionCreateParams.LineItem.PriceData createPriceData(OrderItem orderItem) {
        return SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("usd")
                .setUnitAmountDecimal(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(100)))
                .setProductData(createProductData(orderItem)).build();
    }

    private SessionCreateParams.LineItem.PriceData.ProductData createProductData(OrderItem orderItem) {
        return SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(orderItem.getProduct().getName())
                .build();
    }
}

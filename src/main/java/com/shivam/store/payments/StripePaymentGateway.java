package com.shivam.store.payments;

import com.shivam.store.entities.Order;
import com.shivam.store.entities.OrderItem;
import com.shivam.store.entities.OrderStatus;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
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
    @Value("${websiteUrl}")
    private String webUrl;
    @Value("${stripe.webhookSecretKey}")
    private String webhookSecretKey;
    public CheckoutSession createCheckoutSession(Order order) {
        try{
            var builder = SessionCreateParams.builder().setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(webUrl + "/checkout-success?orderId=" + order.getId())
                    .setCancelUrl(webUrl + "/checkout-cancel")
                    .putMetadata("order_id", order.getId().toString());


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
        var signature = webhookRequest.getHeaders().get("stripe-signature");
        try {
            var event = Webhook.constructEvent(payload, signature, webhookSecretKey);

            var orderId = extractOrderId(event);
            switch (event.getType()) {
                case "payment_intent.succeeded" -> {
                    //Update order status PAID
                        return Optional.of(new PaymentResult(orderId, OrderStatus.PAID));
                }
                case "payment_intent.failed" -> {
                        return Optional.of(new PaymentResult(orderId, OrderStatus.FAILED));
                    }
                    default -> {
                        return Optional.empty();
                    }



            }
        } catch (SignatureVerificationException e) {
            throw new PaymentException("invalid signature");
        }
    }

    private BigInteger extractOrderId(Event event) {
        var stripeObject = event.getDataObjectDeserializer().getObject().orElseThrow(
                () -> new PaymentException("Could not deserialize Stripe event")
        );
        var paymentIntent = (PaymentIntent) stripeObject;

        return new BigInteger(paymentIntent.getMetadata().get("order_id"));

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

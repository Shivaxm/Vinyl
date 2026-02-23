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
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

@Service
public class StripePaymentGateway implements PaymentGateway {
    private static final Logger log = LoggerFactory.getLogger(StripePaymentGateway.class);

    @Value("${store.website-url}")
    private String webUrl;
    @Value("${stripe.webhookSecretKey}")
    private String webhookSecretKey;
    public CheckoutSession createCheckoutSession(Order order) {
        try{
            var baseUrl = resolveWebsiteUrl();
            var orderId = order.getId().toString();
            var builder = SessionCreateParams.builder().setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(baseUrl + "/checkout-success?orderId=" + order.getId())
                    .setCancelUrl(baseUrl + "/checkout-cancel?orderId=" + order.getId())
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
            // OWASP A09: security-relevant payment failures are logged without secrets.
            log.error("security_event=stripe_checkout_session_failed type={}", e.getClass().getSimpleName());
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
            // OWASP A04/A08/A09: webhook signature failures are tracked and rejected.
            log.warn("security_event=stripe_webhook_signature_verification_failed");
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
            if (orderId == null) {
                return Optional.empty();
            }
            try {
                return Optional.of(new BigInteger(orderId));
            } catch (NumberFormatException ex) {
                log.warn("security_event=stripe_webhook_invalid_order_id source=session");
                return Optional.empty();
            }
        }

        if (stripeObject instanceof PaymentIntent paymentIntent) {
            var metadata = paymentIntent.getMetadata();
            var orderId = metadata == null ? null : metadata.get("order_id");
            if (orderId == null) {
                return Optional.empty();
            }
            try {
                return Optional.of(new BigInteger(orderId));
            } catch (NumberFormatException ex) {
                log.warn("security_event=stripe_webhook_invalid_order_id source=payment_intent");
                return Optional.empty();
            }
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

    private String resolveWebsiteUrl() {
        if (webUrl != null && !webUrl.isBlank()) {
            return stripTrailingSlash(webUrl);
        }

        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            String forwardedHost = request.getHeader("X-Forwarded-Host");
            String forwardedProto = request.getHeader("X-Forwarded-Proto");

            if (forwardedHost != null && !forwardedHost.isBlank()) {
                String scheme = (forwardedProto == null || forwardedProto.isBlank())
                        ? request.getScheme()
                        : forwardedProto;
                return stripTrailingSlash(scheme + "://" + forwardedHost);
            }

            String scheme = request.getScheme();
            String host = request.getServerName();
            int port = request.getServerPort();
            boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                    || ("https".equalsIgnoreCase(scheme) && port == 443);
            String origin = defaultPort ? scheme + "://" + host : scheme + "://" + host + ":" + port;
            return stripTrailingSlash(origin);
        }

        throw new PaymentException("Unable to resolve website URL for Stripe redirects");
    }

    private String stripTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}

package com.shivam.store.payments;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping()
    @PreAuthorize("isAuthenticated()")
    public OrderIdDto checkout() {
        return checkoutService.createOrder();
    }
    @PostMapping("/webhook")
    public void handleWebhook(
            @RequestHeader("Stripe-Signature") String stripeSignature,
            @RequestBody String payload
    ) {
        checkoutService.handleWebhookEvent(new WebhookRequest(stripeSignature, payload));
    }
}

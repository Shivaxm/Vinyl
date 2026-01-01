package com.shivam.store.payments;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class WebhookRequest {
    private String stripeSignature;
    private String payload;
}

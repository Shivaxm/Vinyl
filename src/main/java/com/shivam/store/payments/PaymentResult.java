package com.shivam.store.payments;

import com.shivam.store.entities.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigInteger;

@AllArgsConstructor
@Getter
public class PaymentResult {
    private BigInteger orderId;
    private OrderStatus paymentStatus;
}

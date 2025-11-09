package com.shivam.store.payments;

import lombok.Data;

@Data
public class OrderIdDto {
    private Long orderId;
    private String checkoutUrl;

    public OrderIdDto(Long id, String url) {
        this.orderId = id;
        this.checkoutUrl = url;
    }
}

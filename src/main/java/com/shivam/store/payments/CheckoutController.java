package com.shivam.store.payments;

import com.shivam.store.dtos.ErrorDto;
import com.shivam.store.exceptions.CartNotFoundException;
import com.shivam.store.repositories.OrderRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    @PostMapping()
    public OrderIdDto checkout(@Valid @RequestBody CartRequest cartId) {
        return checkoutService.createOrder(cartId.getCartId());
    }
    @PostMapping("/webhook")
    public void handleWebhook(
            @RequestHeader Map<String, String> headers, @RequestBody String payload){
        checkoutService.handleWebhookEvent(new WebhookRequest(headers, payload));
    }


    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCartNotFound(Exception e) {
        return ResponseEntity.badRequest().body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorDto> handlePaymentException() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto("Error creating a checkout session"));
    }

}

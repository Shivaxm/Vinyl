package com.shivam.store.payments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shivam.store.carts.CartOwner;
import com.shivam.store.entities.Cart;
import com.shivam.store.entities.CartItem;
import com.shivam.store.entities.Order;
import com.shivam.store.entities.OrderStatus;
import com.shivam.store.entities.Product;
import com.shivam.store.entities.User;
import com.shivam.store.exceptions.CartNotFoundException;
import com.shivam.store.payments.PaymentException;
import com.shivam.store.repositories.OrderRepository;
import com.shivam.store.services.AuthService;
import com.shivam.store.services.CartService;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartService cartService;
    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private AuthService authService;

    @InjectMocks
    private CheckoutService checkoutService;

    private User user;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        cart = new Cart();
        cart.setCartItems(new LinkedHashSet<>());
        CartItem item = new CartItem();
        Product product = new Product();
        product.setId(2L);
        product.setPrice(BigDecimal.valueOf(5));
        item.setProduct(product);
        item.setQuantity(2);
        item.setCart(cart);
        cart.getCartItems().add(item);
    }

    @Test
    void createOrder_buildsOrderAndDoesNotClearCartBeforePaymentConfirmation() {
        when(authService.getUser()).thenReturn(user);
        when(cartService.getCurrentCartEntity(any())).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(123L);
            return order;
        });
        CheckoutSession session = new CheckoutSession("https://checkout");
        when(paymentGateway.createCheckoutSession(any())).thenReturn(session);

        var result = checkoutService.createOrder();

        assertThat(result.getOrderId()).isEqualTo(123L);
        assertThat(result.getCheckoutUrl()).isEqualTo("https://checkout");

        verify(cartService, never()).clearCurrentCart(any());
    }

    @Test
    void createOrder_whenCartEmpty_throwsCartNotFound() {
        var emptyCart = new Cart();
        emptyCart.setCartItems(new LinkedHashSet<>());

        when(authService.getUser()).thenReturn(user);
        when(cartService.getCurrentCartEntity(any())).thenReturn(emptyCart);

        assertThatThrownBy(() -> checkoutService.createOrder())
                .isInstanceOf(CartNotFoundException.class);

        verify(orderRepository, never()).save(any());
        verify(cartService, never()).clearCurrentCart(any());
    }

    @Test
    void createOrder_whenPaymentFails_rollsBackOrderAndPropagates() {
        when(authService.getUser()).thenReturn(user);
        when(cartService.getCurrentCartEntity(any())).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(55L);
            return order;
        });
        when(paymentGateway.createCheckoutSession(any())).thenThrow(new PaymentException("gateway down"));

        assertThatThrownBy(() -> checkoutService.createOrder())
                .isInstanceOf(PaymentException.class);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        verify(orderRepository).delete(captor.getValue());
        verify(cartService, never()).clearCurrentCart(any());
    }

    @Test
    void handleWebhookEvent_whenPaid_clearsCurrentCart() {
        var order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setCustomer(user);

        when(paymentGateway.processPayment(any()))
                .thenReturn(Optional.of(new PaymentResult(BigInteger.valueOf(10L), OrderStatus.PAID)));
        when(orderRepository.findById(BigInteger.valueOf(10L))).thenReturn(Optional.of(order));

        checkoutService.handleWebhookEvent(new WebhookRequest("sig", "payload"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository).save(order);
        ArgumentCaptor<CartOwner> captor = ArgumentCaptor.forClass(CartOwner.class);
        verify(cartService).clearCurrentCart(captor.capture());
        assertThat(captor.getValue().hasUser()).isTrue();
        assertThat(captor.getValue().user().get()).isEqualTo(user);
    }

    @Test
    void handleWebhookEvent_whenNotPaid_doesNotClearCurrentCart() {
        var order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setCustomer(user);

        when(paymentGateway.processPayment(any()))
                .thenReturn(Optional.of(new PaymentResult(BigInteger.valueOf(11L), OrderStatus.FAILED)));
        when(orderRepository.findById(BigInteger.valueOf(11L))).thenReturn(Optional.of(order));

        checkoutService.handleWebhookEvent(new WebhookRequest("sig", "payload"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
        verify(orderRepository).save(order);
        verify(cartService, never()).clearCurrentCart(any());
    }
}

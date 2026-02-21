package com.shivam.store.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shivam.store.entities.Order;
import com.shivam.store.entities.OrderStatus;
import com.shivam.store.entities.User;
import com.shivam.store.exceptions.IncorrectUserException;
import com.shivam.store.mappers.OrderMapper;
import com.shivam.store.repositories.OrderRepository;
import java.math.BigInteger;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private AuthService authService;

    @InjectMocks
    private OrderService orderService;

    private User owner;
    private User otherUser;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);

        otherUser = new User();
        otherUser.setId(2L);
    }

    @Test
    void cancelPendingOrder_deletesPendingOrderForOwner() {
        var order = new Order();
        order.setCustomer(owner);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(order));
        when(authService.getUser()).thenReturn(owner);

        orderService.cancelPendingOrder(BigInteger.ONE);

        verify(orderRepository).delete(order);
    }

    @Test
    void cancelPendingOrder_whenOrderMissing_isNoOp() {
        when(orderRepository.findById(BigInteger.TWO)).thenReturn(Optional.empty());

        orderService.cancelPendingOrder(BigInteger.TWO);

        verify(orderRepository, never()).delete(org.mockito.ArgumentMatchers.any(Order.class));
    }

    @Test
    void cancelPendingOrder_whenNotOwner_throwsIncorrectUser() {
        var order = new Order();
        order.setCustomer(owner);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(BigInteger.valueOf(3))).thenReturn(Optional.of(order));
        when(authService.getUser()).thenReturn(otherUser);

        assertThatThrownBy(() -> orderService.cancelPendingOrder(BigInteger.valueOf(3)))
                .isInstanceOf(IncorrectUserException.class);

        verify(orderRepository, never()).delete(order);
    }

    @Test
    void cancelPendingOrder_whenNotPending_doesNotDelete() {
        var order = new Order();
        order.setCustomer(owner);
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findById(BigInteger.valueOf(4))).thenReturn(Optional.of(order));
        when(authService.getUser()).thenReturn(owner);

        orderService.cancelPendingOrder(BigInteger.valueOf(4));

        verify(orderRepository, never()).delete(order);
    }
}

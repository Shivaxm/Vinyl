package com.shivam.store.services;

import com.shivam.store.dtos.OrderDto;
import com.shivam.store.entities.OrderStatus;
import com.shivam.store.entities.User;
import com.shivam.store.exceptions.IncorrectUserException;
import com.shivam.store.exceptions.OrderNotFoundException;
import com.shivam.store.mappers.OrderMapper;
import com.shivam.store.repositories.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final AuthService authService;

    public List<OrderDto> findAll(User customer) {
         return orderRepository.getAllByCustomer(customer).stream().map(orderMapper::toDto).toList();
    }

    public OrderDto getOrder(BigInteger orderId) {
        var order = orderRepository.getOrderWithItems(orderId).orElseThrow(OrderNotFoundException::new);

        var user = authService.getUser();
        if(!order.isPlacedBy(user)) {
            throw new IncorrectUserException();
        }
        return orderMapper.toDto(order);
    }

    public void cancelPendingOrder(BigInteger orderId) {
        var order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return;
        }
        var user = authService.getUser();
        if (!order.isPlacedBy(user)) {
            throw new IncorrectUserException();
        }

        if (order.getStatus() == OrderStatus.PENDING) {
            orderRepository.delete(order);
        }
    }
}

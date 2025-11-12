package com.shivam.store.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shivam.store.carts.CartOwner;
import com.shivam.store.dtos.CartDto;
import com.shivam.store.dtos.CartItemDto;
import com.shivam.store.dtos.CartProductDto;
import com.shivam.store.entities.Cart;
import com.shivam.store.entities.Product;
import com.shivam.store.entities.User;
import com.shivam.store.exceptions.CartNotFoundException;
import com.shivam.store.exceptions.ProductNotFoundException;
import com.shivam.store.mappers.CartMapper;
import com.shivam.store.repositories.CartRepository;
import com.shivam.store.repositories.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Cart cart;
    private UUID cartId;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        cartId = UUID.randomUUID();
        cart = new Cart();
        cart.setId(cartId);
        cart.setUser(user);
    }

    @Test
    void createCart_newUserCartIsPersistedAndMapped() {
        var owner = CartOwner.authenticated(user);
        when(cartRepository.findFirstByUserOrderByDateCreatedDesc(user)).thenReturn(Optional.empty());

        var expectedDto = new CartDto();
        expectedDto.setId(cartId);
        when(cartMapper.toDto(any(Cart.class))).thenAnswer(invocation -> {
            Cart saved = invocation.getArgument(0);
            saved.setId(cartId);
            return expectedDto;
        });

        var result = cartService.createCart(owner);

        assertThat(result).isEqualTo(expectedDto);
        var captor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    void addProductToCurrentCart_addsItemAndReturnsDto() {
        var owner = CartOwner.authenticated(user);
        var product = new Product();
        product.setId(10L);
        product.setPrice(BigDecimal.TEN);

        CartDto cartDto = new CartDto();
        cartDto.setId(cartId);

        when(cartRepository.findFirstByUserOrderByDateCreatedDesc(user)).thenReturn(Optional.of(cart));
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartMapper.toDto(cart)).thenReturn(cartDto);

        CartItemDto responseDto = new CartItemDto();
        CartProductDto responseProduct = new CartProductDto();
        responseProduct.setId(product.getId());
        responseDto.setProduct(responseProduct);
        when(cartMapper.toCartItemDto(any())).thenReturn(responseDto);

        CartItemDto result = cartService.addProductToCurrentCart(product.getId(), owner);

        assertThat(result.getProduct().getId()).isEqualTo(product.getId());
        verify(cartRepository).save(cart);
    }

    @Test
    void addProductToCurrentCart_whenCartMissing_throwsCartNotFound() {
        var owner = CartOwner.authenticated(user);
        CartDto cartDto = new CartDto();
        cartDto.setId(cartId);

        when(cartRepository.findFirstByUserOrderByDateCreatedDesc(user)).thenReturn(Optional.of(cart));
        when(cartMapper.toDto(cart)).thenReturn(cartDto);
        when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addProductToCurrentCart(1L, owner))
                .isInstanceOf(CartNotFoundException.class);

        verify(productRepository, never()).findById(any());
    }

    @Test
    void addProductToCurrentCart_whenProductMissing_throwsProductNotFound() {
        var owner = CartOwner.authenticated(user);
        CartDto cartDto = new CartDto();
        cartDto.setId(cartId);

        when(cartRepository.findFirstByUserOrderByDateCreatedDesc(user)).thenReturn(Optional.of(cart));
        when(cartMapper.toDto(cart)).thenReturn(cartDto);
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addProductToCurrentCart(99L, owner))
                .isInstanceOf(ProductNotFoundException.class);

        verify(cartRepository, never()).save(any());
    }

    @Test
    void clearCurrentCart_removesItemsAndSaves() {
        var owner = CartOwner.authenticated(user);
        var product = new Product();
        product.setId(5L);
        product.setPrice(BigDecimal.ONE);
        cart.addItem(product);

        CartDto cartDto = new CartDto();
        cartDto.setId(cartId);

        when(cartRepository.findFirstByUserOrderByDateCreatedDesc(user)).thenReturn(Optional.of(cart));
        when(cartMapper.toDto(cart)).thenReturn(cartDto);
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        cartService.clearCurrentCart(owner);

        assertThat(cart.getCartItems()).isEmpty();
        verify(cartRepository).save(cart);
    }
}

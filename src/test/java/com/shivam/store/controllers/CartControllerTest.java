package com.shivam.store.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivam.store.carts.CartOwner;
import com.shivam.store.dtos.CartDto;
import com.shivam.store.dtos.CartItemDto;
import com.shivam.store.dtos.CartItemRequestDto;
import com.shivam.store.dtos.CartProductDto;
import com.shivam.store.entities.User;
import com.shivam.store.services.CartService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        var controller = new CartController(cartService);
        authenticatedUser = new User();
        authenticatedUser.setId(42L);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new FixedCartOwnerArgumentResolver(authenticatedUser))
                .build();
    }

    @Test
    void createCart_returnsCreatedCart() throws Exception {
        var cartDto = new CartDto();
        cartDto.setId(UUID.randomUUID());
        when(cartService.createCart(any(CartOwner.class))).thenReturn(cartDto);

        mockMvc.perform(post("/carts"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/carts/" + cartDto.getId())))
                .andExpect(jsonPath("$.id").value(cartDto.getId().toString()));

        var ownerCaptor = ArgumentCaptor.forClass(CartOwner.class);
        verify(cartService).createCart(ownerCaptor.capture());
        ownerCaptor.getValue().user().ifPresent(user -> {
            if (!user.getId().equals(authenticatedUser.getId())) {
                throw new AssertionError("CartOwner user mismatch");
            }
        });
    }

    @Test
    void addProduct_returnsCartItem() throws Exception {
        var request = new CartItemRequestDto();
        request.setId(7L);

        var productDto = new CartProductDto();
        productDto.setId(request.getId());

        var cartItemDto = new CartItemDto();
        cartItemDto.setProduct(productDto);
        cartItemDto.setQuantity(2);

        when(cartService.addProductToCurrentCart(any(), any())).thenReturn(cartItemDto);

        mockMvc.perform(post("/carts/current/items")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.id").value(request.getId()))
                .andExpect(jsonPath("$.quantity").value(2));

        verify(cartService).addProductToCurrentCart(any(), any());
    }

    private static class FixedCartOwnerArgumentResolver implements HandlerMethodArgumentResolver {
        private final User user;

        FixedCartOwnerArgumentResolver(User user) {
            this.user = user;
        }

        @Override
        public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
            return CartOwner.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                      ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest,
                                      WebDataBinderFactory binderFactory) {
            return CartOwner.authenticated(user);
        }
    }
}

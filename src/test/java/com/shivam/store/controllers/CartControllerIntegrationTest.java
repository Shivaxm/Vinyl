package com.shivam.store.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivam.store.config.FlywayMigrationConfig;
import com.shivam.store.dtos.CartItemRequestDto;
import com.shivam.store.entities.Cart;
import com.shivam.store.entities.Category;
import com.shivam.store.entities.Product;
import com.shivam.store.entities.User;
import com.shivam.store.repositories.CartRepository;
import com.shivam.store.repositories.CategoryRepository;
import com.shivam.store.repositories.ProductRepository;
import com.shivam.store.repositories.UserRepository;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@ContextConfiguration(classes = FlywayMigrationConfig.class)
@Transactional
class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    void postCarts_authenticatedUser_createsCart() throws Exception {
        var user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("secret");
        user = userRepository.save(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null, java.util.List.of()));

        mockMvc.perform(post("/carts"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists());

        var persisted = cartRepository.findFirstByUserOrderByDateCreatedDesc(user);
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void postCurrentItems_guestToken_resolvesCartAndAddsItem() throws Exception {
        var category = new Category();
        category.setName("Accessories");
        var product = new Product();
        product.setName("Mouse");
        product.setDescription("Wireless");
        product.setPrice(BigDecimal.TEN);
        product.setCategory(category);
        product = productRepository.save(product);

        var cart = new Cart();
        cart.setGuestToken("guest-token-123");
        cart = cartRepository.save(cart);

        SecurityContextHolder.clearContext();

        var payload = new CartItemRequestDto();
        payload.setId(product.getId());

        mockMvc.perform(post("/carts/current/items")
                        .cookie(new Cookie("guestToken", "guest-token-123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.id").value(product.getId()))
                .andExpect(jsonPath("$.quantity").value(1));

        var updated = cartRepository.findById(cart.getId()).orElseThrow();
        assertThat(updated.getCartItems()).hasSize(1);
        assertThat(updated.getCartItems().iterator().next().getProduct().getId()).isEqualTo(product.getId());
    }

}

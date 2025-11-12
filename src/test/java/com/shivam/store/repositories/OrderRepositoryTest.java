package com.shivam.store.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.shivam.store.config.FlywayMigrationConfig;
import com.shivam.store.entities.Category;
import com.shivam.store.entities.Order;
import com.shivam.store.entities.OrderItem;
import com.shivam.store.entities.OrderStatus;
import com.shivam.store.entities.Product;
import com.shivam.store.entities.User;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = FlywayMigrationConfig.class)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User customer;
    private Product product;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setName("Alice");
        customer.setEmail("alice@example.com");
        customer.setPassword("secret");
        customer = userRepository.save(customer);

        var category = new Category();
        category.setName("Books");
        category = categoryRepository.save(category);

        product = new Product();
        product.setName("Spring In Action");
        product.setDescription("Guide");
        product.setPrice(BigDecimal.valueOf(29.99));
        product.setCategory(category);
        product = productRepository.save(product);
    }

    @Test
    void getAllByCustomer_returnsOrdersWithItems() {
        var order = createOrderWithItem(2);
        orderRepository.save(order);

        var results = orderRepository.getAllByCustomer(customer);

        assertThat(results).hasSize(1);
        var fetched = results.get(0);
        assertThat(fetched.getItems()).hasSize(1);
        var item = fetched.getItems().iterator().next();
        assertThat(item.getProduct().getName()).isEqualTo(product.getName());
        assertThat(item.getQuantity()).isEqualTo(2);
    }

    @Test
    void getOrderWithItems_fetchesSingleOrder() {
        var order = createOrderWithItem(1);
        order = orderRepository.save(order);

        var fetched = orderRepository.getOrderWithItems(BigInteger.valueOf(order.getId())).orElseThrow();

        assertThat(fetched.getCustomer().getId()).isEqualTo(customer.getId());
        assertThat(fetched.getItems()).hasSize(1);
        assertThat(fetched.getItems().iterator().next().getProduct().getId()).isEqualTo(product.getId());
    }

    private Order createOrderWithItem(int quantity) {
        var order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        var item = new OrderItem(order, product,
                quantity,
                product.getPrice().multiply(BigDecimal.valueOf(quantity)),
                product.getPrice());
        order.addItem(item);
        return order;
    }
}

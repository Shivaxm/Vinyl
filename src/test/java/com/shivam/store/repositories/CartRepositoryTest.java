package com.shivam.store.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.shivam.store.config.FlywayMigrationConfig;
import com.shivam.store.entities.Cart;
import com.shivam.store.entities.User;
import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = FlywayMigrationConfig.class)
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByGuestToken_returnsMatchingCart() {
        Cart cart = new Cart();
        cart.setGuestToken("guest-123");
        cartRepository.saveAndFlush(cart);

        var result = cartRepository.findByGuestToken("guest-123");

        assertThat(result).isPresent();
        assertThat(result.get().getGuestToken()).isEqualTo("guest-123");
    }

    @Test
    void findFirstByUserOrderByDateCreatedDesc_prefersNewestEntry() {
        var user = new User();
        user.setName("Test User");
        user.setEmail("user@example.com");
        user.setPassword("secret");
        entityManager.persist(user);

        Cart older = new Cart();
        older.setUser(user);
        entityManager.persist(older);

        Cart newer = new Cart();
        newer.setUser(user);
        entityManager.persist(newer);

        entityManager.flush();

        updateDateCreated(older.getId(), LocalDate.now().minusDays(1));
        updateDateCreated(newer.getId(), LocalDate.now());

        var result = cartRepository.findFirstByUserOrderByDateCreatedDesc(user);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(newer.getId());
    }

    private void updateDateCreated(UUID cartId, LocalDate value) {
        entityManager.getEntityManager().createNativeQuery("UPDATE carts SET date_created = ? WHERE id = ?")
                .setParameter(1, Date.valueOf(value))
                .setParameter(2, cartId.toString())
                .executeUpdate();
    }
}

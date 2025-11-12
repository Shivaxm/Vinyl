package com.shivam.store.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.shivam.store.config.FlywayMigrationConfig;
import com.shivam.store.entities.Category;
import com.shivam.store.entities.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = FlywayMigrationConfig.class)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findByCategoryId_returnsMatchingProducts() {
        var electronics = new Category();
        electronics.setName("Electronics");
        electronics = categoryRepository.save(electronics);

        var otherCategory = new Category();
        otherCategory.setName("Other");
        categoryRepository.save(otherCategory);

        productRepository.saveAll(List.of(
                buildProduct("Mouse", electronics),
                buildProduct("Keyboard", electronics),
                buildProduct("Notebook", otherCategory)
        ));

        var products = productRepository.findByCategory_Id(electronics.getId());

        assertThat(products).hasSize(2);
        assertThat(products).extracting(Product::getName)
                .containsExactlyInAnyOrder("Mouse", "Keyboard");
    }

    private Product buildProduct(String name, Category category) {
        var product = new Product();
        product.setName(name);
        product.setDescription(name + " desc");
        product.setPrice(BigDecimal.TEN);
        product.setCategory(category);
        return product;
    }
}

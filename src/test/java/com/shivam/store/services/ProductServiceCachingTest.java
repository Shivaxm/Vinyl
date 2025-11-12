package com.shivam.store.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.shivam.store.config.FlywayMigrationConfig;
import com.shivam.store.entities.Category;
import com.shivam.store.entities.Product;
import com.shivam.store.repositories.CategoryRepository;
import com.shivam.store.repositories.ProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = FlywayMigrationConfig.class)
@Transactional
class ProductServiceCachingTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void cleanState() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        var productCache = cacheManager.getCache(ProductService.PRODUCTS_CACHE);
        if (productCache != null) {
            productCache.clear();
        }
        var productByIdCache = cacheManager.getCache(ProductService.PRODUCT_BY_ID_CACHE);
        if (productByIdCache != null) {
            productByIdCache.clear();
        }
    }

    @Test
    void getProduct_servesCachedValue_afterSourceRemoved() {
        var category = new Category();
        category.setName("Caching");
        category = categoryRepository.save(category);

        var product = new Product();
        product.setName("Cached Mouse");
        product.setDescription("Wireless");
        product.setPrice(BigDecimal.TEN);
        product.setCategory(category);
        product = productRepository.save(product);

        var firstLookup = productService.getProduct(product.getId());

        productRepository.deleteById(product.getId());
        productRepository.flush();

        var cachedLookup = productService.getProduct(product.getId());

        assertThat(cachedLookup.getId()).isEqualTo(firstLookup.getId());
        var cached = cacheManager.getCache(ProductService.PRODUCT_BY_ID_CACHE);
        assertThat(cached).isNotNull();
        assertThat(cached.get(product.getId())).isNotNull();
    }
}

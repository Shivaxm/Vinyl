package com.shivam.store.services;

import com.shivam.store.dtos.ProductDto;
import com.shivam.store.dtos.RegisterProductRequest;
import com.shivam.store.exceptions.ProductNotFoundException;
import com.shivam.store.mappers.ProductMapper;
import com.shivam.store.repositories.CategoryRepository;
import com.shivam.store.repositories.ProductRepository;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    public static final String PRODUCTS_CACHE = "products";
    public static final String PRODUCT_BY_ID_CACHE = "productById";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    @Cacheable(cacheNames = PRODUCTS_CACHE, key = "T(java.util.Objects).toString(#categoryId, 'all')")
    @Transactional(readOnly = true)
    public List<ProductDto> getProducts(Byte categoryId) {
        if (categoryId != null) {
            return productRepository.findByCategory_Id(categoryId).stream()
                    .map(productMapper::toDto)
                    .toList();
        }
        return productRepository.findAll().stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Cacheable(cacheNames = PRODUCT_BY_ID_CACHE, key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public ProductDto getProduct(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .orElseThrow(ProductNotFoundException::new);
    }

    @Caching(
            evict = @CacheEvict(cacheNames = PRODUCTS_CACHE, allEntries = true),
            put = @CachePut(cacheNames = PRODUCT_BY_ID_CACHE, key = "#result.id", condition = "#result != null")
    )
    public ProductDto createProduct(RegisterProductRequest request) {
        var category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category id: " + request.getCategoryId()));
        var product = productMapper.toEntity(request);
        product.setCategory(category);
        productRepository.save(product);
        return productMapper.toDto(product);
    }

    @Caching(
            evict = @CacheEvict(cacheNames = PRODUCTS_CACHE, allEntries = true),
            put = @CachePut(cacheNames = PRODUCT_BY_ID_CACHE, key = "#id", condition = "#result != null")
    )
    public ProductDto updateProduct(long id, ProductDto productDto) {
        var product = productRepository.findById(id).orElseThrow(ProductNotFoundException::new);
        productMapper.updateProduct(productDto, product);
        productRepository.save(product);
        return productMapper.toDto(product);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = PRODUCTS_CACHE, allEntries = true),
            @CacheEvict(cacheNames = PRODUCT_BY_ID_CACHE, key = "#id")
    })
    public void deleteProduct(long id) {
        var product = productRepository.findById(id).orElseThrow(ProductNotFoundException::new);
        productRepository.delete(product);
    }
}

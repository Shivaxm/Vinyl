package com.shivam.store.controllers;

import com.shivam.store.dtos.ProductDto;
import com.shivam.store.dtos.RegisterProductRequest;
import com.shivam.store.mappers.ProductMapper;
import com.shivam.store.repositories.CategoryRepository;
import com.shivam.store.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;


@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @GetMapping
    public List<ProductDto> getProducts(@RequestParam(required = false, name = "categoryId") Byte cat){
        if(cat != null){
            return productRepository.findByCategory_Id(cat).stream().map(productMapper::toDto).toList();
        }
        return productRepository.findAll().stream().map(productMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getId(@PathVariable Long id) {
        return productRepository.findById(id).map(productMapper::toDto)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
        
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody RegisterProductRequest request, UriComponentsBuilder uriBuilder) {
        var category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        if(category == null){
            return ResponseEntity.badRequest().build();
        }
        var product = productMapper.toEntity(request);
        product.setCategory(category);
        productRepository.save(product);
        var productDto = productMapper.toDto(product);
        //productDto.setId(product.getId());
        var uri = uriBuilder.path("/products/{id}").buildAndExpand(productDto.getId()).toUri();
        return ResponseEntity.created(uri).body(productDto);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable long id, @RequestBody ProductDto productDto) {
        
        var product = productRepository.findById(id).orElse(null);
        if(product == null){
            return ResponseEntity.notFound().build();
        }
        
        productMapper.updateProduct(productDto, product);
        productRepository.save(product);

        return ResponseEntity.ok(productMapper.toDto(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable long id){
        var product = productRepository.findById(id).orElse(null);
        if(product == null){
            return ResponseEntity.badRequest().build();
        }
        
        productRepository.delete(product);
        return ResponseEntity.noContent().build();

    }
    
}

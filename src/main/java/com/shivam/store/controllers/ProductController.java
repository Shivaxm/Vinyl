package com.shivam.store.controllers;

import com.shivam.store.dtos.ProductDto;
import com.shivam.store.dtos.RegisterProductRequest;
import com.shivam.store.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;


@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductDto> getProducts(@RequestParam(required = false, name = "categoryId") Byte cat){
        return productService.getProducts(cat);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getId(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody RegisterProductRequest request, UriComponentsBuilder uriBuilder) {
        var productDto = productService.createProduct(request);
        var uri = uriBuilder.path("/products/{id}").buildAndExpand(productDto.getId()).toUri();
        return ResponseEntity.created(uri).body(productDto);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable long id, @Valid @RequestBody ProductDto productDto) {
        return ResponseEntity.ok(productService.updateProduct(id, productDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable long id){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();

    }
    
}

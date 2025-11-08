package com.shivam.store.mappers;

import com.shivam.store.dtos.CartProductDto;
import com.shivam.store.dtos.ProductDto;
import com.shivam.store.dtos.RegisterProductRequest;
import com.shivam.store.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(source = "category.id", target = "categoryId")
    ProductDto toDto(Product product);

    Product toEntity(RegisterProductRequest request);
    Product toEntity(ProductDto request);

    @Mapping(target = "id", ignore = true)
    void updateProduct(ProductDto productDto, @MappingTarget Product product);

    CartProductDto toCartProductDto(Product product);
    
}

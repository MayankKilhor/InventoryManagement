package com.imspos.product_service.service.impl;

import com.imspos.product_service.model.Category;
import com.imspos.product_service.model.Product;
import com.imspos.product_service.model.Variant;
import com.imspos.product_service.payload.dto.AuditableUser;
import com.imspos.product_service.payload.request.CreateCategoryRequest;
import com.imspos.product_service.payload.request.CreateProductRequest;
import com.imspos.product_service.payload.request.CreateVariantRequest;
import com.imspos.product_service.repository.CategoryRepository;
import com.imspos.product_service.repository.ProductRepository;
import com.imspos.product_service.repository.VariantRepository;
import com.imspos.product_service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private VariantRepository variantRepository;

    @Autowired
    private ProductRepository productRepository;

    public Category createCategory(CreateCategoryRequest createCategoryRequest, AuditableUser user){
        Category category = Category.builder()
                .name(createCategoryRequest.getName())
                .description(createCategoryRequest.getDescription())
                .build();

        category.setCreatedBy(user);
        category.setUpdatedBy(user);

        categoryRepository.save(category);

        return category;
    }

    public Variant createVariant(CreateVariantRequest createVariantRequest, AuditableUser user){
        Product product = productRepository.findById(createVariantRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + createVariantRequest.getProductId()));

        Variant variant = Variant.builder()
                .sku(createVariantRequest.getSku())
                .barcode(createVariantRequest.getBarcode())
                .color(createVariantRequest.getColor())
                .size(createVariantRequest.getSize())
                .price(createVariantRequest.getPrice())
                .stock(createVariantRequest.getStock())
                .batchNumber(createVariantRequest.getBatchNumber())
                .product(product)
                .build();


        variant.setCreatedBy(user);
        variant.setUpdatedBy(user);

        variantRepository.save(variant);

        return variant;
    }

    public Product createProduct(CreateProductRequest createProductRequest, AuditableUser user) {
        Category category = categoryRepository.findById(createProductRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + createProductRequest.getCategoryId()));

        Product product = Product.builder()
                .name(createProductRequest.getName())
                .description(createProductRequest.getDescription())
                .brand(createProductRequest.getBrand())
                .category(category)
                .build();

        product.setCreatedBy(user);
        product.setUpdatedBy(user);

        return productRepository.save(product);
    }



}

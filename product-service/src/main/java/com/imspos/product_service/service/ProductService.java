package com.imspos.product_service.service;

import com.imspos.product_service.model.Category;
import com.imspos.product_service.model.Product;
import com.imspos.product_service.model.Variant;
import com.imspos.product_service.payload.dto.AuditableUser;
import com.imspos.product_service.payload.request.CreateCategoryRequest;
import com.imspos.product_service.payload.request.CreateProductRequest;
import com.imspos.product_service.payload.request.CreateVariantRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service
public interface ProductService {

    public Category createCategory(CreateCategoryRequest createCategoryRequest, AuditableUser user);

    public Variant createVariant(CreateVariantRequest createVariantRequest, AuditableUser user);

    public Product createProduct(CreateProductRequest createProductRequest, AuditableUser user);
}

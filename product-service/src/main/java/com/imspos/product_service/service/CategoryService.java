package com.imspos.product_service.service;

import com.imspos.product_service.model.Category;
import com.imspos.product_service.model.Product;
import com.imspos.product_service.model.Variant;
import com.imspos.product_service.payload.dto.AuditableUser;
import com.imspos.product_service.payload.dto.CategoryListDTO;
import com.imspos.product_service.payload.request.CreateCategoryRequest;
import com.imspos.product_service.payload.request.CreateProductRequest;
import com.imspos.product_service.payload.request.CreateVariantRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {

    public Category createCategory(CreateCategoryRequest createCategoryRequest, AuditableUser user);

    public List<CategoryListDTO> getAllCategory();
}

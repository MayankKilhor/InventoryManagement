package com.imspos.product_service.service.impl;

import com.imspos.product_service.model.Category;
import com.imspos.product_service.payload.dto.AuditableUser;
import com.imspos.product_service.payload.request.CreateCategoryRequest;
import com.imspos.product_service.repository.CategoryRepository;
import com.imspos.product_service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private CategoryRepository categoryRepository;

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
}

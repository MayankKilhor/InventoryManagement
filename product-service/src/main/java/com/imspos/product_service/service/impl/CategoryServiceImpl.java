package com.imspos.product_service.service.impl;

import com.imspos.product_service.model.Category;
import com.imspos.product_service.model.Product;
import com.imspos.product_service.model.Variant;
import com.imspos.product_service.payload.dto.AuditableUser;
import com.imspos.product_service.payload.dto.CategoryListDTO;
import com.imspos.product_service.payload.request.CreateCategoryRequest;
import com.imspos.product_service.payload.request.CreateProductRequest;
import com.imspos.product_service.payload.request.CreateVariantRequest;
import com.imspos.product_service.repository.CategoryRepository;
import com.imspos.product_service.repository.ProductRepository;
import com.imspos.product_service.repository.VariantRepository;
import com.imspos.product_service.service.CategoryService;
import com.imspos.product_service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

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
    public List<CategoryListDTO> getAllCategory(){

        List<Category> categoryList = categoryRepository.findAll();

        List<CategoryListDTO> categoryListDTOList = categoryList.stream()
                .map(category -> new CategoryListDTO(category)) // or use a method reference if applicable
                .collect(Collectors.toList());

        return categoryListDTOList;

    }



}

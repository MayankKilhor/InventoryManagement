package com.imspos.product_service.service;

import com.imspos.product_service.model.Category;
import com.imspos.product_service.payload.dto.AuditableUser;
import com.imspos.product_service.payload.request.CreateCategoryRequest;
import org.springframework.stereotype.Service;

@Service
public interface ProductService {

    public Category createCategory(CreateCategoryRequest createCategoryRequest, AuditableUser user);
}

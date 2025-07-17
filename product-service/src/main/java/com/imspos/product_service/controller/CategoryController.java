package com.imspos.product_service.controller;

import com.imspos.product_service.model.Category;
import com.imspos.product_service.payload.dto.AuditableUser;
import com.imspos.product_service.payload.dto.CategoryListDTO;
import com.imspos.product_service.payload.request.CreateCategoryRequest;
import com.imspos.product_service.payload.response.ApiErrorResponse;
import com.imspos.product_service.payload.response.ApiResponse;
import com.imspos.product_service.service.CategoryService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Transactional
    @PostMapping("/createCategory")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CreateCategoryRequest createCategoryRequest, @RequestHeader("x-user-id") String userId, @RequestHeader("x-username") String username ){
        try{

            AuditableUser user = new AuditableUser(userId, username);

            Category created = categoryService.createCategory(createCategoryRequest, user);

            ApiResponse response = new ApiResponse(true, "Category created successfully", null);
            response.addDetail("categoryId", created.getId());
            response.addDetail("name", created.getName());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch(Exception e){

            ApiErrorResponse errorResponse = new ApiErrorResponse(false,"Failed to create Category!");
            errorResponse.addDetail("error",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/allCategories")
    public ResponseEntity<?> allCategories(@RequestHeader("x-user-id") String userId, @RequestHeader("x-username") String username){
        try{
            List<CategoryListDTO> categoryListDTOList = categoryService.getAllCategory();
            ApiResponse apiResponse = new ApiResponse(true, "Successfully fetched all Categories List");

            apiResponse.addDetail("categoryList",categoryListDTOList);

            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

        }catch(Exception e) {
            ApiErrorResponse errorResponse = new ApiErrorResponse(false, "Failed to fetch all Categories!");
            errorResponse.addDetail("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

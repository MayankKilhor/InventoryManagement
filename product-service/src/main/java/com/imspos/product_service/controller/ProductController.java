package com.imspos.product_service.controller;


import com.imspos.product_service.model.Category;
import com.imspos.product_service.model.Variant;
import com.imspos.product_service.payload.dto.AuditableUser;
import com.imspos.product_service.payload.request.CreateCategoryRequest;
import com.imspos.product_service.payload.request.CreateVariantRequest;
import com.imspos.product_service.payload.response.ApiErrorResponse;
import com.imspos.product_service.payload.response.ApiResponse;
import com.imspos.product_service.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/createCategory")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CreateCategoryRequest createCategoryRequest, @RequestHeader("x-user-id") String userId, @RequestHeader("x-username") String username ){
        try{

            AuditableUser user = new AuditableUser(userId, username);

            Category created = productService.createCategory(createCategoryRequest, user);

            ApiResponse response = new ApiResponse(true, "Category created successfully", null);
            response.addDetail("categoryId", created.getId());
            response.addDetail("name", created.getName());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
//        }catch(BadRequestException  e){
//
//            ApiErrorResponse errorResponse = new ApiErrorResponse(false,"Failed to create Category!");
//            errorResponse.addDetail("error",e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }catch(Exception e){

            ApiErrorResponse errorResponse = new ApiErrorResponse(false,"Failed to create Category!");
            errorResponse.addDetail("error",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping("/createVariant")
    public ResponseEntity<?> createVariant(
            @Valid @RequestBody CreateVariantRequest createVariantRequest,
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-username") String username) {
        try {
            AuditableUser user = new AuditableUser(userId, username);

            Variant created = productService.createVariant(createVariantRequest, user);

            ApiResponse response = new ApiResponse(true, "Variant created successfully", null);
            response.addDetail("variantId", created.getId());
            response.addDetail("sku", created.getSku());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            ApiErrorResponse errorResponse = new ApiErrorResponse(false, "Failed to create Variant!");
            errorResponse.addDetail("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    //TODO connect with openfeign
}

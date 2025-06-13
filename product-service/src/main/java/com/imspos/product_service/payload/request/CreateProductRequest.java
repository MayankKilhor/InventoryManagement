package com.imspos.product_service.payload.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    private String brand;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}

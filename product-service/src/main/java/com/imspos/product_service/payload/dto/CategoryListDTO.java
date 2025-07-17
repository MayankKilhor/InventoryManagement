package com.imspos.product_service.payload.dto;

import com.imspos.product_service.model.Category;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryListDTO {
    private Long id;

    private String name;

    private String description;

    public CategoryListDTO(Category category){
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
    }

}

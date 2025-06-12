package com.imspos.product_service.repository;

import com.imspos.product_service.model.Variant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariantRepository extends JpaRepository<Variant, Long> {

}
package com.imspos.auth_service.repository;

import com.imspos.auth_service.model.ApiAccessControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiAccessControlRepository extends JpaRepository<ApiAccessControl, Long> {


    Optional<ApiAccessControl> findByEndpointPath(String endpointPath);
}

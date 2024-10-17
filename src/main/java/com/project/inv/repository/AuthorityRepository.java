package com.project.inv.repository;


import com.project.inv.model.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, String> {
    Optional<Authority> findByName(String name);
}

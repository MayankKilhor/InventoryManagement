package com.project.inv.repository;
import com.project.inv.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {


    User findByUsername(String username);
}
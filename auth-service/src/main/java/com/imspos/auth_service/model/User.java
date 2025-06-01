package com.imspos.auth_service.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "name",nullable = false)
    private String name;

    @Column(name = "username",unique = true,nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String password_hash;

    @Column(name="designation")
    private String designation;

    @Column(name="image_url")
    private String imageUrl;

    @Column(name="location")
    private String location;

    @Column(name = "phone_no")
    private String phoneNo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_role"))
    @JsonBackReference
    private UserRole userRole;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Column(name = "updated_at",nullable = false)
    private Date updatedAt;


}

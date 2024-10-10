package com.project.inv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseTimeEntity{
    @Id
    @Column(name = "user_id",nullable = false)
    private String userId;

    @Column(name = "description")
    private String description;

    @Column(name = "name")
    private String name;
}


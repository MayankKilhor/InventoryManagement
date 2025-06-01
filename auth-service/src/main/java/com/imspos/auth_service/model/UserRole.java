package com.imspos.auth_service.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "user_role")
@Getter
@Setter
public class UserRole {

    @Id
    @Column(name = "role_id", nullable = false)
    private String roleId;

    @Column(name = "name", unique = true,nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role_authority",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id")
    )
    private Set<Authority> authorities;

    @ManyToOne(fetch = FetchType.EAGER) // Many UserRoles can be created by one User
    @JoinColumn(name = "created_by", referencedColumnName = "user_id")
    @JsonBackReference
    private User createdBy;

    @ManyToOne(fetch = FetchType.EAGER) // Many UserRoles can be created by one User
    @JoinColumn(name = "updated_by", referencedColumnName = "user_id")
    @JsonBackReference
    private User updatedBy;


    @Column(name = "created_at", nullable = false)
    private Date created_at;

    @Column(name = "updated_at",nullable = false)
    private Date updated_at;

}

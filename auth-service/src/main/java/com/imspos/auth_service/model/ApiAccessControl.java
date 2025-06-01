package com.imspos.auth_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "api_access_control")
@Getter
@Setter
public class ApiAccessControl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "endpoint_path", nullable = false, unique = true)
    private String endpointPath;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "api_access_authority",  // Join table name
            joinColumns = @JoinColumn(name = "api_access_id"),  // Foreign key for ApiAccessControl
            inverseJoinColumns = @JoinColumn(name = "authority_id")  // Foreign key for Authority
    )
    private Set<Authority> authorities;

    @Column(name = "public")
    private Boolean isPublic  = Boolean.FALSE;

}
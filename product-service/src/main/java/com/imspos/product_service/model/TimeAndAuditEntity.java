package com.imspos.product_service.model;

import com.imspos.product_service.payload.dto.AuditableUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class TimeAndAuditEntity {

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "userId", column = @Column(name = "created_by_user_id")),
            @AttributeOverride(name = "username", column = @Column(name = "created_by_username"))
    })
    private AuditableUser createdBy;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "userId", column = @Column(name = "updated_by_user_id")),
            @AttributeOverride(name = "username", column = @Column(name = "updated_by_username"))
    })
    private AuditableUser updatedBy;

}

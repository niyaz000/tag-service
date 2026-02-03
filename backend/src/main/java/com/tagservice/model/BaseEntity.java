package com.tagservice.model;

import com.tagservice.util.MDCUtil;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Base entity providing common auditing fields and request tracking.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", length = 40, updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 40)
    private String updatedBy;

    @Version
    @Column(nullable = false)
    private Integer version;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    /**
     * Automatically populates requestId from MDC if available before persisting.
     */
    @PrePersist
    protected void prePersist() {
        this.requestId = MDCUtil.getCurrentRequestIdAsUUID();
    }
}


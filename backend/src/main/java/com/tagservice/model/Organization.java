package com.tagservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * JPA entity representing the organizations table.
 */
@Entity
@Table(name = "organizations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "display_name", nullable = false, length = 255, unique = true)
    private String displayName;

    @Column(nullable = false, length = 255, unique = true)
    private String domain;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    /**
     * Raw JSON settings stored as JSONB in PostgreSQL.
     */
    @Column(columnDefinition = "jsonb")
    private String settings;
}


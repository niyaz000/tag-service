package com.tagservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapping the {@code tenant_settings} table.
 * <p>
 * Field names follow the database schema defined in {@code DATABASE_SCHEMA.md}
 * for the tenant_settings table.
 */
@Entity
@Table(name = "tenant_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationSetting extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "allow_upsert_tag", nullable = false)
    private boolean allowUpsertTag;

    @Column(name = "allow_rename_tag", nullable = false)
    private boolean allowRenameTag;

    @Column(name = "allow_delete_active_tag", nullable = false)
    private boolean allowDeleteActiveTag;

    @Column(name = "allow_color", nullable = false)
    private boolean allowColor;

    @Column(name = "key_case_sensitive", nullable = false)
    private boolean keyCaseSensitive;

    @Column(name = "max_tag_count_per_entity", nullable = false)
    private int maxTagCountPerEntity;

    @Column(name = "min_tag_key_length", nullable = false)
    private int minTagKeyLength;

    @Column(name = "max_tag_key_length", nullable = false)
    private int maxTagKeyLength;

    @Column(name = "min_tag_value_length", nullable = false)
    private int minTagValueLength;

    @Column(name = "max_tag_value_length", nullable = false)
    private int maxTagValueLength;

    @Column(name = "search_mode", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private SearchMode searchMode;
}


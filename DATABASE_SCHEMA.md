# Database Schema Documentation

## Overview
The **Tags as a Service** application uses **PostgreSQL** as its persistence layer.

## Tables

### `organizations`
Stores valid tenant/organization identities.
**RLS Enabled**: Yes.
*   **Insert**: Bypassed (Allowed for all authenticated users).
*   **Select/Update**: Restricted to `app.current_org_id`, unless user is `admin_user` (full access).
*   **Delete**: Disabled for standard users. Allowed for `admin_user`.
*   **Permissions**: `app_user` has `SELECT`, `INSERT`, `UPDATE`. `admin_user` has full access including `DELETE`.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `SERIAL` | `PRIMARY KEY` | Unique auto-incrementing identifier. |
| `name` | `VARCHAR(100)` | `NOT NULL, CHECK > 0` | Internal name or slug. |
| `display_name` | `VARCHAR(255)` | `NOT NULL, UNIQUE, CHECK > 0` | Human-readable name. |
| `domain` | `VARCHAR(255)` | `NOT NULL, UNIQUE, CHECK > 0` | Associated domain name. |
| `type` | `VARCHAR(50)` | `NOT NULL, DEFAULT 'standard'` | Org Type (e.g. enterprise). |
| `version` | `INTEGER` | `DEFAULT 1` | Record version number. |
| `created_at` | `TIMESTAMPTZ` | `DEFAULT NOW()` | Creation timestamp. |
| `created_by` | `VARCHAR(40)` | `NULLABLE` | User/System that created the record. |
| `updated_at` | `TIMESTAMPTZ` | `DEFAULT NOW()` | Last update timestamp. |
| `updated_by` | `VARCHAR(40)` | `NULLABLE` | User who last updated the record. |
| `deleted_at` | `TIMESTAMPTZ` | `NULLABLE` | Timestamp when the record was soft-deleted. |
| `request_id` | `UUID` | `NOT NULL` | Traceability ID for the request. |

### `organizations_audit`
Records all changes to the `organizations` table.
**Partitioned**: By `created_at` (Range).
**Append-Only**: Updates and Deletes are blocked for standard users (Allowed for `admin_user`).
**Select**: Restricted to `app.current_org_id` or `admin_user`.
**Indexes**: `(organization_id)` for fast modification history lookup.

| Column | Type | Description |
| :--- | :--- | :--- |
| `audit_id` | `SERIAL` | Part of Composite PK `(audit_id, created_at)`. |
| `organization_id` | `INTEGER` | ID of the organization being audited. |
| `action` | `ENUM` | `audit_action_type`: 'INSERT', 'UPDATE', 'DELETE'. |
| `created_at` | `TIMESTAMPTZ` | When the audit event happened. |
| `created_by` | `VARCHAR(40)` | User who performed the action. |
| `request_id` | `UUID` | ID of the request causing the change. |

### `tenant_settings`
Stores configuration and constraints per organization.
**RLS Enabled**: Yes. One-to-One with `organizations`.

| Column | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `setting_id` | `SERIAL PK` | - | Unique ID. |
| `organization_id` | `INTEGER` | - | FK to organizations. |
| `allow_upsert_tag` | `BOOLEAN` | `TRUE` | Enable PUT to create tags. |
| `allow_rename_tag` | `BOOLEAN` | `TRUE` | Enable renaming tag keys. |
| `allow_delete_active_tag` | `BOOLEAN` | `FALSE` | Controls deletion behavior. |
| `allow_color` | `BOOLEAN` | `TRUE` | Enable color metadata. |
| `key_case_sensitive` | `BOOLEAN` | `FALSE` | Enforce key casing. |
| `max_tag_count_per_entity` | `INT` | `50` | Max tags per entity. |
| `min/max_tag_key_length` | `INT` | `1`/`50` | Key length constraints. |
| `min/max_tag_value_length` | `INT` | `0`/`255` | Value length constraints. |
| `search_mode` | `VARCHAR` | `partial` | Search behavior mode. |
| `name`, `*_changed` | ... | Mirrored data and change flags. |
| `display_name`, `*_changed` | ... | Mirrored data and change flags. |
| `domain`, `*_changed` | ... | Mirrored data and change flags. |
| `version` | ... | Mirrored data (no changed flag). |
| `deleted_at` | ... | Mirrored data (no changed flag). |

### `tags`
Stores the actual tag data linking an entity to a key-value pair.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `SERIAL` | `PRIMARY KEY` | Unique auto-incrementing identifier. |
| `tenant_id` | `INTEGER` | `FK -> organizations(id)` | Must exist in `organizations`. |
| `entity_type` | `VARCHAR(50)` | `NOT NULL` | The category of the entity. |
| `entity_id` | `VARCHAR(255)` | `NOT NULL` | The unique ID of the entity. |
| `key` | `VARCHAR(100)` | `NOT NULL` | The tag name/category. |
| `value` | `TEXT` | `NULLABLE` | The tag value. |
| `created_at` | `TIMESTAMPTZ` | `DEFAULT NOW()` | When the tag was added. |

## Indexes

1.  **Entity Lookup Index**: `(tenant_id, entity_type, entity_id)`
2.  **Tag Query Index**: `(tenant_id, key, value)`

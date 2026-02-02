-- =============================================
-- Migration: V2 Seed Organizations
-- Description: Seed data for local development. Insert test organizations.
-- =============================================

INSERT INTO organizations (
    name, 
    display_name, 
    domain, 
    type,
    created_by, 
    created_at,
    updated_at,
    updated_by,
    deleted_at,
    request_id,
    version,
    settings
) VALUES 
(
    'freshworks', 
    'freshworks', 
    'freshworks.tags.com', 
    'dynamic',
    '1',
    NOW(),
    NOW(),
    '1',
    NULL,
    gen_random_uuid(),
    0,
    '{
        "constraints": {
            "max_count": 10,
            "case_sensitive": false,
            "length": {"min": 1, "max": 64},
            "type_required": false
        },
        "features": {
            "upsert_tag": true,
            "rename_tag": false,
            "delete_tag": true,
            "delete_active_tag": true,
            "search_mode": "WILDCARD",
            "colors_enabled": false
        }
    }'::jsonb
),
(
    'amazon', 
    'amazon', 
    'amazon.tags.com', 
    'static',
    '1', 
    NOW(),
    NOW(),
    '1',
    NULL,
    gen_random_uuid(),
    0,
    '{
        "constraints": {
            "max_count": 10,
            "case_sensitive": false,
            "length": {"min": 1, "max": 64},
            "type_required": false
        },
        "features": {
            "upsert_tag": false,
            "rename_tag": true,
            "delete_tag": true,
            "delete_active_tag": true,
            "search_mode": "WILDCARD",
            "colors_enabled": true
        }
    }'::jsonb
)
ON CONFLICT (domain) DO NOTHING; -- Idempotency check 

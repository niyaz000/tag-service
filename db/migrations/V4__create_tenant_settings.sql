-- =============================================
-- Migration: V4 Create Tenant Settings Table
-- Description: Stores per-tenant configuration and constraints for the tagging service.
-- =============================================

-- 1. Create tenant_settings table
-- One-to-One relationship with organizations.
CREATE TABLE IF NOT EXISTS tenant_settings (
    setting_id SERIAL PRIMARY KEY,
    organization_id INTEGER NOT NULL UNIQUE REFERENCES organizations(id) ON DELETE CASCADE,
    
    -- Feature Flags
    allow_upsert_tag BOOLEAN NOT NULL DEFAULT TRUE,       -- If true, PUT can create. If false, separate POST/PUT required.
    allow_rename_tag BOOLEAN NOT NULL DEFAULT TRUE,       -- If true, allows changing the Key of an existing tag.
    allow_delete_active_tag BOOLEAN NOT NULL DEFAULT FALSE, -- If false, tags must be archived/soft-deleted? (User specific requirement)
    allow_color BOOLEAN NOT NULL DEFAULT TRUE,            -- If true, tags can have associated color metadata.

    -- Tag Constraints
    key_case_sensitive BOOLEAN NOT NULL DEFAULT FALSE,    -- Enforce case sensitivity for Tag Keys.
    max_tag_count_per_entity INTEGER NOT NULL DEFAULT 50, -- Limit tags per entity to prevent spam.
    min_tag_key_length INTEGER NOT NULL DEFAULT 1,
    max_tag_key_length INTEGER NOT NULL DEFAULT 50,
    min_tag_value_length INTEGER NOT NULL DEFAULT 0,
    max_tag_value_length INTEGER NOT NULL DEFAULT 255,

    -- Search Configuration
    search_mode VARCHAR(20) NOT NULL DEFAULT 'partial',   -- e.g., 'exact', 'partial', 'fuzzy'

    -- Audit Columns
    version INTEGER NOT NULL DEFAULT 1,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(40)
);

-- 2. Index for fast lookup by Organization
-- (Though UNIQUE constraint above already creates an index, declaring intent is good)
-- CREATE INDEX IF NOT EXISTS idx_tenant_settings_org_id ON tenant_settings(organization_id);

-- 3. Idempotent Trigger Helper (if not already defined in V1 in this session)
-- (We assume V1 ran, but safe to redefine or check existence)
CREATE OR REPLACE FUNCTION drop_trigger_if_exists(trigger_name text, table_name text)
RETURNS void AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = trigger_name) THEN
        EXECUTE 'DROP TRIGGER ' || trigger_name || ' ON ' || table_name;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- 4. Auto-Update Timestamp
CREATE OR REPLACE FUNCTION update_tenant_settings_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

SELECT drop_trigger_if_exists('trigger_update_tenant_settings_ts', 'tenant_settings');

CREATE TRIGGER trigger_update_tenant_settings_ts
BEFORE UPDATE ON tenant_settings
FOR EACH ROW EXECUTE FUNCTION update_tenant_settings_timestamp();

-- 5. Row Level Security (RLS)
ALTER TABLE tenant_settings ENABLE ROW LEVEL SECURITY;

-- Policy: Insert
-- Allow inserts by admin or the org owner (during org creation usually).
DROP POLICY IF EXISTS policy_insert_tenant_settings ON tenant_settings;
CREATE POLICY policy_insert_tenant_settings ON tenant_settings
    FOR INSERT WITH CHECK (
        current_user = 'admin_user' 
        OR 
        organization_id = current_setting('app.current_org_id', true)::INTEGER
    );

-- Policy: Select
-- Visible to Org Owner and Admin.
DROP POLICY IF EXISTS policy_select_tenant_settings ON tenant_settings;
CREATE POLICY policy_select_tenant_settings ON tenant_settings
    FOR SELECT USING (
        current_user = 'admin_user' 
        OR 
        organization_id = current_setting('app.current_org_id', true)::INTEGER
    );

-- Policy: Update
-- Editable by Org Owner and Admin.
DROP POLICY IF EXISTS policy_update_tenant_settings ON tenant_settings;
CREATE POLICY policy_update_tenant_settings ON tenant_settings
    FOR UPDATE USING (
        current_user = 'admin_user' 
        OR 
        organization_id = current_setting('app.current_org_id', true)::INTEGER
    );

-- Policy: Delete
-- Generally settings shouldn't be deleted unless Org is deleted (CASCADE).
-- Admin can delete.
DROP POLICY IF EXISTS policy_delete_tenant_settings ON tenant_settings;
CREATE POLICY policy_delete_tenant_settings ON tenant_settings
    FOR DELETE USING (current_user = 'admin_user');

-- =============================================
-- Migration: V1 Create Organizations Table
-- Description: Sets up the core organizations table, audit logging, and Row Level Security (RLS).
-- =============================================

-- 0. Custom Types
-- Create ENUM type for audit actions to ensure data integrity and optimize storage.
-- This aligns with best practices for known, limited sets of values.
DO $$ BEGIN
    CREATE TYPE audit_action_type AS ENUM ('INSERT', 'UPDATE', 'DELETE');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

-- 1. Create organizations table
-- Storage for tenant/organization identities.
-- Includes soft-delete support via `deleted_at` and traceability via `request_id`.
-- Constraints ensure data quality (non-empty strings, unique names).
-- Recommendation: Using Partial Unique Indexes instead of UNIQUE constraints usually allows better soft-delete reusability,
-- but standard constraints are stricter. Kept as standard UNIQUE for now.
CREATE TABLE IF NOT EXISTS organizations (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL CHECK (length(name) > 0),
    display_name VARCHAR(255) NOT NULL UNIQUE CHECK (length(display_name) > 0),
    domain VARCHAR(255) NOT NULL UNIQUE CHECK (length(domain) > 0),
    type VARCHAR(50) NOT NULL DEFAULT 'standard', -- Organization Type (e.g. 'enterprise', 'trial', 'standard')
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    created_by VARCHAR(40),
    updated_at TIMESTAMPTZ DEFAULT NOW(), -- Track last update time
    updated_by VARCHAR(40), -- User who last updated the record
    deleted_at TIMESTAMPTZ, -- If NULL, record is active. If set, record is soft-deleted.
    settings JSONB DEFAULT '{}'::jsonb, -- Flexible tenant configuration
    request_id UUID NOT NULL -- Traceability ID for the request that created/updated this record.
);

-- 2. Audit Table (Partitioned)
-- Stores a history of all changes to the organizations table.
-- Partitioned by RANGE on created_at for efficient time-based management (archiving, dropping old logs).
-- Mirroring columns allows for point-in-time reconstruction.
CREATE TABLE IF NOT EXISTS organizations_audit (
    audit_id SERIAL, -- specific PK handling below
    organization_id INTEGER NOT NULL,
    action audit_action_type NOT NULL, -- Uses the ENUM type created above
    created_at TIMESTAMPTZ DEFAULT NOW(), -- When the audit log was created
    created_by VARCHAR(40), -- The actor who performed the action
    request_id UUID NOT NULL, -- Traceability back to the request that caused the change

    -- Mirrored columns (Snapshot of data at time of event)
    name VARCHAR(100),
    display_name VARCHAR(255),
    domain VARCHAR(255),
    type VARCHAR(50),
    version INTEGER,
    deleted_at TIMESTAMPTZ,
    settings JSONB,

    -- Change flags (True if the specific column was modified in this transaction)
    name_changed BOOLEAN,
    display_name_changed BOOLEAN,
    domain_changed BOOLEAN,
    type_changed BOOLEAN,
    settings_changed BOOLEAN,

    -- Primary Key must include partition key for PostgreSQL partitioning support
    PRIMARY KEY (audit_id, created_at)
) PARTITION BY RANGE (created_at);

-- Create a Default Partition (catches everything not in specific ranges)
-- In a real production setup, you would create specific monthly/yearly partitions.
CREATE TABLE IF NOT EXISTS organizations_audit_default PARTITION OF organizations_audit DEFAULT;

-- Create an Index on organization_id for fast lookup of history
-- Essential for querying the history of a specific organization.
CREATE INDEX IF NOT EXISTS idx_organizations_audit_org_id ON organizations_audit(organization_id);

-- 3. Audit Trigger Function
-- logic to populate the audit table automatically on Insert, Update, or Delete.
CREATE OR REPLACE FUNCTION audit_organizations_changes()
RETURNS TRIGGER AS $$
DECLARE
    current_audit_user VARCHAR(40);
    current_action audit_action_type; -- Uses ENUM type
BEGIN
    -- Determine the user for the audit log
    IF (TG_OP = 'INSERT') THEN
        current_audit_user := NEW.created_by;
    ELSE
        -- Fallback to current database user. In a real app, this might come from a session variable.
        current_audit_user := current_user; 
    END IF;

    IF (TG_OP = 'INSERT') THEN
        INSERT INTO organizations_audit (
            organization_id, action, created_at, created_by, request_id,
            name, display_name, domain, type, version, deleted_at, settings,
            name_changed, display_name_changed, domain_changed, type_changed, settings_changed
        ) VALUES (
            NEW.id, 'INSERT', NOW(), current_audit_user, NEW.request_id,
            NEW.name, NEW.display_name, NEW.domain, NEW.type, NEW.version, NEW.deleted_at, NEW.settings,
            TRUE, TRUE, TRUE, TRUE, TRUE
        );
        RETURN NEW;
    ELSIF (TG_OP = 'UPDATE') THEN
        -- Detect Soft Delete vs Regular Update
        -- A soft delete is characterized by `deleted_at` transitioning from NULL to NOT NULL.
        IF NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL THEN
             current_action := 'DELETE'; -- Log soft-deletes as DELETE actions for audit clarity
        ELSE
             current_action := 'UPDATE';
        END IF;

        INSERT INTO organizations_audit (
            organization_id, action, created_at, created_by, request_id,
            name, display_name, domain, type, version, deleted_at, settings,
            name_changed, display_name_changed, domain_changed, type_changed, settings_changed
        ) VALUES (
            NEW.id, 
            current_action, 
            NOW(), current_audit_user, NEW.request_id,
            NEW.name, NEW.display_name, NEW.domain, NEW.type, NEW.version, NEW.deleted_at, NEW.settings,
            (NEW.name IS DISTINCT FROM OLD.name),
            (NEW.display_name IS DISTINCT FROM OLD.display_name),
            (NEW.domain IS DISTINCT FROM OLD.domain),
            (NEW.type IS DISTINCT FROM OLD.type),
            (NEW.settings IS DISTINCT FROM OLD.settings)
        );
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        -- Log hard deletes (though these should be blocked by RLS for standard users)
        INSERT INTO organizations_audit (
            organization_id, action, created_at, created_by, request_id,
            name, display_name, domain, type, version, deleted_at, settings,
            name_changed, display_name_changed, domain_changed, type_changed, settings_changed
        ) VALUES (
            OLD.id, 'DELETE', NOW(), current_audit_user, OLD.request_id,
            OLD.name, OLD.display_name, OLD.domain, OLD.type, OLD.version, OLD.deleted_at, OLD.settings,
            FALSE, FALSE, FALSE, FALSE, FALSE
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- 4. Safe Trigger Drop Helper
-- Required to make the migration idempotent (re-runnable without errors)
CREATE OR REPLACE FUNCTION drop_trigger_if_exists(trigger_name text, table_name text)
RETURNS void AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = trigger_name) THEN
        EXECUTE 'DROP TRIGGER ' || trigger_name || ' ON ' || table_name;
    END IF;
END;
$$ LANGUAGE plpgsql;

SELECT drop_trigger_if_exists('trigger_audit_organizations', 'organizations');

-- 5. Apply Audit Trigger
CREATE TRIGGER trigger_audit_organizations
AFTER INSERT OR UPDATE OR DELETE ON organizations
FOR EACH ROW EXECUTE FUNCTION audit_organizations_changes();

-- 5.1 Auto-Update Timestamp Function
-- Added back since updated_at column was restored.
CREATE OR REPLACE FUNCTION update_organizations_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

SELECT drop_trigger_if_exists('trigger_update_organizations_ts', 'organizations');

CREATE TRIGGER trigger_update_organizations_ts
BEFORE UPDATE ON organizations
FOR EACH ROW EXECUTE FUNCTION update_organizations_updated_at();

-- 6. Row Level Security (RLS): Organizations
-- Security layer to enforce tenant isolation and data integrity at the database level.
ALTER TABLE organizations ENABLE ROW LEVEL SECURITY;

-- Note: 'admin_user' is treated as a Super Admin in policies below.

-- Policy: Insert
-- Allow creation of new organizations by all users.
DROP POLICY IF EXISTS policy_insert_organizations ON organizations;
CREATE POLICY policy_insert_organizations ON organizations
    FOR INSERT WITH CHECK (true);

-- Policy: Select
-- Standard user: Restricted to `app.current_org_id`.
-- Admin: 'admin_user' can see everything.
DROP POLICY IF EXISTS policy_select_organizations ON organizations;
CREATE POLICY policy_select_organizations ON organizations
    FOR SELECT USING (
        current_user = 'admin_user' 
        OR 
        id = current_setting('app.current_org_id', true)::INTEGER
    );

-- Policy: Update
-- Standard user: Restricted to `app.current_org_id`.
-- Admin: 'admin_user' can update everything.
DROP POLICY IF EXISTS policy_update_organizations ON organizations;
CREATE POLICY policy_update_organizations ON organizations
    FOR UPDATE USING (
        current_user = 'admin_user' 
        OR 
        id = current_setting('app.current_org_id', true)::INTEGER
    );

-- Policy: Delete (Hard Delete Protection)
-- Standard user: Forbidden (USING false).
-- Admin: 'admin_user' Allowed (USING true).
DROP POLICY IF EXISTS policy_delete_organizations ON organizations;
CREATE POLICY policy_delete_organizations ON organizations
    FOR DELETE USING (
        current_user = 'admin_user'
    );

-- 7. Row Level Security (RLS): Audit Table
-- Enforce Append-Only nature of the audit log to ensure immutability.
ALTER TABLE organizations_audit ENABLE ROW LEVEL SECURITY;

-- Allow inserts (needed by the trigger, assuming trigger runs as owner, but explicit policy is safer)
DROP POLICY IF EXISTS policy_insert_audit ON organizations_audit;
CREATE POLICY policy_insert_audit ON organizations_audit
    FOR INSERT WITH CHECK (true);

-- Allow Select: Restricted to `app.current_org_id` owner OR `admin_user`
DROP POLICY IF EXISTS policy_select_audit ON organizations_audit;
CREATE POLICY policy_select_audit ON organizations_audit
    FOR SELECT USING (
        current_user = 'admin_user' 
        OR 
        organization_id = current_setting('app.current_org_id', true)::INTEGER
    ); 

-- Block Updates entirely (Append-Only) unless admin
DROP POLICY IF EXISTS policy_update_audit ON organizations_audit;
CREATE POLICY policy_update_audit ON organizations_audit
    FOR UPDATE USING (current_user = 'admin_user');

-- Block Deletes entirely (Append-Only) unless admin
DROP POLICY IF EXISTS policy_delete_audit ON organizations_audit;
CREATE POLICY policy_delete_audit ON organizations_audit
    FOR DELETE USING (current_user = 'admin_user');

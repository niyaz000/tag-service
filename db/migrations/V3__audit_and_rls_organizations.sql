-- Create audit table for organizations
CREATE TABLE IF NOT EXISTS organizations_audit (
    audit_id SERIAL PRIMARY KEY,
    organization_id INTEGER NOT NULL,
    name VARCHAR(100),
    display_name VARCHAR(255),
    domain VARCHAR(255),
    version INTEGER,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    created_by VARCHAR(40),
    updated_by VARCHAR(40),
    deleted_at TIMESTAMPTZ,
    audit_action VARCHAR(10) NOT NULL, -- INSERT, UPDATE, DELETE, SOFT_DELETE
    audit_timestamp TIMESTAMPTZ DEFAULT NOW(),
    audit_user VARCHAR(40) -- User who performed the action
);

-- Trigger function for auditing
CREATE OR REPLACE FUNCTION audit_organizations_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO organizations_audit (
            organization_id, name, display_name, domain, version,
            created_at, updated_at, created_by, updated_by, deleted_at,
            audit_action, audit_user
        ) VALUES (
            NEW.id, NEW.name, NEW.display_name, NEW.domain, NEW.version,
            NEW.created_at, NEW.updated_at, NEW.created_by, NEW.updated_by, NEW.deleted_at,
            'INSERT', NEW.updated_by
        );
        RETURN NEW;
    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO organizations_audit (
            organization_id, name, display_name, domain, version,
            created_at, updated_at, created_by, updated_by, deleted_at,
            audit_action, audit_user
        ) VALUES (
            NEW.id, NEW.name, NEW.display_name, NEW.domain, NEW.version,
            NEW.created_at, NEW.updated_at, NEW.created_by, NEW.updated_by, NEW.deleted_at,
            CASE WHEN NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL THEN 'SOFT_DELETE' ELSE 'UPDATE' END,
            NEW.updated_by
        );
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        INSERT INTO organizations_audit (
            organization_id, name, display_name, domain, version,
            created_at, updated_at, created_by, updated_by, deleted_at,
            audit_action, audit_user
        ) VALUES (
            OLD.id, OLD.name, OLD.display_name, OLD.domain, OLD.version,
            OLD.created_at, OLD.updated_at, OLD.created_by, OLD.updated_by, OLD.deleted_at,
            'DELETE', OLD.updated_by
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Apply Audit Trigger
CREATE TRIGGER trigger_audit_organizations
AFTER INSERT OR UPDATE OR DELETE ON organizations
FOR EACH ROW EXECUTE FUNCTION audit_organizations_changes();

-- Enable RLS on organizations
ALTER TABLE organizations ENABLE ROW LEVEL SECURITY;

-- Note: Policies need to be defined based on application context (e.g., current_user or session variables)
-- Example (commented out):
-- CREATE POLICY tenant_isolation_policy ON organizations
-- USING (id = current_setting('app.current_tenant_id')::INTEGER);

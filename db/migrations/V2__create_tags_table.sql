-- Create tags table
CREATE TABLE IF NOT EXISTS tags (
    id SERIAL PRIMARY KEY,
    tenant_id INTEGER NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    key VARCHAR(100) NOT NULL,
    value TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Index for looking up tags by entity
CREATE INDEX IF NOT EXISTS idx_tags_entity ON tags(tenant_id, entity_type, entity_id);

-- Index for searching entities by tag
CREATE INDEX IF NOT EXISTS idx_tags_lookup ON tags(tenant_id, key, value);

-- Optional: Prevent duplicate keys for the same entity
-- CREATE UNIQUE INDEX idx_unique_entity_key ON tags(tenant_id, entity_type, entity_id, key);

# Tags as a Service (TaaS) - Design Document

## 1. Overview
A standalone REST API service designed to manage tags associated with arbitrary entities. This service allows decoupling tagging logic from core business logic, enabling flexible organization and categorization of resources across different systems.

## 2. Core Concepts

*   **Tenant**: The organization or account that owns the data. All data is isolated by `tenant_id`.
*   **Entity**: The object being tagged. It is uniquely identified by:
    *   `entityId`: The ID of the object in its host system (e.g., "user_123", "order_999").
    *   `entityType`: The category of the object (e.g., "user", "product", "invoice").
*   **Tag**: A key-value pair attached to an entity.
    *   `key`: The label category (e.g., "status", "priority", "department").
    *   `value`: (Optional) The specific value (e.g., "active", "high", "engineering").
    *   If no value is provided, the tag acts as a simple flag (e.g., "archived").

## 3. Data Model (SQL)

We will use PostgreSQL for robust data integrity and concurrency.

See [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) for the full schema definition.

### `tags` Table Summary
| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | SERIAL PRIMARY KEY | Auto-incrementing unique ID |
| `tenant_id` | VARCHAR(50) | Tenant ID (from X-Tenant-ID header) |
| `entity_type` | VARCHAR(50) | Usage: 'user', 'post' |
| `entity_id` | VARCHAR(255) | Usage: '101', 'uuid-v4' |
| `key` | VARCHAR(100) | The tag key |
| `value` | TEXT | The tag value |
| `created_at` | TIMESTAMPTZ | Creation timestamp |

**Indexes**:
- `(tenant_id, entity_type, entity_id)`
- `(tenant_id, key, value)`

## 4. API Endpoints

**Authentication/Context**: All requests must provide the **`X-Tenant-ID`** header to specify the tenant context.

### 4.1. Add Tag
Attach a tag to an entity. If the tag (key/value) already exists for that entity, this is idempotent.

*   **POST** `/api/tags`
*   **Body**:
    ```json
    {
      "entityType": "server",
      "entityId": "srv-01",
      "tags": [
        { "key": "env", "value": "prod" },
        { "key": "region", "value": "us-east-1" }
      ]
    }
    ```

### 4.2. Get Tags for Entity
Retrieve all tags associated with a specific entity.

*   **GET** `/api/tags/:entityType/:entityId`
*   **Response**:
    ```json
    {
      "entityType": "server",
      "entityId": "srv-01",
      "tags": [
        { "key": "env", "value": "prod" },
        { "key": "region", "value": "us-east-1" }
      ]
    }
    ```

### 4.3. Remove Tag
Remove a specific tag from an entity.

*   **DELETE** `/api/tags/:entityType/:entityId`
*   **Body**:
    ```json
    {
      "key": "env",
      "value": "prod" 
    }
    ```
    *(Note: If `value` is omitted, remove all tags with that key for the entity? No, typically explicit is better. We will require key and value for precise deletion, or just key to delete the category.)*
    *Refined Decision*: Query param `?key=env` to delete all 'env' tags, or specific match via API logic.

### 4.4. Search Entities
Find entities that match specific tags.

*   **GET** `/api/search`
*   **Query Params**: `?q=env:prod`
*   **Response**:
    ```json
    [
      { "entityType": "server", "entityId": "srv-01" },
      { "entityType": "server", "entityId": "srv-02" }
    ]
    ```

## 5. Technology Stack

*   **Framework**: Next.js (serving both the REST API routes and a simple Dashboard UI).
*   **Database**: PostgreSQL (using `pg` driver).
*   **Styling**: Vanilla CSS (CSS Modules).
*   **Language**: TypeScript.

## 6. Implementation Plan

1.  Initialize Next.js project.
2.  Set up PostgreSQL connection helper.
3.  Implement CRUD API handlers in `app/api/`.
4.  Create a minimal frontend to demo the service (Add tags/View tags).

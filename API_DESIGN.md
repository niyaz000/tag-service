# REST API Design: Organization Management

This document defines the REST API endpoints for managing Organizations in the Tag Service.

## Base URL
`/api/v1`

## Common Headers
*   `Content-Type`: `application/json`

---

## 1. Create Organization
Creates a new organization tenant.

- **Endpoint**: `POST /organizations`
- **Auth**: Admin only (or Open if self-sign-up is allowed)

**Request Body:**
```json
{
  "name": "acme-corp",
  "display_name": "Acme Corporation",
  "domain": "acme.com",
  "type": "enterprise",
  "settings": {
    "constraints": { ... },
    "features": { ... }
  }
}
```

**Response (201 Created):**
```json
{
  "id": 123,
  "name": "acme-corp",
  "display_name": "Acme Corporation",
  "domain": "acme.com",
  "type": "enterprise",
  "status": "active",
  "request_id": "c6a2...",
  "created_at": "2023-10-27T10:00:00Z"
}
```

---

## 2. Update Organization
Updates an existing organization's metadata or settings.

- **Endpoint**: `PUT /organizations/:id`
- **Auth**: Admin or Org Owner
- **RLS**: Enforced (Standard users can only update their own org).

**Request Body** (Partial updates allowed via PATCH, or full resource via PUT):
```json
{
  "display_name": "Acme Corp Global",
  "settings": {
    "features": {
       "colors_enabled": true
    }
  }
}
```

**Response (200 OK):**
```json
{
  "id": 123,
  "display_name": "Acme Corp Global",
  "version": 2,
  "updated_at": "..."
}
```

---

## 3. Delete Organization
Soft-deletes an organization.

- **Endpoint**: `DELETE /organizations/:id`
- **Auth**: Admin Only (Hard Delete) or Org Owner (Soft Delete if allowed)
- **Note**: Standard `DELETE` performs a Soft Delete (updates `deleted_at`). Hard delete requires specific Admin privileges or a query param like `?hard=true` (if implemented).

**Response (204 No Content)**

---

## 4. Fetch Organization by Id
Look up an organization by its id.

- **Endpoint**: `GET /organizations/:id`
- **Auth**: Authenticated User

**Response (200 OK):**
```json
{
  "id": 123,
  "name": "acme-corp",
  "display_name": "Acme Corporation",
  ...
}
```

**Response (404 Not Found)**

---

## 5. List All Organizations
Retrieve a paginated list of organizations.

- **Endpoint**: `GET /organizations`
- **Auth**: Admin Only (Standard users will only see their own single org due to RLS).
- **Query Params**:
    - `page`: Integer (default 1)
    - `limit`: Integer (default 20)
    - `type`: String (filter by type, e.g., 'enterprise')

**Response (200 OK):**
```json
{
  "data": [
    { "id": 1, "name": "org-a", ... },
    { "id": 2, "name": "org-b", ... }
  ],
  "meta": {
    "total": 2,
    "page": 1,
    "limit": 20
  }
}
```

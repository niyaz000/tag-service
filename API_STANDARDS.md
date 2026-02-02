# REST API Standards & Conventions

This document outlines the standard practices used across the Tag Service API.

## 1. Base URL
All API endpoints are prefixed with:
`https://api.tag-service.com/api/v1`

## 2. Authentication
The API supports the following authentication mechanism:

*   **Header Authentication**:
    *   `Authorization`: `Bearer <token>` (JWT or API Key)
    *   `X-Tenant-ID`: `Integer` (Required for tenant-specific operations. Maps to `organization.id`)

## 3. Request Headers
| Header | Required | Description |
| :--- | :--- | :--- |
| `Content-Type` | Yes | Must be `application/json` for bodies. |
| `Accept` | No | Defaults to `application/json`. |
| `X-Request-ID` | No | Custom UUID for tracing request lifecycles. |
| `X-Tenant-ID` | **Yes** | The Organization ID context for the request (for multi-tenancy). |

## 4. Response Headers
| Header | Description |
| :--- | :--- |
| `X-Request-ID` | The tracing ID (echoed back or generated). |
| `X-Execution-Time` | Time taken to process the request (ms). |
| `Content-Type` | `application/json; charset=utf-8` |

## 5. Error Handling

### Error Response Structure
All errors return a consistent JSON format:

```json
{
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "The requested organization was not found.",
    "trace_id": "c6a2e460-..."
  }
}
```

### Standard HTTP Status Codes

| Status | Code String | Meaning |
| :--- | :--- | :--- |
| **200** | `OK` | Request succeeded. |
| **201** | `CREATED` | Resource successfully created. |
| **204** | `NO_CONTENT` | Request succeeded, no content returned (e.g., DELETE). |
| **400** | `BAD_REQUEST` | Validation failed or malformed JSON. |
| **401** | `UNAUTHORIZED` | Missing or invalid authentication token. |
| **403** | `FORBIDDEN` | Authenticated, but insufficient permissions (e.g., restricted by RLS). |
| **404** | `NOT_FOUND` | Resource does not exist. |
| **409** | `CONFLICT` | Resource already exists or state conflict (e.g., duplicate domain). |
| **422** | `UNPROCESSABLE_ENTITY` | Semantic validation errors (e.g., invalid email format). |
| **429** | `TOO_MANY_REQUESTS` | Rate limit exceeded. |
| **500** | `INTERNAL_SERVER_ERROR` | Unexpected server error. |

---

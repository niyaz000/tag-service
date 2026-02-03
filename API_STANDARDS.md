# REST API Standards & Conventions

This document outlines the standard practices used across the Tag Service API.

## 1. Base URL
All API endpoints are prefixed with:
`https://api.<your domain>.tags-service.com/v1`

## 2. Authentication
The API supports the following authentication mechanism:

*   **Header Authentication**:
    *   `Authorization`: `Bearer <token>` (JWT or API Key)
    *   `X-Organization-ID`: `Integer` (Required for tenant-specific operations)

## 3. Request Headers
| Header | Required | Description |
| :--- | :--- | :--- |
| `Content-Type` | Yes | Must be `application/json` for bodies. |
| `Accept` | No | Defaults to `application/json`. |
| `X-Organization-ID` | **Yes** | The Organization ID context for the request (for multi-tenancy). |

## 4. Response Headers
| Header | Description |
| :--- | :--- |
| `X-Request-ID` | The tracing ID (echoed back or generated). |
| `Content-Type` | `application/json; charset=utf-8` |

## 5. Error Handling

### Error Response Structure
All errors return a consistent JSON format:

```json
{
  "type": "https://api.tag-service.com/errors#validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "One or more fields failed validation.",
  "instance": "/api/v1/organizations",
  "request_id": "c6a2e460-705a-471f-8c66-1234567890ab",
  "timestamp": "2026-02-03T11:55:00.000Z",
  "errors": [
    {
      "field": "display_name",
      "type": "https://api.tag-service.com/errors#duplicate-entity",
      "description": "Entity identified by 'display_name' with value 'Acme Corp' already exists."
    }
  ]
}
```

### Field Descriptions
*   `type`: URI reference identifying the error type (RFC 7807).
*   `title`: Short, human-readable summary of the error.
*   `status`: HTTP status code.
*   `detail`: Human-readable explanation specific to this occurrence.
*   `instance`: URI reference identifying the specific occurrence (request path).
*   `request_id`: Tracing ID for the request.
*   `timestamp`: ISO 8601 timestamp when the error occurred.
*   `errors`: (Optional) List of specific field validation errors.
    *   `field`: The field that caused the error.
    *   `type`: URI reference for the specific field error type.
    *   `description`: Detailed message for the field error.

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

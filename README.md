# Tag-as-a-Service (Tags Service) [WIP]

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/niyaz000/tag-service)
[![Java Version](https://img.shields.io/badge/java-21-blue)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/spring--boot-3.2.2-green)](https://spring.io/projects/spring-boot)

A high-performance, multi-tenant backend system designed to provide "Tagging as a Service". This project focuses on strict tenant isolation, enterprise-grade security, and robust observability.

---

## 🏗 Core Architecture

- **Multi-Tenancy:** Strict isolation using PostgreSQL Row-Level Security (RLS) and schema-based constraints.
- **Request Tracing:** End-to-end request tracking via mandatory `X-Request-ID` headers and MDC logging.
- **Tenant Context:** Mandatory `X-Organization-Id` enforcement at the gateway/middleware level.
- **Standardized Errors:** RFC 7807 compliant error responses for consistent client-side handling.

---

## 🛠 Tech Stack

| Component          | Technology                                         |
|-------------------|----------------------------------------------------|
| **Runtime**       | Java 21 (LTS)                                      |
| **Framework**     | Spring Boot 3.2.2                                  |
| **Database**      | PostgreSQL (with RLS)                              |
| **Migrations**    | Flyway / SQL Automations                          |
| **Utilities**     | Apache Commons Lang3, Lombok                       |
| **API**           | RESTful, JSON API                                 |
| **Observability** | Prometheus (Metrics), Sentry (Logging - Planned)   |

---

## � Documentation

Detailed designs and standards used in this project:

- [📘 Design Overview](./DESIGN.md) - Architectural vision and system components.
- [📙 API Design](./API_DESIGN.md) - Endpoint definitions and resource models.
- [📒 API Standards](./API_STANDARDS.md) - Error formats, headers, and status codes.
- [📗 Database Schema](./DATABASE_SCHEMA.md) - Tables, RLS policies, and indexing strategy.

---

## 🗺 Roadmap & Progress

### Phase 1: Foundation & Security
- [x] **Database Architecture:** RLS policies, migrations, and auditing.
- [x] **Middleware Layer:**
    - [x] `X-Request-ID` generation and validation.
    - [x] `X-Organization-Id` enforcement.
    - [x] Validation utility centralization (using Apache Commons).
- [x] **Error Handling:** Standardized error DTOs and JSON response format.

### Phase 2: Domain Implementation
- [ ] **Organization Management:** CRUD for organizations and tenant settings.
- [ ] **Tagging Engine:** Performant tag assignment, search, and categorization.
- [ ] **Soft Deletes:** Logic for data retention and recovery.

### Phase 3: Observability & Scale
- [ ] **Metrics:** Custom Prometheus metrics for tagging latency.
- [ ] **Performance:** JMeter benchmarking and query optimization.
- [ ] **Cloud:** AWS deployment scripts and CI/CD pipelines.

---

## � Getting Started

### Prerequisites
- JDK 21
- Maven 3.8+
- PostgreSQL (Running on 5432)

### Build & Compile
```bash
cd backend
mvn compile
```

### API Standards Quick Reference
All requests must include:
- `X-Request-ID`: Client or server-generated UUID (validated by `RequestIdFilter`).
- `X-Organization-Id`: Mandatory for all domain endpoints (validated by `OrganizationIdFilter`).

---

## 🧪 Testing
The project uses a mix of Unit and Integration tests.
- **Unit Tests:** `mvn test`
- **Integration Tests:** (Planned)
- **Performance:** JMeter test plans in `/docs/performance` (Planned)

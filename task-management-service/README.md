# Task Management Service

Spring Boot module that provides JWT authentication, RBAC, task workflow, and audit tracking.

## Features
- JWT auth (`/api/auth/register`, `/api/auth/login`)
- RBAC with `ROLE_USER` and `ROLE_ADMIN`
- Task lifecycle: `PENDING -> IN_PROGRESS -> COMPLETED -> APPROVED/REJECTED`
- Soft delete for tasks
- Audit fields auto-populated via JPA auditing (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`)
- Optional task comments

## Default Runtime
- Port: `8105`
- H2 console: `http://localhost:8105/h2-console`
- Swagger UI: `http://localhost:8105/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8105/v3/api-docs`
- Default admin user:
  - username: `admin`
  - password: `admin12345`

## Quick Start
```powershell
mvn -pl task-management-service spring-boot:run
```

## Key Endpoints
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/users/me`
- `GET /api/users` (ADMIN)
- `POST /api/users` (ADMIN, create user with role)
- `PATCH /api/users/{id}/status` (ADMIN)
- `POST /api/tasks`
- `GET /api/tasks?status=PENDING&page=0&size=10`
- `PATCH /api/tasks/{id}/status`
- `POST /api/tasks/{id}/comments`

## Security Notes
- JWT includes `roles` claim.
- Authorization is enforced with Spring Security and `@PreAuthorize` for admin-only user management APIs.
- Task ownership checks are enforced in the service layer to prevent cross-user data access.

## DB Schema
See `task-management-service/schema.sql`.


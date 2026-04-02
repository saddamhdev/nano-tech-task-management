# API Documentation

The task management service exposes interactive API documentation through Swagger/OpenAPI.

## Documentation URLs
- Swagger UI: `http://localhost:8105/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8105/v3/api-docs`

## Security
The API uses JWT bearer authentication. Include the access token in the `Authorization` header:

```text
Authorization: Bearer <JWT_TOKEN>
```

## API Groups

### Authentication APIs
- `POST /api/auth/register`
- `POST /api/auth/login`

### User APIs
- `GET /api/users/me`
- `GET /api/users` (ADMIN)
- `POST /api/users` (ADMIN)
- `PATCH /api/users/{id}/status` (ADMIN)

### Task APIs
- `POST /api/tasks`
- `GET /api/tasks`
- `GET /api/tasks/{id}`
- `PUT /api/tasks/{id}`
- `PATCH /api/tasks/{id}/status`
- `DELETE /api/tasks/{id}`
- `POST /api/tasks/{id}/comments`
- `GET /api/tasks/{id}/comments`

## Gateway Access
The gateway can forward task-management requests through rewritten routes:
- `/api/task-auth/**` → `/api/auth/**`
- `/api/task-users/**` → `/api/users/**`
- `/api/tasks/**` → task management service passthrough


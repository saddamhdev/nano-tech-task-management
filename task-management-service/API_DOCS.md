# API Documentation (Task Management Service)

## Auth
### Register
`POST /api/auth/register`
```json
{
  "username": "alice",
  "password": "password123"
}
```

### Login
`POST /api/auth/login`
```json
{
  "username": "alice",
  "password": "password123"
}
```

Response:
```json
{
  "token": "<jwt>",
  "username": "alice",
  "role": "ROLE_USER"
}
```

## User
### My Profile
`GET /api/users/me`

### Admin: List Users
`GET /api/users`

### Admin: Create User With Role
`POST /api/users`
```json
{
  "username": "bob",
  "password": "password123",
  "role": "ROLE_ADMIN",
  "active": true
}
```

### Admin: Activate/Deactivate User
`PATCH /api/users/{id}/status`
```json
{
  "active": false
}
```

## Task
### Create Task
`POST /api/tasks`
```json
{
  "title": "Write release notes",
  "description": "Prepare release notes for sprint 13"
}
```

### List Tasks
`GET /api/tasks?page=0&size=10&status=PENDING`

### Update Task
`PUT /api/tasks/{id}`

### Change Status
`PATCH /api/tasks/{id}/status`
```json
{
  "status": "COMPLETED"
}
```

### Soft Delete
`DELETE /api/tasks/{id}`

### Comments
- `POST /api/tasks/{id}/comments`
- `GET /api/tasks/{id}/comments`

## Workflow Rules
- USER:
  - `PENDING -> IN_PROGRESS`
  - `IN_PROGRESS -> COMPLETED`
- ADMIN:
  - `COMPLETED -> APPROVED`
  - `COMPLETED -> REJECTED`
- Any invalid transition returns `400 Bad Request`.


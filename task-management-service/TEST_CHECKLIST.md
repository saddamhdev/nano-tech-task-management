# Task Management Service - Full Requirement Test Checklist

This checklist is for manual testing in **Swagger UI** for `task-management-service`.

## 0. Test Setup
- [ ] Start the service
  ```powershell
  Set-Location "D:\module project\base"
  mvn -pl task-management-service spring-boot:run
  ```
- [ ] Open Swagger UI: `http://localhost:8105/swagger-ui.html`
- [ ] Confirm H2 console is available: `http://localhost:8105/h2-console`
- [ ] Confirm seeded admin exists:
  - username: `admin`
  - password: `admin12345`
- [ ] Confirm public auth endpoints are visible in Swagger:
  - `POST /api/auth/register`
  - `POST /api/auth/login`

---

## 1. Authentication Requirements

### 1.1 Register a new user
- [ ] Call `POST /api/auth/register`
- [ ] Request body:
  ```json
  {
    "username": "alice",
    "password": "password123"
  }
  ```
- [ ] Expected status: `201 Created`
- [ ] Verify response includes:
  - token
  - username = `alice`
  - role = `ROLE_USER`

### 1.2 Duplicate registration is rejected
- [ ] Call `POST /api/auth/register` again with the same username
- [ ] Expected status: `400 Bad Request`
- [ ] Expected message mentions username already exists

### 1.3 Login with valid credentials
- [ ] Call `POST /api/auth/login`
- [ ] Use the same user credentials
- [ ] Expected status: `200 OK`
- [ ] Verify token is returned

### 1.4 Login with invalid credentials
- [ ] Call `POST /api/auth/login` with wrong password
- [ ] Expected status: `400 Bad Request`
- [ ] Expected message indicates invalid credentials

### 1.5 Passwords are stored encrypted
- [ ] Open H2 console
- [ ] Inspect `app_user` table
- [ ] Verify password is not plain text
- [ ] Verify password looks BCrypt-hashed

### 1.6 JWT is required for protected endpoints
- [ ] Call a protected endpoint without token, for example `GET /api/users/me`
- [ ] Expected status: `401 Unauthorized` or `403 Forbidden` depending on Swagger/client behavior

---

## 2. JWT and Security Enforcement

### 2.1 JWT token works for authenticated requests
- [ ] Copy token from login response
- [ ] Click **Authorize** in Swagger UI
- [ ] Paste token
- [ ] Authorize successfully
- [ ] Call `GET /api/users/me`
- [ ] Expected status: `200 OK`

### 2.2 Invalid JWT is rejected
- [ ] Replace token with an invalid string
- [ ] Call a protected endpoint
- [ ] Expected status: `401 Unauthorized` or `403 Forbidden`

### 2.3 Role is extracted from JWT
- [ ] Login as admin
- [ ] Verify response role is `ROLE_ADMIN`
- [ ] Call admin-only endpoints successfully

### 2.4 Public endpoints remain accessible
- [ ] Verify `POST /api/auth/register` is accessible without login
- [ ] Verify `POST /api/auth/login` is accessible without login

---

## 3. User Management - ADMIN Only

### 3.1 Admin can view all users
- [ ] Login as admin
- [ ] Call `GET /api/users`
- [ ] Expected status: `200 OK`
- [ ] Verify all users are listed

### 3.2 Admin can create a user with role selection
- [ ] Call `POST /api/users`
- [ ] Request body:
  ```json
  {
    "username": "bob",
    "password": "password123",
    "role": "ROLE_USER",
    "active": true
  }
  ```
- [ ] Expected status: `201 Created`
- [ ] Verify response role = `ROLE_USER`

### 3.3 Admin can create another admin
- [ ] Call `POST /api/users`
- [ ] Request body:
  ```json
  {
    "username": "manager1",
    "password": "password123",
    "role": "ROLE_ADMIN",
    "active": true
  }
  ```
- [ ] Expected status: `201 Created`
- [ ] Verify response role = `ROLE_ADMIN`

### 3.4 Admin can activate/deactivate a user
- [ ] Find a user id from `GET /api/users`
- [ ] Call `PATCH /api/users/{id}/status`
- [ ] Request body:
  ```json
  {
    "active": false
  }
  ```
- [ ] Expected status: `200 OK`
- [ ] Verify user active flag becomes false

### 3.5 Admin-only access is enforced
- [ ] Login as normal user
- [ ] Call `GET /api/users`
- [ ] Expected status: `403 Forbidden`
- [ ] Call `POST /api/users`
- [ ] Expected status: `403 Forbidden`
- [ ] Call `PATCH /api/users/{id}/status`
- [ ] Expected status: `403 Forbidden`

### 3.6 My profile endpoint works for user
- [ ] Login as normal user
- [ ] Call `GET /api/users/me`
- [ ] Expected status: `200 OK`
- [ ] Verify response username matches logged-in user

---

## 4. Task Creation and CRUD

### 4.1 User can create a task
- [ ] Login as normal user
- [ ] Call `POST /api/tasks`
- [ ] Request body:
  ```json
  {
    "title": "Write release notes",
    "description": "Prepare release notes for sprint 13"
  }
  ```
- [ ] Expected status: `201 Created`
- [ ] Save the returned task id

### 4.2 User can view own task
- [ ] Call `GET /api/tasks/{id}`
- [ ] Expected status: `200 OK`
- [ ] Verify owner is the current user

### 4.3 User can update own task
- [ ] Call `PUT /api/tasks/{id}`
- [ ] Request body:
  ```json
  {
    "title": "Write release notes v2",
    "description": "Updated description"
  }
  ```
- [ ] Expected status: `200 OK`

### 4.4 User can soft-delete own task
- [ ] Call `DELETE /api/tasks/{id}`
- [ ] Expected status: `204 No Content`

### 4.5 Soft-deleted task is hidden
- [ ] Try `GET /api/tasks/{id}` after delete
- [ ] Expected status: `404 Not Found`

### 4.6 Admin can view all tasks
- [ ] Login as admin
- [ ] Call `GET /api/tasks`
- [ ] Expected status: `200 OK`
- [ ] Verify tasks from all users are visible

### 4.7 Pagination works
- [ ] Call `GET /api/tasks?page=0&size=10`
- [ ] Expected status: `200 OK`
- [ ] Verify paged response includes content, page, size, totalElements, totalPages

### 4.8 Filtering works
- [ ] Call `GET /api/tasks?page=0&size=10&status=PENDING`
- [ ] Expected status: `200 OK`
- [ ] Verify only tasks with `PENDING` status are returned

---

## 5. Ownership Restrictions

### 5.1 User cannot access another user's task
- [ ] Create a task as user A
- [ ] Login as user B
- [ ] Call `GET /api/tasks/{id}` on user A's task
- [ ] Expected status: `403 Forbidden`

### 5.2 User cannot update another user's task
- [ ] As user B, call `PUT /api/tasks/{id}` on user A's task
- [ ] Expected status: `403 Forbidden`

### 5.3 User cannot delete another user's task
- [ ] As user B, call `DELETE /api/tasks/{id}` on user A's task
- [ ] Expected status: `403 Forbidden`

---

## 6. Task Workflow and Approval

### 6.1 User valid workflow transition
- [ ] Create a fresh task
- [ ] Call `PATCH /api/tasks/{id}/status` with:
  ```json
  { "status": "IN_PROGRESS" }
  ```
- [ ] Expected status: `200 OK`
- [ ] Call again with:
  ```json
  { "status": "COMPLETED" }
  ```
- [ ] Expected status: `200 OK`

### 6.2 User invalid workflow transition is rejected
- [ ] On a new task, try to set status directly to `APPROVED`
- [ ] Expected status: `400 Bad Request`
- [ ] Expected message indicates invalid transition

### 6.3 Admin can approve only completed tasks
- [ ] Login as admin
- [ ] Pick a task with status `COMPLETED`
- [ ] Call `PATCH /api/tasks/{id}/status`
- [ ] Request body:
  ```json
  { "status": "APPROVED" }
  ```
- [ ] Expected status: `200 OK`

### 6.4 Admin can reject only completed tasks
- [ ] Pick another completed task
- [ ] Call `PATCH /api/tasks/{id}/status`
- [ ] Request body:
  ```json
  { "status": "REJECTED" }
  ```
- [ ] Expected status: `200 OK`

### 6.5 Admin invalid approval is rejected
- [ ] Try approving a task that is not `COMPLETED`
- [ ] Expected status: `400 Bad Request`
- [ ] Expected message indicates admin can only approve/reject completed tasks

---

## 7. Comments Requirement

### 7.1 User can add a comment
- [ ] Call `POST /api/tasks/{id}/comments`
- [ ] Request body:
  ```json
  {
    "content": "Please review this task"
  }
  ```
- [ ] Expected status: `201 Created`

### 7.2 User can list comments
- [ ] Call `GET /api/tasks/{id}/comments`
- [ ] Expected status: `200 OK`
- [ ] Verify comment author and timestamp are present

### 7.3 Comment access respects ownership
- [ ] Login as another user
- [ ] Try `GET /api/tasks/{id}/comments` on a task not owned by you
- [ ] Expected status: `403 Forbidden`

---

## 8. Audit Fields Requirement

### 8.1 Audit fields appear on task response
- [ ] Create a task
- [ ] Verify response includes:
  - `createdAt`
  - `updatedAt`
  - `createdBy`
  - `updatedBy`

### 8.2 Audit fields update after modification
- [ ] Update the same task
- [ ] Verify `updatedAt` changes
- [ ] Verify `updatedBy` matches the user who made the change

### 8.3 Audit fields appear for comments
- [ ] Add a comment
- [ ] Verify comment response includes timestamp
- [ ] Verify author name is included

### 8.4 Audit data is auto-populated
- [ ] Confirm no manual audit fields are passed in request bodies
- [ ] Confirm values are filled by JPA auditing / authenticated user context

---

## 9. Swagger / API Documentation Requirement

### 9.1 Swagger UI is available
- [ ] Open Swagger UI successfully
- [ ] Verify all endpoints are visible

### 9.2 Swagger authentication works
- [ ] Use the **Authorize** button
- [ ] Paste the JWT token
- [ ] Verify protected endpoints can be executed

### 9.3 Path and query parameters are visible correctly
- [ ] `PATCH /api/users/{id}/status` shows `id`
- [ ] `GET /api/tasks` shows `page`, `size`, `status`

---

## 10. Negative Security Tests

### 10.1 Missing token is blocked
- [ ] Call protected endpoint without token
- [ ] Expected status: `401` or `403`

### 10.2 Expired or invalid token is blocked
- [ ] Use invalid JWT
- [ ] Expected status: `401` or `403`

### 10.3 Non-admin cannot call admin endpoints
- [ ] As normal user, call:
  - `GET /api/users`
  - `POST /api/users`
  - `PATCH /api/users/{id}/status`
- [ ] Expected status: `403 Forbidden`

### 10.4 Admin cannot bypass workflow rules
- [ ] As admin, try approving a non-completed task
- [ ] Expected status: `400 Bad Request`

---

## 11. First Admin Bootstrap Check
- [ ] Confirm the app starts with a seeded admin user
- [ ] Login using:
  - username: `admin`
  - password: `admin12345`
- [ ] Confirm admin can then create additional users and admins through `POST /api/users`

---

## 12. Completion Criteria
- [ ] All assignment requirements have a passing test case
- [ ] All expected security failures are confirmed
- [ ] Swagger UI can be used to reproduce the tests
- [ ] Screenshots or response logs are collected for submission

---

## Notes
- Public signup creates `ROLE_USER` only.
- Admin-only `POST /api/users` can create `ROLE_USER` or `ROLE_ADMIN`.
- `PATCH /api/users/{id}/status` is admin-only.
- `PATCH /api/tasks/{id}/status` follows the workflow rules exactly.


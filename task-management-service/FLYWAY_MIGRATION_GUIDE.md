# Flyway Database Migration Configuration

## Overview

This document describes the Flyway database migration setup for the task-management-service.

Flyway is used to manage database schema versioning and evolution in a controlled, trackable manner.

## Key Benefits

- **Version Control**: Database schema changes are tracked with version numbers
- **Reproducibility**: Migrations can be applied consistently across environments
- **Rollback Safety**: Clean separation between migrations makes it easier to reason about schema changes
- **Integration**: Works seamlessly with Spring Boot and Hibernate

## File Structure

```
src/main/resources/db/migration/
├── V1__Initial_schema.sql       # Initial database schema (tables, indexes)
└── V2__Seed_admin_user.sql      # Seed default admin user
```

## Migration Files

### V1__Initial_schema.sql

Creates the core tables for the application:
- `app_user` - User accounts with role-based access control
- `task` - Task records with ownership and status tracking
- `task_comment` - Comments on tasks with author tracking

Also creates indexes for optimized query performance.

### V2__Seed_admin_user.sql

Seeds the initial admin user:
- **Username**: `admin`
- **Password**: `admin12345` (BCrypt hashed)
- **Role**: `ROLE_ADMIN`

## Naming Convention

Flyway uses the following naming convention for migrations:

```
V{version}__{description}.sql
```

Where:
- `V` prefix indicates a versioned migration
- `{version}` is a numeric version (1, 2, 3, etc.)
- `__` (double underscore) separates version and description
- `{description}` is a human-readable description (underscores instead of spaces)

## Configuration

### application.yml

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baselineOnMigrate: true
```

### Hibernate Configuration

Set `ddl-auto` to `validate` to prevent Hibernate from creating tables:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

This ensures Flyway is the sole authority for schema management.

## Migration Process

1. **Startup**: Spring Boot detects migration files in `src/main/resources/db/migration/`
2. **Validation**: Flyway compares file hashes to detect any manual changes
3. **Execution**: Unmigrated files are executed in version order
4. **Tracking**: `flyway_schema_history` table records all migrations
5. **Validation**: Hibernate validates schema matches entity annotations

## Adding New Migrations

When schema changes are needed:

1. Create a new file: `src/main/resources/db/migration/V{N}__{description}.sql`
2. Increment the version number from the last migration
3. Write the SQL changes
4. Restart the application

**Important**: Never modify existing migration files. Always create new ones.

### Example: Adding a Column

```sql
-- V3__Add_priority_to_task.sql
ALTER TABLE task ADD COLUMN priority VARCHAR(10) DEFAULT 'MEDIUM' NOT NULL;
```

## Checking Migration Status

### H2 Console

View migration history in the `flyway_schema_history` table:

```
SELECT * FROM flyway_schema_history;
```

This shows:
- Version
- Description
- Type (SQL, JDBC, etc.)
- Installation date
- Execution time
- Success status

## Troubleshooting

### Migration Already Exists Error

If you see `FlywayMigrationScriptMissingException`:
- A migration file was deleted or renamed
- Never delete migration files; create a new one to undo the change

### Schema Validation Fails

If Hibernate validation fails:
1. Ensure `ddl-auto: validate` in application.yml
2. Verify entity annotations match the database schema
3. Check the flyway_schema_history table for successful migrations

### Reset for Development

To reset the database (development only):

1. Delete the database file: `rm task-management.mv.db`
2. Restart the application
3. Flyway will re-run all migrations

## Integration with Spring Boot

- Flyway is auto-configured when `org.flywaydb:flyway-core` is on the classpath
- Migrations run automatically on application startup
- No additional code needed; configuration in `application.yml` is sufficient

## References

- [Flyway Documentation](https://flywaydb.org/)
- [Spring Boot Flyway Integration](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto.data-initialization.migration-tool.flyway)


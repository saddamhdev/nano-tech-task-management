INSERT INTO app_user (username, password, role, active, created_by, updated_by)
SELECT
    'admin',
    '$2a$10$sL.lcy7uoy0YnqiRElepeeeeJp7JKaZRajc6OCcM7wtg2dnSk.WAS',
    'ROLE_ADMIN',
    TRUE,
    'system',
    'system'
WHERE NOT EXISTS (
    SELECT 1
    FROM app_user
    WHERE username = 'admin'
);


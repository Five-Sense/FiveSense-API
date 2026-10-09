-- Nome de usuario usado no login. Usuarios existentes recebem um valor derivado do e-mail.
ALTER TABLE app_user ADD COLUMN username VARCHAR(255);

UPDATE app_user
SET username = LOWER(split_part(email, '@', 1)) || '-' || SUBSTRING(id::text FROM 1 FOR 8)
WHERE username IS NULL;

ALTER TABLE app_user ALTER COLUMN username SET NOT NULL;
ALTER TABLE app_user ADD CONSTRAINT uq_app_user_username UNIQUE (username);

-- Admin padrao (avaliacao/estudo): senha em texto puro, conforme decisao 0005.
INSERT INTO app_user (id, name, username, email, password, role, status)
VALUES (gen_random_uuid(), 'admin', 'admin', 'admin@gmail.com', 'admin123', 'ADMIN', 'ACTIVE')
ON CONFLICT DO NOTHING;

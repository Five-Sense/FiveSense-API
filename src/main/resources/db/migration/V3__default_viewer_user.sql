-- Usuario visualizador padrao (avaliacao/estudo): senha em texto puro, conforme decisao 0005.
INSERT INTO app_user (id, name, username, email, password, role, status)
VALUES (gen_random_uuid(), 'Visualizador', 'visualizador', 'visualizador@gmail.com', 'viewer123', 'VIEWER', 'ACTIVE')
ON CONFLICT DO NOTHING;

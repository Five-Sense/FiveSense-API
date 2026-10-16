-- Equipes de exemplo do contexto 5S. Representantes sao nomes ficticios em texto simples.
INSERT INTO team (id, name, code, representatives, schedule, status) VALUES
    (gen_random_uuid(), 'Produção A', 'PROD-A', 'Carlos Menezes, Juliana Ribeiro', 'Seg a Sex, 07:00 - 09:00', 'DOING_5S'),
    (gen_random_uuid(), 'Produção B', 'PROD-B', 'Rafael Tavares, Patrícia Lima', 'Seg a Sex, 09:00 - 11:00', 'NOT_DOING_5S'),
    (gen_random_uuid(), 'Almoxarifado', 'ALMOX', 'Marcos Andrade, Fernanda Souza', 'Ter e Qui, 13:00 - 15:00', 'NOT_DOING_5S'),
    (gen_random_uuid(), 'Manutenção', 'MANUT', 'Eduardo Pacheco, Camila Duarte', 'Seg, Qua e Sex, 14:00 - 16:00', 'DOING_5S'),
    (gen_random_uuid(), 'Expedição', 'EXPED', 'Thiago Barros, Larissa Mendes', 'Seg a Sex, 16:00 - 17:00', 'NOT_DOING_5S')
ON CONFLICT DO NOTHING;

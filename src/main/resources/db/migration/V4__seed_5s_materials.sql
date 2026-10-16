-- Materiais de limpeza usados nas atividades 5S, com estoque inicial e limite minimo.
INSERT INTO material (id, name, stock_quantity, minimum_stock, active) VALUES
    (gen_random_uuid(), 'Panos reutilizáveis', 10, 3, TRUE),
    (gen_random_uuid(), 'Pás de sujeira', 5, 2, TRUE),
    (gen_random_uuid(), 'Vassouras', 10, 3, TRUE),
    (gen_random_uuid(), 'Borrifadores de álcool', 15, 5, TRUE),
    (gen_random_uuid(), 'Estopas', 50, 15, TRUE)
ON CONFLICT DO NOTHING;

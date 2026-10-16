-- Problemas comuns relacionados aos materiais 5S (nome limitado a 20 caracteres).
-- related_email recebe a resposta padrao quando uma ocorrencia e registrada.
INSERT INTO problem (id, name, related_email, default_response, active) VALUES
    (gen_random_uuid(), 'Vassoura quebrada', 'admin@gmail.com', 'Recebemos o aviso de vassoura quebrada. A reposição será providenciada.', TRUE),
    (gen_random_uuid(), 'Vassoura sem cerdas', 'admin@gmail.com', 'Recebemos o aviso de vassoura sem cerdas. Será feita a troca.', TRUE),
    (gen_random_uuid(), 'Borrifador vazando', 'admin@gmail.com', 'Recebemos o aviso de borrifador vazando. Descarte o item e aguarde a reposição.', TRUE),
    (gen_random_uuid(), 'Borrifador vazio', 'admin@gmail.com', 'Recebemos o aviso de borrifador vazio. O álcool será reabastecido.', TRUE),
    (gen_random_uuid(), 'Borrifador entupido', 'admin@gmail.com', 'Recebemos o aviso de borrifador entupido. O bico será limpo ou trocado.', TRUE),
    (gen_random_uuid(), 'Pá quebrada', 'admin@gmail.com', 'Recebemos o aviso de pá de sujeira quebrada. A reposição será providenciada.', TRUE),
    (gen_random_uuid(), 'Pá cabo solto', 'admin@gmail.com', 'Recebemos o aviso de pá com cabo solto. O item será consertado ou trocado.', TRUE),
    (gen_random_uuid(), 'Pano rasgado', 'admin@gmail.com', 'Recebemos o aviso de pano rasgado. Será feita a substituição.', TRUE),
    (gen_random_uuid(), 'Pano sujo', 'admin@gmail.com', 'Recebemos o aviso de pano sujo. Será feita a lavagem ou troca.', TRUE),
    (gen_random_uuid(), 'Estopa molhada', 'admin@gmail.com', 'Recebemos o aviso de estopa molhada. Será feita a reposição por estopas secas.', TRUE)
ON CONFLICT DO NOTHING;

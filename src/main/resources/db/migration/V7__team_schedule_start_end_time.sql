-- O horario da equipe passa a ser um intervalo: hora de inicio e hora de fim (decisao 0011).
ALTER TABLE team ADD COLUMN start_time TIME;
ALTER TABLE team ADD COLUMN end_time TIME;

-- Aproveita o texto antigo quando ele traz um intervalo "HH:mm - HH:mm", como nas equipes de exemplo da V5
-- ("Seg a Sex, 07:00 - 09:00"). Os dias da semana e qualquer outro texto (ex.: "Turno manha") sao descartados.
WITH parsed AS (
    SELECT id, regexp_match(schedule, '((?:[01]?[0-9]|2[0-3]):[0-5][0-9])\s*-\s*((?:[01]?[0-9]|2[0-3]):[0-5][0-9])') AS m
    FROM team
)
UPDATE team t
SET start_time = p.m[1]::time, end_time = p.m[2]::time, updated_at = CURRENT_TIMESTAMP
FROM parsed p
WHERE t.id = p.id AND p.m IS NOT NULL AND p.m[2]::time > p.m[1]::time;

ALTER TABLE team DROP COLUMN schedule;

-- Os dois horarios juntos ou nenhum, e o fim depois do inicio.
ALTER TABLE team ADD CONSTRAINT ck_team_time_interval CHECK (
    (start_time IS NULL AND end_time IS NULL)
    OR (start_time IS NOT NULL AND end_time IS NOT NULL AND end_time > start_time)
);

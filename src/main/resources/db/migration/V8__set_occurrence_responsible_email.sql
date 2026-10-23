-- Reconfigura o e-mail do responsavel que recebe a notificacao quando uma
-- ocorrencia e lancada por um visualizador.
--
-- Dois destinos recebem o e-mail de ocorrencia em OccurrenceService.create:
--   1. Usuarios ADMIN/MANAGER ativos (notificationRecipients()) -> app_user.email.
--   2. O responsavel do problema (problem.related_email), que recebe a resposta
--      padrao e a imagem anexada.
--
-- As migracoes V2 e V6 ja foram aplicadas em producao; editar seus arquivos
-- quebraria o checksum do Flyway. Por isso, atualizamos as linhas existentes
-- aqui para que ambos os destinos usem crisjesusdofigmamobile@gmail.com.

-- Responsavel de todos os problemas (email em caixa baixa, como o dominio normaliza).
UPDATE problem
SET related_email = 'crisjesusdofigmamobile@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE related_email <> 'crisjesusdofigmamobile@gmail.com';

-- Admin padrao: passa a receber a notificacao ADMIN/MANAGER no mesmo endereco.
UPDATE app_user
SET email = 'crisjesusdofigmamobile@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE username = 'admin'
  AND email = 'admin@gmail.com';

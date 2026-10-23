# Trabalho atual

## Concluído

- API REST para usuários, equipes, problemas, materiais e ocorrências.
- Login direto por e-mail e senha e cadastro simples de usuários.
- Site estático com documentação e navegação dos endpoints em `site/index.html`.
- Configuração padrão do PostgreSQL Supabase em `.env.example`.
- Schema Flyway inicial alinhado ao modelo atual da API.
- [2026-10-02] Imagem temporária no fluxo de ocorrência: upload em `POST /occurrences/images`, anexo no e-mail e exclusão após o envio; sem persistência no banco (decisão 0004).
- [2026-10-19] Várias imagens por ocorrência (até 5) e upload também como texto base64 (multipart ou JSON); anexos com extensão do tipo real; sem persistência no banco (decisão 0009).
- [2026-10-19] Imagens enviadas na própria ocorrência (binário ou base64, sem `imageId`), e-mail HTML com Thymeleaf, envio assíncrono com timeouts de SMTP e validação do usuário que reporta; corrige o carregamento infinito do app (decisão 0010).
- [2026-10-19] Horário da equipe passa a ser um intervalo (`startTime` e `endTime` em `HH:mm`, colunas `start_time`/`end_time`, migration V7, validação na API e CHECK no banco, exemplos do site corrigidos); decisão 0011.

- [2026-10-09] Login por `username`, admin padrão (migration V2), perfis ADMIN/MANAGER/VIEWER via header `X-User-Role` e `PATCH /materials/{id}/stock` (decisão 0005).
- [2026-10-09] Site `docs/site/index.html` revisado (login por username, forgot-password, perfis, headers, ajuste de estoque) e publicado em `/docs` (decisão 0006).
- [2026-10-16] Migrations `V3`–`V6`: usuário VIEWER padrão, materiais, equipes e problemas 5S (decisão 0007).
- [2026-10-23] Migration `V7`: `problem.related_email` (responsável da ocorrência) e e-mail do admin padrão passam a ser `crisjesusdofigmamobile@gmail.com`; `EmailService` com codificação MIME UTF-8 explícita; `.env.example`/`operations.md` com SMTP Gmail (porta 587, STARTTLS, senha de app).
## Pendente
- [ ] Executar `V3`–`V7` em PostgreSQL real (Docker indisponível na verificação) e confirmar os valores de mínimo de estoque e `related_email` escolhidos (V7 define o responsável como `crisjesusdofigmamobile@gmail.com`).
- [ ] Confirmar com o humano se VIEWER pode criar/editar equipes (hoje só altera o status).
- [ ] Teste de integração PostgreSQL/Testcontainers para a migration V2 e os fluxos de perfil (Docker indisponível na verificação).

- [ ] Preencher a senha do PostgreSQL Supabase no `.env` local e validar a conexão.
- [ ] Executar build usando Java 21.
- [ ] Validar as rotas com o cliente mobile.
- [ ] Configurar `MAIL_*` apenas se o ambiente de avaliação exigir envio de e-mail.

## Notas de verificação de e-mail (2026-10-02)

- Ocorrência: envia e-mail aos usuários ADMIN/MANAGER ativos e ao contato do problema (resposta padrão). Agora com anexo opcional de imagem. OK.
- Estoque: material criado abaixo do mínimo ou cruzando o mínimo dispara alerta aos ADMIN/MANAGER. OK.
- "Esqueci minha senha": não existe no escopo atual (sem endpoint, service ou requisito). Alterar o fluxo de login exige decisão humana (AGENTS.md). Nenhum envio foi adicionado; aguardando decisão caso o fluxo seja desejado.

- [2026-10-16] CORS liberado para http://localhost:8081 (decisão 0008). Pendente: validar com o cliente React Native real.

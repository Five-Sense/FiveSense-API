# Trabalho atual

## Concluído

- API REST para usuários, equipes, problemas, materiais e ocorrências.
- Login direto por e-mail e senha e cadastro simples de usuários.
- Site estático com documentação e navegação dos endpoints em `site/index.html`.
- Configuração padrão do PostgreSQL Supabase em `.env.example`.
- Schema Flyway inicial alinhado ao modelo atual da API.
- [2026-10-02] Imagem temporária no fluxo de ocorrência: upload em `POST /occurrences/images`, anexo no e-mail e exclusão após o envio; sem persistência no banco (decisão 0004).

## Pendente

- [ ] Preencher a senha do PostgreSQL Supabase no `.env` local e validar a conexão.
- [ ] Executar build usando Java 21.
- [ ] Validar as rotas com o cliente mobile.
- [ ] Configurar `MAIL_*` apenas se o ambiente de avaliação exigir envio de e-mail.

## Notas de verificação de e-mail (2026-10-02)

- Ocorrência: envia e-mail aos usuários ADMIN/MANAGER ativos e ao contato do problema (resposta padrão). Agora com anexo opcional de imagem. OK.
- Estoque: material criado abaixo do mínimo ou cruzando o mínimo dispara alerta aos ADMIN/MANAGER. OK.
- "Esqueci minha senha": não existe no escopo atual (sem endpoint, service ou requisito). Alterar o fluxo de login exige decisão humana (AGENTS.md). Nenhum envio foi adicionado; aguardando decisão caso o fluxo seja desejado.

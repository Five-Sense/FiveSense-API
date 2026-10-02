# Plano de implementação

## Estado

API de avaliação mobile implementada como monólito Spring MVC com PostgreSQL e documentação estática dos endpoints.

## Entregue

- Login por e-mail e senha com comparação direta e resposta com dados básicos do usuário.
- Cadastro de usuário ativo por `POST /api/v1/users`.
- Rotas abertas; o campo `role` é informativo.
- CRUD de equipes, problemas e materiais; criação e listagem de ocorrências.
- Flyway com schema inicial correspondente ao modelo atual.
- Configuração do Supabase em `.env.example` e carregamento local por `.env`.
- Referência pesquisável dos endpoints em `docs/site/index.html`.

## Pendências externas

- [ ] Informar `DATABASE_PASSWORD` no `.env` local e testar a conexão Supabase.
- [ ] Executar compilação usando Java 21.
- [ ] Fazer integração do cliente mobile e validar os contratos em `api-contract.md`.
- [ ] Configurar `MAIL_*` somente se o envio de e-mail for necessário.

# Five Sense API

API backend de avaliação mobile Five Sense para gerenciamento de atividades 5S. O login compara e-mail e senha cadastrados e as rotas ficam abertas.

Este projeto segue o framework SpecFirst: o escopo, os contratos e as decisões são documentados e aprovados antes da implementação.

## Estado atual

- API reconfigurada para avaliação mobile: login direto, nenhuma rota protegida; cadastro inicial via API.
- Conexão Supabase configurada por `.env`; preencher a senha do banco antes de iniciar.
- Veja `docs/issues.md`, `docs/implementation-plan.md` e `docs/deployment-log.md` para status e validações pendentes.

## Stack

- Java 21 e Spring Boot 4.1.1.
- Spring MVC, Spring Data JPA, Bean Validation e Spring Mail.
- PostgreSQL 18 e Flyway.
- MapStruct 1.6.3.
- Maven Wrapper.
- Docker.
- JUnit 5 e Mockito.

As dependências atualmente declaradas estão em `pom.xml`. O login compara diretamente a senha recebida com o campo `password` no PostgreSQL e retorna `userId`, nome, e-mail e papel. O papel é informativo.

## Arquitetura

O sistema será um monólito MVC simples. Pacotes por capacidade de negócio podem conter as pastas `controller`, `app`, `infra`, `dto` e `domain`; isso é apenas organização interna e não transforma o projeto em modular monolith.

Visão resumida:

```text
HTTP -> Controller/DTO -> App/Service/MapStruct -> Domain/Infra -> PostgreSQL ou integração externa
```

Consulte `docs/architecture.md` para dependências permitidas e `docs/domains.md` para as fronteiras funcionais.

## Escopo do MVP

- Login básico; `POST /api/v1/users` cadastra usuário com nome, e-mail, senha e papel.
- Usuários são criados como ativos; o papel é dado informativo.
- Usuários e papéis `ADMIN`, `MANAGER` e `VIEWER`.
- CRUD de problemas.
- Registro de ocorrências com problema, material, quantidade e notificação a todos os Admins e Gestores por e-mail; a quantidade não altera o estoque e imagens estão fora do escopo.
- CRUD de equipes, representantes textuais (até 255 caracteres), faixa/horário e status de execução do 5S.
- Consulta à organização dos horários de cada equipe (não é calendário de eventos).
- CRUD e consulta de materiais, quantidades e limites de estoque.
- Alertas de estoque e notificações por e-mail.
- Todas as rotas são públicas para facilitar integração durante a avaliação mobile.
- API documentada por OpenAPI.

O mapa completo de RF001-RF033, NF001-NF005 e RN001-RN029 está em `docs/requirements.md`.

## Documentação

- `AGENTS.md` - contrato obrigatório do projeto.
- `docs/README.md` - índice e ordem de leitura.
- `docs/project-overview.md` - objetivo, público e limites do MVP.
- `docs/requirements.md` - rastreabilidade dos requisitos e ambiguidades.
- `docs/api-contract.md` - rotas HTTP públicas e contratos.
- `docs/site/index.html` - site estático navegável para pesquisar e consultar exemplos dos endpoints.
- `docs/architecture.md` - arquitetura autorizada.
- `docs/data-model.md` - modelo de dados implementado.
- `docs/login.md` - cadastro e login usados nesta avaliação.
- `docs/testing.md` - estratégia de testes e fitness functions.
- `docs/implementation-plan.md` e `docs/issues.md` - sequência e estado do trabalho.

## Execução local

O `.env` local está preparado para conectar a API ao PostgreSQL Supabase configurado para o projeto; preencha `DATABASE_PASSWORD` com a senha do banco do painel Supabase. Cadastre o primeiro usuário via `POST /api/v1/users` e use `POST /api/v1/auth/login`. Consulte `docs/operations.md` para SSL e alternativa pooler. Os checks Maven disponíveis no Windows são:

```powershell
.\mvnw.cmd clean verify
```

Consulte `docs/operations.md` para configurar PostgreSQL e SMTP.

## Governança

Antes de implementar:

1. leia `AGENTS.md` e os documentos nucleares;
2. selecione uma issue aprovada;
3. confirme critérios de aceite e consulte decisões abertas somente se o escopo novo as reabrir;
4. implemente o menor incremento;
5. execute testes, métricas e fitness functions;
6. atualize issue, plano e log técnico.

Arquivos Markdown só podem ser removidos após aprovação humana explícita. As remoções propostas para este backend estão registradas em `docs/README.md` e `docs/issues.md`.

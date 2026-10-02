# Five Sense API

API backend do MVP Five Sense, uma aplicação de gerenciamento de atividades 5S para uma única empresa. O sistema permitirá administrar usuários, problemas, ocorrências, equipes, horários de 5S, materiais e estoque, com controle de acesso por perfil.

Este projeto segue o framework SpecFirst: o escopo, os contratos e as decisões são documentados e aprovados antes da implementação.

## Estado atual

- Escopo da Fase 0 consolidado e decisões funcionais autorizadas pelo responsável do produto.
- Implementação do MVP da API iniciada; veja `docs/issues.md`, `docs/implementation-plan.md` e `docs/deployment-log.md` para status e evidências.
- Verificação de integração PostgreSQL requer Docker disponível.

## Stack

- Java 21 e Spring Boot 4.1.1.
- Spring MVC, Spring Data JPA, Spring Security, Bean Validation e Spring Mail.
- PostgreSQL 18 e Flyway.
- MapStruct 1.6.3.
- Maven Wrapper.
- Docker.
- JUnit 5 e Mockito.

As dependências atualmente declaradas estão em `pom.xml`. RS256 e o suporte JWT do Spring Security estão aprovados. Admin/Gestor usam JWT de 1 hora; Viewer usa access token de 15 minutos com refresh token rotativo, sessão ociosa de 8 horas, duração absoluta de 30 dias e no máximo cinco sessões simultâneas. Testcontainers e ArchUnit estão configurados; a integração PostgreSQL roda quando Docker está disponível.

## Arquitetura

O sistema será um monólito MVC simples. Pacotes por capacidade de negócio podem conter as pastas `controller`, `app`, `infra`, `dto` e `domain`; isso é apenas organização interna e não transforma o projeto em modular monolith.

Visão resumida:

```text
HTTP -> Controller/DTO -> App/Service/MapStruct -> Domain/Infra -> PostgreSQL ou integração externa
```

Consulte `docs/architecture.md` para dependências permitidas e `docs/domains.md` para as fronteiras funcionais.

## Escopo do MVP

- Login, logout, alteração e recuperação de senha; cadastro administrativo envia senha inicial temporária ao e-mail da conta criada.
- O primeiro Admin será provisionado com senha aleatória revelada uma vez ao operador autorizado. Admin cria contas Manager/Viewer; Manager cria e gerencia contas Viewer.
- Usuários e papéis `ADMIN`, `MANAGER` e `VIEWER`.
- CRUD de problemas.
- Registro de ocorrências com problema, material, quantidade e notificação a todos os Admins e Gestores por e-mail; a quantidade não altera o estoque e imagens estão fora do escopo.
- CRUD de equipes, representantes textuais (até 255 caracteres), faixa/horário e status de execução do 5S.
- Consulta à organização dos horários de cada equipe (não é calendário de eventos).
- CRUD e consulta de materiais, quantidades e limites de estoque.
- Alertas de estoque e notificações por e-mail.
- Viewer consulta equipes, status, representantes e estoque; pode registrar ocorrências/alertas e alterar somente o status 5S. Admin/Gestor consultam dados operacionais, mas a API não fornece dashboards; dados de usuários só podem ser consultados por Admin/Gestor.
- API documentada por OpenAPI.

O mapa completo de RF001-RF033, NF001-NF005 e RN001-RN029 está em `docs/requirements.md`.

## Documentação

- `AGENTS.md` - contrato obrigatório do projeto.
- `docs/README.md` - índice e ordem de leitura.
- `docs/project-overview.md` - objetivo, público e limites do MVP.
- `docs/requirements.md` - rastreabilidade dos requisitos e ambiguidades.
- `docs/api-contract.md` - rotas e permissões HTTP implementadas.
- `docs/architecture.md` - arquitetura autorizada.
- `docs/data-model.md` - modelo de dados proposto.
- `docs/security.md` - autenticação, autorização e proteção de dados.
- `docs/testing.md` - estratégia de testes e fitness functions.
- `docs/implementation-plan.md` e `docs/issues.md` - sequência e estado do trabalho.

## Execução local

Para subir a API e o PostgreSQL localmente, configure `.env` a partir de `.env.example` e use Docker Compose. Os checks Maven disponíveis no Windows são:

```powershell
.\mvnw.cmd clean verify
```

Não assuma que a aplicação está operacional sem PostgreSQL, chaves JWT e SMTP configurados. Consulte `docs/operations.md`.

## Governança

Antes de implementar:

1. leia `AGENTS.md` e os documentos nucleares;
2. selecione uma issue aprovada;
3. confirme critérios de aceite e consulte decisões abertas somente se o escopo novo as reabrir;
4. implemente o menor incremento;
5. execute testes, métricas e fitness functions;
6. atualize issue, plano e log técnico.

Arquivos Markdown só podem ser removidos após aprovação humana explícita. As remoções propostas para este backend estão registradas em `docs/README.md` e `docs/issues.md`.

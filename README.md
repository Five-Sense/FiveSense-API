# Five Sense API

API backend do MVP Five Sense, uma aplicação de gerenciamento de atividades 5S para uma única empresa. O sistema permitirá administrar usuários, problemas, ocorrências, equipes, horários de 5S, materiais e estoque, com controle de acesso por perfil.

Este projeto segue o framework SpecFirst: o escopo, os contratos e as decisões são documentados e aprovados antes da implementação.

## Estado atual

- Fase 0 documental em revisão humana.
- Existe um scaffold Spring Boot mínimo, ainda sem funcionalidades de negócio.
- Nenhuma implementação deve avançar até a aprovação do escopo documentado e das decisões abertas.

## Stack

- Java 21 e Spring Boot 4.1.1.
- Spring MVC, Spring Data JPA, Spring Security, Bean Validation e Spring Mail.
- PostgreSQL 18 e Flyway.
- MapStruct 1.6.3.
- Maven Wrapper.
- Docker.
- JUnit 5 e Mockito.

As dependências atualmente declaradas estão em `pom.xml`. O projeto ainda precisa decidir e aprovar as ferramentas de JWT, testes com PostgreSQL real e fitness functions antes de adicioná-las.

## Arquitetura

O sistema será um monólito MVC simples. Pacotes por capacidade de negócio podem conter as pastas `controller`, `app`, `infra`, `dto` e `domain`; isso é apenas organização interna e não transforma o projeto em modular monolith.

Visão resumida:

```text
HTTP -> Controller/DTO -> App/Service/MapStruct -> Domain/Infra -> PostgreSQL ou integração externa
```

Consulte `docs/architecture.md` para dependências permitidas e `docs/domains.md` para as fronteiras funcionais.

## Escopo do MVP

- Login, logout, alteração e recuperação de senha.
- Usuários e papéis `ADMIN`, `MANAGER` e `VIEWER`.
- CRUD de problemas.
- Registro de ocorrências com material, quantidade e imagem opcional.
- CRUD de equipes, representantes, horário e status de execução do 5S.
- Consulta ao calendário de 5S.
- CRUD e consulta de materiais, quantidades e limites de estoque.
- Alertas de estoque e notificações por e-mail.
- API documentada por OpenAPI.

O mapa completo de RF001-RF033, NF001-NF005 e RN001-RN029 está em `docs/requirements.md`.

## Documentação

- `AGENTS.md` - contrato obrigatório do projeto.
- `docs/README.md` - índice e ordem de leitura.
- `docs/project-overview.md` - objetivo, público e limites do MVP.
- `docs/requirements.md` - rastreabilidade dos requisitos e ambiguidades.
- `docs/architecture.md` - arquitetura autorizada.
- `docs/data-model.md` - modelo de dados proposto.
- `docs/security.md` - autenticação, autorização e proteção de dados.
- `docs/testing.md` - estratégia de testes e fitness functions.
- `docs/implementation-plan.md` e `docs/issues.md` - sequência e estado do trabalho.

## Execução local

Os comandos finais de Docker e as variáveis ainda serão consolidados na Fase 1. O scaffold atual pode ser validado no Windows com:

```powershell
.\mvnw.cmd test
```

Não assuma que a aplicação está operacional sem PostgreSQL, chaves JWT, SMTP e armazenamento de imagens configurados. Consulte `docs/operations.md`.

## Governança

Antes de implementar:

1. leia `AGENTS.md` e os documentos nucleares;
2. selecione uma issue aprovada;
3. confirme critérios de aceite e decisões pendentes;
4. implemente o menor incremento;
5. execute testes, métricas e fitness functions;
6. atualize issue, plano e log técnico.

Arquivos Markdown só podem ser removidos após aprovação humana explícita. As remoções propostas para este backend estão registradas em `docs/README.md` e `docs/issues.md`.

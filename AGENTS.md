# AGENTS.md

## Propósito

Este arquivo é o contrato universal da **Five Sense API** para pessoas, agentes de IA e automações. Leia-o antes de qualquer implementação relevante e use `docs/*` como fonte canônica detalhada.

O projeto é exclusivamente uma API backend de gerenciamento de 5S para uma única empresa. O MVP cobre autenticação e autorização, usuários, problemas, ocorrências/alertas, equipes e horários, materiais, estoque e notificações por e-mail. Não há UI, dashboards ou imagens no escopo atual.

## Regras invioláveis

1. Não implementar fora da arquitetura autorizada em `docs/architecture.md`.
2. Manter um monólito Spring MVC simples; módulos servem apenas para organização de pacotes, não formam um modular monolith.
3. Não adicionar dependência, alterar arquitetura, modelo de dados, segurança ou escopo sem registrar a proposta e obter decisão humana quando ela for relevante.
4. Validar entradas externas na fronteira HTTP e reforçar invariantes no domínio/aplicação.
5. Usar MapStruct para mapeamentos entre entidades e DTOs; não criar mapeadores manuais sem justificativa registrada.
6. Usar Flyway para toda evolução do schema; Hibernate não deve alterar o banco de produção automaticamente.
7. Todo método público de `Service` deve ter testes unitários relevantes com JUnit e Mockito.
8. Todo fluxo entregue, do banco ao controller, deve ter ao menos um teste de integração com PostgreSQL compatível com a versão 18.
9. Ao terminar uma implementação, executar testes, fitness functions arquiteturais e métricas de complexidade definidas em `docs/testing.md`.
10. Nenhum ciclo de dependência entre pacotes/camadas é aceito.
11. Nunca persistir ou registrar senhas, tokens, chaves privadas ou payloads sensíveis em texto puro.
12. Não remover arquivos Markdown sem aprovação humana explícita e sem limpar suas referências cruzadas.
13. Toda tarefa concluída deve sincronizar `docs/issues.md`, `docs/implementation-plan.md` e `docs/deployment-log.md`.

## Stack autorizada

- Java 21.
- Spring Boot 4.1.1 e Spring MVC.
- Maven Wrapper.
- PostgreSQL 18.
- Spring Data JPA.
- Flyway.
- Spring Security com suporte a JWT assinado com RS256.
- Bean Validation.
- MapStruct 1.6.3.
- Spring Mail; Thymeleaf pode ser usado apenas para templates de e-mail enquanto permanecer como dependência.
- Spring Boot Actuator, com exposição somente de health/readiness sem detalhes sensíveis.
- springdoc OpenAPI.
- Docker para ambiente local, integração e empacotamento.
- JUnit 5 e Mockito; Testcontainers PostgreSQL 18 e ArchUnit aprovados para testes.

O `pom.xml` atual é a fonte para versões já presentes. Dependências existentes não são autorização para uso sem necessidade; dependências necessárias e ausentes devem ser propostas antes de inclusão.

## Arquitetura e estrutura esperada

Pacote raiz: `com.fivesense.api`.

```text
src/main/java/com/fivesense/api/
  <modulo>/
    controller/  # endpoints HTTP e tradução do protocolo
    app/         # services, regras de aplicação e mappers MapStruct
    infra/       # JPA, e-mail, arquivos e integrações externas
    dto/         # contratos de entrada e saída
    domain/      # entidades, enums e invariantes do domínio
  shared/        # somente código realmente transversal
```

Os módulos previstos são `auth`, `users`, `teams`, `problems`, `materials` e `occurrences`. Essa divisão organiza o monólito; não cria deploys, bancos ou contratos remotos separados.

Dependências permitidas e proibidas estão em `docs/architecture.md`. Não criar interfaces, ports, eventos ou abstrações apenas por antecipação.

## Fluxo de trabalho

1. Entender objetivo, issue, critérios de aceite e decisões pendentes.
2. Ler este arquivo e os documentos relevantes em `docs/README.md`.
3. Confirmar impactos em dados, segurança, integrações e testes.
4. Escrever ou ajustar testes antes ou junto do comportamento.
5. Implementar o menor incremento seguro usando as dependências já escolhidas.
6. Rodar os checks de `docs/testing.md`, inclusive fitness functions.
7. Atualizar documentação afetada.
8. Sincronizar issue, plano e log técnico antes de encerrar.

O escopo base e as decisões de produto constantes dos documentos canônicos foram aprovados para implementação. Decisões novas que alterem esse escopo continuam exigindo registro e decisão humana.

## Definition of Done

Uma tarefa só está pronta quando:

- o comportamento e os critérios de aceite foram atendidos;
- cada método público de service alterado possui cobertura unitária significativa;
- cada fluxo alterado possui teste de integração banco-controller;
- build, testes e fitness functions passam;
- métricas de complexidade, acoplamento e coesão estão dentro dos limites ou têm exceção justificada;
- validação, autorização, logs seguros e migração Flyway foram tratados quando aplicáveis;
- não há ciclos de dependência ou abstração sem uso;
- OpenAPI e documentação foram atualizados quando o contrato mudou;
- `docs/issues.md`, `docs/implementation-plan.md` e `docs/deployment-log.md` refletem a entrega;
- riscos residuais e checks não executados estão registrados.

## Mapa de documentação

Leia sempre:

- `docs/README.md`
- `docs/project-overview.md`
- `docs/requirements.md`
- `docs/architecture.md`
- `docs/ai-workflow.md`
- `docs/coding-standards.md`
- `docs/testing.md`

Leia sob demanda:

| Documento | Quando consultar |
| --- | --- |
| `docs/domains.md` | Fronteiras e dependências entre capacidades. |
| `docs/data-model.md` | Entidades, relações, constraints e migrações. |
| `docs/security.md` | Auth, autorização, uploads, secrets ou dados pessoais. |
| `docs/workflows.md` | Jornadas e efeitos de negócio. |
| `docs/api-contract.md` | Rotas HTTP, acesso por perfil e convenções de request/response. |
| `docs/operations.md` e `docs/deploy.md` | Ambiente, Docker, backup e deploy. |
| `docs/templates.md` | Recipes aprovadas de endpoint, mapper, migração e teste. |
| `docs/decision-log.md` | Decisões duradouras e propostas abertas. |
| `docs/implementation-plan.md` e `docs/issues.md` | Sequência e trabalho ativo. |
| `docs/deployment-log.md` | Entregas e checks executados. |

## Hierarquia de contexto

1. `AGENTS.md`.
2. Documentação canônica em `docs/*` e decisões aceitas em `docs/decision-log.md`, não leia `docs/site`.
3. Código e testes, para o comportamento já implementado.
4. Adaptadores de ferramenta, como `CLAUDE.md`.

Em conflito com o documento de requisitos original, registre a divergência e peça decisão humana; não invente a regra.

## Limites da IA

O humano define produto, prioridade, escopo e decisões relevantes. A IA propõe o caminho técnico, implementa incrementos aprovados, valida e mantém o histórico.

Pausar e pedir decisão antes de: mudar os papéis ou permissões já aprovados, alterar política de token, reabrir persistência de imagens, vincular ocorrência a movimentação de estoque, mudar política de exclusão, alterar módulos, adicionar dependências ou remover documentos.

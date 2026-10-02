# Deployment Log

Este log registra entregas técnicas e documentais da Five Sense API. A entrada mais recente fica no topo.

> As entradas datadas de 2026-09-25 são registros históricos do estado anterior às decisões finais e à implementação. Para o estado atual, use a entrada de 2026-09-28 e `implementation-plan.md`.

## [2026-09-28] - Implementação do MVP e encerramento do framework SpecFirst

- **Fase:** Fases 0-4 concluídas; implementação da Fase 5 concluída, com validações operacionais/deploy pendentes.
- **O que foi feito:** finalizados os módulos API de autenticação/sessões, usuários, problemas, equipes, materiais, ocorrências/e-mail; bootstrap seguro do primeiro Admin; limites de sessões Viewer; autorização e documentação do contrato HTTP. Adicionadas pipeline CI, Dockerfile/Compose, migrações Flyway, ArchUnit, cobertura unitária dos services e teste de integração PostgreSQL.
- **Arquivos modificados:** código em `src/main/java`, testes em `src/test/java`, configuração (`pom.xml`, Maven Wrapper, `application.properties`, `.env.example`, `compose.yaml`, `Dockerfile`, `.github/workflows/ci.yml`) e documentação SpecFirst (`AGENTS.md`, `README.md`, `docs/*`). Nenhum Markdown foi removido.
- **Checks executados:** `mvnw.cmd clean verify` com JDK 25 compilando para Java 21: BUILD SUCCESS; 57 testes reportados, 0 falhas/erros e 1 teste PostgreSQL-Testcontainers ignorado porque o daemon Docker não está disponível. As 4 fitness functions ArchUnit passaram. A integração PostgreSQL 18 precisa rodar em CI/Docker.
- **Limitações:** sem daemon Docker não foi possível executar o teste de integração contra PostgreSQL 18. Métricas quantitativas de complexidade/acoplamento seguem revisão manual; não foram produzidos relatórios de métricas nesta execução. SMTP, secrets RSA/bootstrap, backup/restauração e staging precisam ser configurados/verificados no ambiente de operação.
- **Próxima ação operacional:** executar `./mvnw clean verify` no CI/Docker e confirmar provisionamento de secrets, SMTP, health/readiness, backup e rollback antes do deploy.

## [2026-09-25] - Revisão de permissões, primeiro acesso e sessão Viewer

- **Fase:** Fase 0 - Escopo e contratos (aguarda decisões residuais e aprovação final).
- **O que foi feito:** sincronizadas regras de troca obrigatória da senha inicial, estado `FIRST_ACCESS`, reenvio com invalidação da credencial anterior, limite padrão de texto, permissões por perfil, Viewer limitado a equipes/status/representantes/estoque, horários sem semântica de calendário e recomendação de sessão persistente rotativa/revogável com fallback JWT de 8h.
- **Arquivos modificados:** `AGENTS.md`, `README.md`, `docs/README.md`, `docs/requirements.md`, `docs/security.md`, `docs/data-model.md`, `docs/workflows.md`, `docs/project-overview.md`, `docs/domains.md`, `docs/architecture.md`, `docs/decision-log.md`, `docs/implementation-plan.md`, `docs/issues.md`, `docs/operations.md` e este log.
- **Código modificado:** nenhum.
- **Arquivos removidos:** nenhum.
- **Checks executados:** leitura e revisão cruzada dos Markdown; buscas por regras residuais contraditórias. Build/testes não executados por não haver alterações de código.
- **Riscos/débitos:** aprovar duração/idle/max de sessão Viewer; confirmar se alerta é ocorrência RF016 ou fluxo separado; definir granularidade dos horários, rate limit/falha de e-mail, regra/canal de alerta de estoque e exclusões.

## [2026-09-25] - Regras finais de Viewer, representantes e bootstrap

- **Fase:** Fase 0 - Escopo e contratos (bloqueada por pendências restantes).
- **O que foi feito:** alinhados papéis de criação de contas, hash de senha com Spring Security, Viewer sem relação com equipe e com `teamId` no request de status, token Viewer sem expiração, notificações para todos Admins/Gestores, representantes como texto e provisionamento do Admin inicial com revelação única.
- **Arquivos modificados:** `docs/requirements.md`, `docs/security.md`, `docs/data-model.md`, `docs/workflows.md`, `docs/domains.md`, `docs/decision-log.md`, `docs/operations.md`, `docs/implementation-plan.md`, `docs/issues.md`, `docs/deploy.md`, `docs/architecture.md`, `docs/templates.md`, `docs/README.md`, `README.md` e este log.
- **Código modificado:** nenhum.
- **Arquivos removidos:** nenhum.
- **Checks executados:** revisão cruzada das regras de autorização, dados, autenticação e notificações; `git diff --check`. Build/testes não executados por não haver mudanças de código.
- **Riscos/débitos:** limite/formato do texto de representantes, revogação de token Viewer sem expiração, obrigatoriedade de troca de senha, canal de bootstrap, alcance do `teamId`, fluxo de e-mails, política de alertas, exclusões, permissões de consulta e contratos HTTP ainda precisam de definição.

## [2026-09-25] - Atribuições de conta, Viewer e bootstrap Admin

- **Fase:** Fase 0 - Escopo e contratos (bloqueada por decisões restantes).
- **O que foi feito:** registrados: Admin cria contas Manager/Viewer; Manager cria e gerencia Viewer; representantes são texto na equipe; Viewer não tem associação a equipe, mas envia `teamId` para alteração de status; Viewer usa token sem expiração no totem; tokens Admin/Manager duram uma hora; ocorrências notificam todos Admins e Gestores; senha usa hash unidirecional Spring Security; usuário pode trocar senha no primeiro login; primeiro Admin será pré-cadastrado e terá senha aleatória revelada uma vez.
- **Arquivos modificados:** `docs/requirements.md`, `docs/security.md`, `docs/data-model.md`, `docs/workflows.md`, `docs/domains.md`, `docs/decision-log.md`, `docs/operations.md`, `docs/implementation-plan.md`, `docs/issues.md`, `README.md` e este log.
- **Código modificado:** nenhum.
- **Arquivos removidos:** nenhum.
- **Checks executados:** comparação cruzada da matriz de papéis, modelo, fluxos e decisões. Build/testes não executados por não haver alterações de código.
- **Riscos/débitos:** escolher mecanismo de revelação única do primeiro Admin; decidir se troca de senha no primeiro login é obrigatória; definir revogação de token Viewer sem expiração, limites/formato do texto de representantes, regras do calendário, semântica de e-mails, política de alerta de estoque, exclusões, permissões de consulta e contratos HTTP.

## [2026-09-25] - Revisão de consistência do escopo API-only

- **Fase:** Fase 0 - Escopo e contratos (bloqueada por decisões de produto/segurança restantes).
- **O que foi feito:** nova varredura de referências a UI, upload, imagem, destinatários, estoque, JWT e status; esclarecido que regras genéricas de upload só se aplicariam se o escopo fosse reaberto; alinhados fluxo de IA, contexto, padrões de código e arquitetura à API sem arquivos/imagens; marcada como substituída a proposta antiga de senha por link/armazenamento de imagens; remoções documentais explicitamente tratadas como opcionais.
- **Arquivos modificados:** `docs/ai-workflow.md`, `docs/coding-standards.md`, `docs/context-strategy.md`, `docs/architecture.md`, `docs/decision-log.md`, `docs/implementation-plan.md`, `docs/issues.md` e este log.
- **Código modificado:** nenhum.
- **Arquivos removidos:** nenhum.
- **Checks executados:** busca textual das referências e `git diff --check`. Build/testes não executados por não haver alteração de código.
- **Riscos/débitos:** decisões abertas de Fase 0 permanecem em `requirements.md`; a documentação de requisitos retém descrições de UI do PDF somente como contexto de origem, sem escopo de frontend.

## [2026-09-25] - Decisões complementares da Fase 0

- **Fase:** Fase 0 - Escopo e contratos (ainda bloqueada por decisões remanescentes).
- **O que foi feito:** registradas decisões humanas: senha temporária inicial enviada ao e-mail da conta criada; Gestores definem representantes; API não terá imagens; quantidade afetada em ocorrência não altera estoque; suporte JWT do Spring Security com RS256.
- **Arquivos modificados:** `AGENTS.md`, `README.md`, `docs/requirements.md`, `docs/project-overview.md`, `docs/data-model.md`, `docs/workflows.md`, `docs/security.md`, `docs/architecture.md`, `docs/domains.md`, `docs/operations.md`, `docs/deploy.md`, `docs/implementation-plan.md`, `docs/issues.md`, `docs/testing.md`, `docs/backlog.md`, `docs/decision-log.md` e este log.
- **Código modificado:** nenhum.
- **Arquivos removidos:** nenhum.
- **Checks executados:** busca por referências residuais a imagens, e-mail inicial, representantes, estoque e JWT; revisão documental. Build/testes não executados por não haver mudança de código.
- **Riscos/débitos:** definir obrigatoriedade de troca de senha inicial, política de validade/logout/revogação de token, papéis/limite de representantes, calendário, semântica/idempotência de e-mail, limiar/canal do alerta, exclusões, permissões e contratos HTTP/OpenAPI.

## [2026-09-25] - Revisão do PDF e reconciliação da Fase 0

- **Fase:** Fase 0 - Escopo e contratos (bloqueada por decisões pendentes).
- **O que foi feito:** comparado o PDF reenviado com a rastreabilidade e os documentos de produto; detalhados RF001-RF033 e RN001-RN029; registradas duplicatas e divergências; corrigidos os fluxos de primeiro acesso, e-mail de ocorrência e imagem de material; RS256 registrado como aprovado pelo humano e NF005.
- **Arquivos modificados:** `README.md`, `docs/requirements.md`, `docs/data-model.md`, `docs/workflows.md`, `docs/security.md`, `docs/decision-log.md`, `docs/issues.md`, `docs/implementation-plan.md`, `docs/implementation-governance.md` e este log.
- **Código modificado:** nenhum.
- **Arquivos removidos:** nenhum.
- **Checks executados:** extração textual das 31 páginas do PDF e comparação documental; revisão pontual de arquivos alterados. Build/testes não executados por não haver mudança de código.
- **Riscos/débitos:** prioridade de requisitos, política de tokens apesar do algoritmo RS256 aceito, primeiro acesso, representantes, agenda, comportamento de e-mail e estoque, imagens, exclusões, permissões indefinidas e critérios técnicos de fitness functions continuam aguardando decisão.

## [2026-09-18] - ISSUE-001: adaptação inicial do SpecFirst

- **Fase:** Fase 0 - Escopo e contratos.
- **O que foi feito:** leitura integral do PDF de requisitos, `AGENTS.md`, `README.md`, `CLAUDE.md` e `docs/*`; adaptação do framework para a API; criação de rastreabilidade; definição proposta de arquitetura, dados, segurança, testes, operação e fases.
- **Arquivos modificados:** `AGENTS.md`, `README.md`, `CLAUDE.md`, documentos aplicáveis em `docs/*`.
- **Arquivo criado:** `docs/requirements.md`.
- **Código modificado:** nenhum.
- **Arquivos removidos:** nenhum.
- **Checks executados:** extração completa das 31 páginas do PDF; inspeção visual das páginas 25-31; inventário e leitura integral dos Markdown; inspeção read-only do `pom.xml`, scaffold e configuração; busca de placeholders (restaram somente nos cinco templates propostos para remoção); validação de que todas as referências a arquivos Markdown resolvem para arquivos existentes; confirmação de que `pom.xml` e `src/*` não foram alterados. Build Maven não foi executado porque esta entrega modifica somente documentação.
- **Docs atualizados:** overview, requisitos, arquitetura, workflow, standards, testes, domínios, dados, segurança, fluxos, operação, deploy, recipes, contexto, adapters, governança, backlog, decisões, plano, issues e este log.
- **Riscos/débitos:** decisões de segurança, representantes, calendário, ocorrência/estoque, e-mail, imagens, exclusão, paginação e dependências de teste/fitness permanecem abertas; PDF fonte não está versionado; arquivos propostos para remoção continuam presentes até aprovação.

_Registre novas entregas acima desta linha._

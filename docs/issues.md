# Issues

Status: `Planejada`, `Em andamento`, `Em revisão`, `Concluída` ou `Bloqueada`.

> Estado consolidado em 2026-09-28: Fase 0 está concluída, sem pendências de decisão funcional. A implementação da API e do framework está concluída; ISSUE-002 a ISSUE-005 permanecem em revisão apenas pelas verificações de ambiente descritas em cada issue. Ver o registro mais recente no histórico de ISSUE-001 e `deployment-log.md`.

## ISSUE-001 - Adaptar SpecFirst à Five Sense API

**Tipo:** Docs  
**Epic:** EPIC-00 Fundação  
**Status:** Concluída
**Fase:** Fase 0

### Objetivo

Transformar o template SpecFirst em contrato específico do projeto e mantê-lo alinhado com as decisões e a implementação.

### Critérios de aceite

- [x] PDF e framework completos lidos.
- [x] Requisitos reconciliados com o PDF reenviado e RS256 registrado como autorizado.
- [x] Objetivo, escopo, arquitetura, domínios, dados, segurança e operação documentados.
- [x] Requisitos e contradições rastreados.
- [x] Estratégia de testes e fitness functions proposta.
- [x] Arquivos não aplicáveis listados, sem remoção.
- [x] Logs e plano sincronizados.
- [x] Humano confirma o escopo funcional e critérios por instruções da conversa; decisões foram refletidas na documentação.
- [x] Pendências de produto/segurança foram resolvidas; remoções documentais continuam opcionais e não bloqueiam implementação.

### Estado atual

- **2026-09-25 (IA, histórico superado em 2026-09-28):** PDF reenviado comparado requisito a requisito; RF001-RF033 e RN001-RN029 foram detalhados, duplicatas da fonte registradas e RS256 confirmado. Naquele momento, Fase 0 parecia bloqueada por decisões humanas.
- **2026-09-25 (IA):** humano confirmou senha inicial temporária por e-mail ao titular, representantes escolhidos por Gestores, ausência de imagens na API e que ocorrência não altera estoque; suporte JWT do Spring Security selecionado. Documentos reconciliados. Permanecem os pontos listados na seção de pendências de `requirements.md`.
- **2026-09-25 (IA):** nova revisão API-only alinhou regras genéricas de upload ao escopo sem arquivos, marcou a proposta inicial de senha por link/imagens como substituída e esclareceu que remoções de templates não bloqueiam implementação. Fase 0 continua bloqueada pelos pontos de produto/segurança ainda abertos.
- **2026-09-25 (IA, histórico supersedido):** humano definiu senha inicial alterável desde o primeiro login e token Viewer sem expiração. Orientações posteriores tornaram a troca obrigatória e substituíram JWT infinito por proposta de sessão renovável/revogável, com fallback de 8h.
- **2026-09-25 (IA):** confirmado que o endpoint Viewer recebe `teamId`; atualizada a matriz para não exigir vínculo da conta com equipe. Definidos `PasswordEncoder` unidirecional e destinatários ADMIN/MANAGER. Removido vínculo `TeamRepresentative` do modelo e substituído por `Team.representatives` textual.
- **2026-09-25 (IA):** humano confirmou troca obrigatória da senha inicial, reenvio para contas ainda não validadas, `varchar(255)` como limite padrão de texto sem limite explícito, horários como organização por equipe, leitura restrita do Viewer, consulta de usuários apenas por Admin/Gestor e ausência de dashboards. Registrada recomendação de sessão refresh rotativa/revogável para Viewer, com fallback JWT de 8h. Falta confirmar semântica de “gerar alertas” e limites da sessão.
- **2026-09-25 (IA):** execução autorizada; requisitos e decisões foram fechados em docs canônicos, Fase 0 concluída e contrato de API registrado. Nenhum Markdown foi removido. Framework alinhado com a implementação.
- **2026-09-28 (IA):** encerrada a implementação do MVP e adaptado o framework; sincronizados estados das fases, issues e deployment log. `clean verify` passou com 57 testes: 56 passaram e 1 integração foi ignorada porque Docker não está disponível. Pendências restantes são executar essa integração PostgreSQL 18 em Docker/CI e preparar deploy; não há decisão funcional pendente. Nenhum arquivo Markdown removido.

## ISSUE-002 - Criar fundação técnica

**Tipo:** Chore  
**Epic:** EPIC-00 Fundação  
**Status:** Em revisão
**Fase:** Fase 1

### Objetivo

Preparar build, Docker/PostgreSQL 18, configuração, Flyway, OpenAPI, testes, fitness functions e CI.

### Critérios de aceite

- `.\mvnw.cmd clean verify` executa unitários, integração e arquitetura.
- Ambiente local sobe de forma reproduzível.
- Dependências novas estão declaradas e justificadas.
- Dockerfile, Compose, Flyway, OpenAPI, ArchUnit e CI adicionados; falta execução local de integração com Docker ativo.

## ISSUE-003 - Autenticação e usuários

**Tipo:** Feature  
**Epic:** EPIC-01 Identidade  
**Status:** Em revisão
**Fase:** Fase 2

### Objetivo

Entregar RF001-RF008 com senha inicial por e-mail, troca obrigatória, reenvio enquanto conta estiver em `FIRST_ACCESS` e regras de criação por papel.

Implementação presente: sessão Viewer rotativa e limitada, reset, logout, bootstrap e gestão de usuários; integração real precisa rodar com PostgreSQL 18 disponível.

## ISSUE-004 - Problemas, equipes e materiais

**Tipo:** Feature  
**Epic:** EPIC-02 Operação 5S  
**Status:** Em revisão
**Fase:** Fase 3

### Objetivo

Entregar catálogos, equipes, agenda, status, estoque e representantes textuais conforme decisões aprovadas.

Implementação presente; integração real precisa rodar com PostgreSQL 18 disponível.

## ISSUE-005 - Ocorrências e notificações

**Tipo:** Feature  
**Epic:** EPIC-03 Ocorrências  
**Status:** Em revisão
**Fase:** Fase 4

### Objetivo

Registrar ocorrência sem imagens; notificar Admins e Gestores ativos e enviar a resposta padrão ao e-mail do problema. Quantidade afetada não altera estoque. E-mail best effort sem outbox/retry durável.

Semântica definida e implementação presente. E-mails são best effort sem outbox durável; integração de banco está condicionada a Docker.

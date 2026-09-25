# Issues

Status: `Planejada`, `Em andamento`, `Em revisão`, `Concluída` ou `Bloqueada`.

## ISSUE-001 - Adaptar SpecFirst à Five Sense API

**Tipo:** Docs  
**Epic:** EPIC-00 Fundação  
**Status:** Em revisão  
**Fase:** Fase 0

### Objetivo

Transformar o template SpecFirst em contrato específico do projeto antes de criar código.

### Critérios de aceite

- [x] PDF e framework completos lidos.
- [x] Objetivo, escopo, arquitetura, domínios, dados, segurança e operação documentados.
- [x] Requisitos e contradições rastreados.
- [x] Estratégia de testes e fitness functions proposta.
- [x] Arquivos não aplicáveis listados, sem remoção.
- [x] Logs e plano sincronizados.
- [ ] Humano aprova ou ajusta o escopo documentado.
- [ ] Humano decide remoções e pontos bloqueadores.

### Estado atual

- **2026-09-18 (IA):** documentação adaptada ao backend Five Sense; nenhum código ou arquivo Markdown foi removido. Aguardando revisão humana das decisões abertas e dos candidatos a remoção.

## ISSUE-002 - Criar fundação técnica

**Tipo:** Chore  
**Epic:** EPIC-00 Fundação  
**Status:** Planejada  
**Fase:** Fase 1

### Objetivo

Preparar build, Docker/PostgreSQL 18, configuração, Flyway, OpenAPI, estratégia de testes e fitness functions.

### Critérios de aceite

- `.\mvnw.cmd clean verify` executa unitários, integração e arquitetura.
- Ambiente local sobe de forma reproduzível.
- Dependências novas foram previamente aprovadas.

## ISSUE-003 - Autenticação e usuários

**Tipo:** Feature  
**Epic:** EPIC-01 Identidade  
**Status:** Planejada  
**Fase:** Fase 2

### Objetivo

Entregar RF001-RF008 após aprovação das decisões de token, logout e primeiro acesso.

## ISSUE-004 - Problemas, equipes e materiais

**Tipo:** Feature  
**Epic:** EPIC-02 Operação 5S  
**Status:** Planejada  
**Fase:** Fase 3

### Objetivo

Entregar catálogos, equipes, agenda, status e estoque conforme decisões aprovadas.

## ISSUE-005 - Ocorrências e notificações

**Tipo:** Feature  
**Epic:** EPIC-03 Ocorrências  
**Status:** Planejada  
**Fase:** Fase 4

### Objetivo

Registrar ocorrência, imagem e e-mail com efeito de estoque definido e integração idempotente.

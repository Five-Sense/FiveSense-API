# Decision Log

Estados: `Proposta`, `Aceita`, `Substituída` ou `Rejeitada`.

## 0001 - Usar SpecFirst como contrato do projeto

- **Data:** 2026-09-18
- **Estado:** Aceita

### Contexto

O projeto será implementado por humanos e agentes e precisa preservar escopo, regras e evidências.

### Decisão

`AGENTS.md` é o contrato universal; `docs/*` é a fonte canônica detalhada. Issue, plano e deployment log devem ser sincronizados.

### Consequências

- Implementação começa somente após aprovação documental.
- Decisões e riscos deixam rastro versionável.

## 0002 - Adotar monólito Spring MVC simples

- **Data:** 2026-09-18
- **Estado:** Aceita

### Contexto

O MVP atende uma única empresa e não exige distribuição ou arquitetura sofisticada.

### Decisão

Usar Java 21, Spring Boot 4.1.1, Spring MVC, PostgreSQL 18, Flyway, Docker e Maven em um único deploy. Não adotar multi-tenancy, microservices ou modular monolith formal.

### Consequências

- Menor custo operacional e cognitivo.
- Fronteiras internas continuam testadas para impedir acoplamento acidental.

## 0003 - Exigir testes por service, fluxo e arquitetura

- **Data:** 2026-09-18
- **Estado:** Aceita

### Contexto

O humano exige teste unitário para cada método de service, teste de integração banco-controller para cada fluxo e fitness functions após implementações.

### Decisão

JUnit/Mockito cobrem services; integração usa PostgreSQL 18 real; o build mede ciclos, complexidade ciclomática, acoplamento e coesão conforme `testing.md`.

### Consequências

- `verify` será o gate de conclusão.
- Testcontainers, ArchUnit e ferramenta de métricas ainda precisam de aprovação como dependências.

## 0004 - Organizar por capacidade com subcamadas

- **Data:** 2026-09-18
- **Estado:** Proposta

### Contexto

Uma estrutura apenas por camada global tende a misturar capacidades, enquanto um modular monolith seria excessivo.

### Decisão proposta

Usar pacotes `auth`, `users`, `teams`, `problems`, `materials` e `occurrences`, cada um com `controller`, `app`, `infra`, `dto` e `domain` somente quando necessário.

### Consequências

- Navegação simples e coesão por capacidade.
- Sem módulos Maven, isolamento de runtime ou eventos internos obrigatórios.
- Requer aprovação humana antes do scaffold.

## 0005 - Estratégia inicial de segurança e imagens

- **Data:** 2026-09-18
- **Estado:** Proposta

### Decisão proposta

- Tokens de acesso finitos para todos os papéis, com renovação revogável em vez de token infinito.
- Link de ativação de uso único em vez de senha em texto por e-mail.
- Imagens em volume privado, metadata no PostgreSQL e acesso autenticado.
- Exclusão bloqueada/inativação quando houver histórico.

### Consequências

- Reduz risco de credencial permanente, vazamento e perda de histórico.
- Exige decisão humana e definição de contratos antes de implementação.

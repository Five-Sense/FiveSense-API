# Plano de implementação

## Objetivo

Entregar o MVP da Five Sense API como monólito MVC simples, seguro, testado e operável para uma única empresa.

## Fase 0 - Escopo e contratos

**Status:** Concluída; decisões funcionais aprovadas para implementação.

- [x] Ler o PDF de requisitos e os documentos SpecFirst.
- [x] Adaptar AGENTS, README e documentos aplicáveis.
- [x] Rastrear RF001-RF033, NF001-NF005 e RN001-RN029.
- [x] Registrar divergências da fonte e decisões humanas.
- [x] Não remover templates; eventual limpeza documental permanece sujeita a aprovação explícita.
- [x] Consolidar autenticação/roles, sessão Viewer, bootstrap Admin, ocorrências, estoque, exclusões e contrato HTTP.

## Fase 1 - Fundação técnica

**Status:** Implementada; verificação com PostgreSQL real pendente de Docker disponível.

- [x] Dependências, Maven Wrapper e processamento MapStruct.
- [x] Dockerfile/Compose para API e PostgreSQL 18.
- [x] Configuração por ambiente, Flyway, Hibernate validate, e-mail, JWT RS256 e bootstrap.
- [x] OpenAPI e erros RFC 9457.
- [x] Migração inicial e fitness functions ArchUnit.
- [ ] Executar `clean verify` com daemon Docker ativo e corrigir qualquer falha de integração.
- [x] Pipeline CI executa `clean verify`; métricas quantitativas são revisadas manualmente.

## Fase 2 - Autenticação e usuários

**Status:** Implementada e coberta por testes unitários; integração PostgreSQL ainda precisa rodar no ambiente com Docker.

- [x] Login, logout, refresh rotativo, reset de senha, BCrypt e autorização.
- [x] Bootstrap de primeiro Admin com senha aleatória revelada uma vez.
- [x] Primeiro acesso obrigatório, envio/reenvio de credencial e rate limit.
- [x] Gestão de usuários com regras de Admin/Manager/Viewer.
- [x] Invalidação de sessões após troca/reset de senha.
- [x] Criar testes unitários comportamentais para os métodos públicos de service alterados.
- [ ] Rodar fluxo banco-controller PostgreSQL 18.

## Fase 3 - Problemas, equipes e materiais

**Status:** Implementada e coberta por testes unitários; integração PostgreSQL ainda precisa rodar no ambiente com Docker.

- [x] CRUD de problemas, consulta de opções para ocorrência e exclusão protegida.
- [x] Equipes com representantes e horários textuais, status padrão e atualização Viewer por `teamId`.
- [x] CRUD e leitura de materiais, estoque 0..999, alerta por mínimo e exclusão protegida.
- [x] Regras de autorização e paginação padrão 20/máxima 100.
- [x] Criar testes unitários comportamentais para os métodos públicos de service alterados.
- [ ] Rodar integração banco-controller.

## Fase 4 - Ocorrências e notificações

**Status:** Implementada e coberta por testes unitários; integração PostgreSQL e SMTP real dependem do ambiente configurado.

- [x] Criação/listagem de ocorrência sem imagem.
- [x] Notificação a Admins/Gestores ativos e resposta padrão ao e-mail associado ao problema.
- [x] Quantidade afetada não altera estoque.
- [x] Criar testes unitários de regras e do adaptador de e-mail.
- [ ] Executar integração banco-controller com SMTP simulado.

## Fase 5 - Hardening e deploy

**Status:** Implementação concluída; hardening e validação de ambiente de produção permanecem pendentes.

- [ ] Executar e revisar matriz de acesso ponta a ponta com PostgreSQL 18 (daemon Docker indisponível nesta execução).
- [ ] Revisar concorrência de refresh/reset/bootstrap e configurações de produção.
- [x] Pipeline de build/verify.
- [ ] Secrets, backup/restauração, staging e rollback.
- [ ] Confirmar SMTP/DNS/remetente, health/readiness e runbook operacional no ambiente de deploy.
- [ ] Revisar OpenAPI gerada e relatório final de qualidade.

## Condições externas

1. Daemon Docker ativo para Testcontainers e verificação local completa.
2. Chaves RSA, SMTP e segredo de bootstrap provisionados fora do repositório.
3. Aprovação explícita para remover qualquer documento Markdown listado como candidato no índice.

## Estado de encerramento desta execução (2026-09-28)

- Implementação da API e framework SpecFirst concluída; `clean verify` passou com 57 testes reportados, sem falhas/erros, e 1 teste de integração ignorado por falta de Docker. A suíte inclui 4 fitness functions ArchUnit.
- A integração com PostgreSQL 18 e SMTP simulado está implementada no teste de integração, mas não executou localmente; deve rodar em Docker/CI.
- Não foram removidos documentos Markdown. Os documentos genéricos candidatos continuam aguardando aprovação explícita.
- O deploy não está declarado como concluído: chaves RSA, segredo de bootstrap, SMTP/DNS, backup e staging são pré-requisitos operacionais externos.

## Critérios globais

- Requisitos aprovados entregues; imagens e dashboards permanecem fora do escopo.
- Nenhum ciclo; fitness functions passam; métricas são revisadas e exceções registradas.
- PostgreSQL 18 e Flyway validados em testes de integração.
- Permissões cobertas por casos positivos e negativos.
- OpenAPI, issues, plano e logs sincronizados.

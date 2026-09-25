# Plano de implementação

## Objetivo

Entregar o MVP da Five Sense API como monólito MVC simples, seguro, testado e operável para uma única empresa.

## Fase 0 - Escopo e contratos

**Status:** Em revisão

- [x] Ler o PDF de requisitos e todos os arquivos do SpecFirst.
- [x] Adaptar AGENTS, README, adapter e documentos aplicáveis.
- [x] Mapear RF001-RF033, NF001-NF005 e RN001-RN029.
- [x] Registrar ambiguidades, riscos e propostas.
- [x] Propor documentos não aplicáveis para remoção, sem removê-los.
- [x] Atualizar issue, plano, decision log e deployment log.
- [ ] Obter aprovação humana do escopo, arquitetura proposta e critérios.
- [ ] Decidir as ambiguidades bloqueadoras de `requirements.md`.
- [ ] Executar remoções documentais aprovadas e limpar referências.

## Fase 1 - Fundação técnica

**Status:** Planejada

- [ ] Confirmar/corrigir dependências do `pom.xml`, incluindo processor MapStruct.
- [ ] Aprovar e configurar JWT, Testcontainers, ArchUnit e métricas.
- [ ] Criar Dockerfile/Compose com PostgreSQL 18.
- [ ] Configurar ambientes, secrets, erros, OpenAPI e health.
- [ ] Criar primeira migração Flyway e estratégia do primeiro Admin.
- [ ] Implementar fitness functions e pipeline `verify`.

## Fase 2 - Autenticação e usuários

**Status:** Planejada

- [ ] RF001-RF008, NF001, NF002, NF005.
- [ ] Login/logout e política de tokens aprovada.
- [ ] Gestão de usuários e primeiro acesso.
- [ ] Alteração e recuperação de senha.
- [ ] Unitários, integração e fitness functions.

## Fase 3 - Problemas, equipes e materiais

**Status:** Planejada

- [ ] RF009-RF015: problemas.
- [ ] RF017-RF026: equipes, representantes, status e calendário.
- [ ] RF027-RF033: materiais, estoque e alertas.
- [ ] Paginação, autorização e exclusão conforme decisões.
- [ ] Unitários, integração e fitness functions por fluxo.

## Fase 4 - Ocorrências e integrações

**Status:** Planejada

- [ ] RF016 e RN013-RN015.
- [ ] Upload/armazenamento de imagem.
- [ ] E-mail idempotente e destinatário aprovado.
- [ ] Efeito aprovado sobre estoque.
- [ ] Unitários, integração e fitness functions.

## Fase 5 - Hardening e deploy

**Status:** Planejada

- [ ] Revisão de segurança, limites e logs.
- [ ] Validar backup/restauração e migrações.
- [ ] Teste de todos os fluxos e matriz de acesso.
- [ ] Imagem Docker, staging, rollback e runbook.
- [ ] Revisão final de OpenAPI e documentação.

## Dependências críticas

1. Aprovação desta Fase 0.
2. Decisões de segurança, representantes, agenda, estoque, imagens e exclusão.
3. Aprovação de dependências técnicas ausentes.
4. SMTP, URL do cliente e destino de deploy.

## Critérios globais

- RF aprovado entregue com teste unitário e integração.
- Nenhum ciclo; métricas dentro do limite ou exceção aceita.
- PostgreSQL 18 e Flyway validados.
- Segurança e permissões cobertas por casos positivos/negativos.
- OpenAPI, issues, plano e logs atualizados.

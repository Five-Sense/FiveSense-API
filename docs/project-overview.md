# Visão geral do projeto

## Problema

A empresa precisa coordenar atividades de 5S sem depender de controles dispersos. É necessário saber quem pode operar o sistema, quais equipes e horários existem, quais equipes estão em atividade, quais materiais estão disponíveis, quando o estoque se aproxima do mínimo e quais ocorrências foram reportadas.

## Público-alvo

- **Administrador:** mantém usuários e seus níveis de acesso.
- **Gestor:** mantém problemas, equipes, representantes, materiais e estoque; recebe ocorrências.
- **Visualizador:** consulta dados, altera o status da própria equipe e registra ocorrências.
- **Equipe de desenvolvimento, qualidade e operação:** mantém e opera a API.

## Objetivo

Entregar uma API REST segura e documentada que centralize o gerenciamento de 5S de uma única empresa, mantendo o MVP simples, testável e operável com Java, Spring, PostgreSQL 18, Flyway e Docker.

## Escopo inicial

- Autenticação por token e autorização por perfil.
- Recuperação e alteração de senha.
- Gestão de usuários pelo Administrador.
- Gestão de problemas pelo Gestor e consulta pelo Visualizador.
- Registro de ocorrências ligadas a problema e material, com imagem opcional e e-mail.
- Gestão de equipes, representantes, horários e status de 5S.
- Calendário de horários de 5S.
- Gestão e consulta de materiais, estoque mínimo/máximo e alertas.
- OpenAPI, validação, migrações, logs seguros, testes e execução em Docker.

## Fora do escopo do MVP

- Multi-tenancy ou isolamento entre empresas.
- Microservices, event bus, CQRS ou modular monolith formal.
- Frontend, aplicativo móvel ou identidade visual neste repositório.
- CMS/editor e exportação de PDF.
- Analytics avançado, BI, auditoria regulatória completa ou workflows configuráveis.
- Replicação white-label por cliente.

## Critérios de sucesso

- Os fluxos aprovados de RF001-RF033 funcionam conforme papéis e regras definidos.
- Cada service possui testes unitários para seus métodos públicos.
- Cada fluxo implementado possui teste de integração do PostgreSQL ao controller.
- Fitness functions bloqueiam ciclos e complexidade excessiva.
- O schema é reproduzível por Flyway e o ambiente por Docker.
- Contratos HTTP são validados e documentados.

## Restrições e riscos

- O PDF contém requisitos duplicados e contraditórios; eles estão destacados em `requirements.md`.
- JWT, integração de e-mail, imagens e política de estoque exigem decisões antes da implementação.
- O PDF fonte está fora do repositório, reduzindo a rastreabilidade; sua inclusão versionada deve ser decidida.

# Visão geral do projeto

## Problema

A empresa precisa coordenar atividades de 5S sem depender de controles dispersos. É necessário saber quem pode operar o sistema, quais equipes e horários existem, quais equipes estão em atividade, quais materiais estão disponíveis, quando o estoque se aproxima do mínimo e quais ocorrências foram reportadas.

## Público-alvo

- **Administrador:** administra usuários e cria contas de Gestor e Visualizador.
- **Gestor:** cria e gerencia contas Visualizador; mantém problemas, equipes, nomes textuais de representantes, materiais e estoque; recebe notificações de ocorrência junto com Admins.
- **Visualizador:** usuário de baixa permissão para o totem; consulta equipes, status, representantes e estoque; pode registrar ocorrência/alerta e alterar status 5S enviando o ID da equipe. Não possui vínculo persistido com uma equipe.
- **Equipe de desenvolvimento, qualidade e operação:** mantém e opera a API.

## Objetivo

Entregar uma API REST segura e documentada que centralize o gerenciamento de 5S de uma única empresa, mantendo o MVP simples, testável e operável com Java, Spring, PostgreSQL 18, Flyway e Docker.

## Escopo inicial

- Login simples por e-mail e senha; chamadas da API abertas nesta avaliação.
- Recuperação e alteração de senha.
- Gestão de usuários: Admin cria Gestores e Visualizadores; Gestor cria e gerencia Visualizadores.
- Gestão de problemas pelo Gestor e consulta pelo Visualizador.
- Registro de ocorrências ligadas a problema e material, com notificação por e-mail; imagens fora do escopo da API.
- Gestão de equipes, representantes, horários e status de 5S.
- Organização/exibição dos horários atribuídos a cada equipe, sem calendário de eventos.
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
- Fitness functions bloqueiam ciclos e dependências proibidas; métricas quantitativas ainda são verificadas manualmente.
- O schema é reproduzível por Flyway e o ambiente por Docker.
- Contratos HTTP são validados e documentados.

## Restrições e riscos

- O PDF contém requisitos duplicados e contraditórios; eles estão destacados em `requirements.md`.
- A integração PostgreSQL exige credenciais do Supabase configuradas. E-mails são best effort. Imagens e dashboards estão fora do escopo; quantidade afetada de ocorrência não altera estoque.
- O PDF fonte está fora do repositório, reduzindo a rastreabilidade; sua inclusão versionada deve ser decidida.

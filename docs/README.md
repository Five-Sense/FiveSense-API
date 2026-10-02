# Documentação da Five Sense API

Este diretório contém a fonte canônica de produto, arquitetura, dados, segurança, qualidade, operação e governança da Five Sense API.

## Ordem de leitura

1. `../AGENTS.md`
2. `project-overview.md`
3. `requirements.md`
4. `architecture.md`
5. `ai-workflow.md`
6. `coding-standards.md`
7. `testing.md`

## Documentos mantidos

| Documento | Responsabilidade |
| --- | --- |
| `project-overview.md` | Objetivo, público, escopo e limites. |
| `requirements.md` | Rastreabilidade do PDF, regras, matriz de acesso e ambiguidades residuais. |
| `api-contract.md` | Inventário das rotas implementadas, acesso por papel e convenções HTTP. |
| `architecture.md` | Monólito MVC, pacotes e dependências. |
| `domains.md` | Capacidades e relações entre domínios. |
| `data-model.md` | Entidades, relações e migrações aplicadas. |
| `security.md` | Auth, perfis, senhas, tokens, secrets e logs; imagens/uploads estão fora do escopo atual. |
| `workflows.md` | Fluxos operacionais principais. |
| `testing.md` | Unitários, integração e fitness functions. |
| `coding-standards.md` | Convenções Java/Spring. |
| `operations.md` | Ambiente local, configuração e backup. |
| `deploy.md` | Ambientes, publicação e rollback. |
| `templates.md` | Recipes técnicas recorrentes. |
| `context-strategy.md` | Estratégia de leitura por agentes. |
| `tooling-adapters.md` | Regras para adaptadores como `CLAUDE.md`. |
| `implementation-governance.md` | Travas de escopo e avanço. |
| `implementation-plan.md` | Fases e critérios globais. |
| `issues.md` | Trabalho e estado vivo. |
| `backlog.md` | Ideias ainda não aprovadas. |
| `decision-log.md` | Decisões duradouras. |
| `deployment-log.md` | Entregas e validações executadas. |

## Documentos propostos para remoção

Nenhum arquivo foi removido. A aprovação humana é necessária para:

| Documento | Motivo da proposta |
| --- | --- |
| `design-guidelines.md` | O escopo atual é exclusivamente backend; não há UI neste repositório. |
| `editor.md` | Não há CMS, rich text ou conteúdo editável por não técnicos. |
| `pdf-export.md` | O MVP não gera PDFs ou documentos imprimíveis. |
| `new-client-workflow.md` | O MVP é single-company e não cria instâncias por cliente. |
| `client-launch-checklist.md` | Os checks aplicáveis já pertencem a `operations.md` e `deploy.md`. |
| `../HELP.md` | Arquivo genérico do Spring Initializr; não é documentação canônica do produto. |

`CLAUDE.md` permanece por enquanto. Se Claude Code não for usado, sua remoção também pode ser aprovada e as referências serão limpas.

## Fronteira entre históricos

- `decision-log.md`: motivo de decisões duradouras ou propostas.
- `deployment-log.md`: o que foi alterado e quais checks rodaram.
- `issues.md`: status operacional das tarefas.

Qualquer nova rota documental deve ser incluída neste índice, no `README.md` e no `AGENTS.md`.

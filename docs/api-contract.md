# Contrato HTTP da API

Este documento descreve as rotas públicas implementadas. OpenAPI em `/v3/api-docs` é a referência gerada mais próxima do código; alterações de rota, payload ou autorização devem atualizar este inventário.

## Convenções

- Prefixo `/api/v1`; JSON UTF-8.
- Autenticação por `Authorization: Bearer <JWT>` salvo rotas marcadas públicas.
- Erros usam RFC 9457 `ProblemDetail`.
- Listagens aceitam `page` (padrão 0) e `size` (padrão 20, máximo 100), com ordenação fixa por endpoint.
- Requests são validados por Bean Validation; identificadores são UUID.

## Autenticação e provisionamento

| Método | Rota | Acesso | Resultado |
| --- | --- | --- | --- |
| POST | `/auth/login` | Público | Token de acesso; token de troca obrigatória quando `mustChangePassword` |
| POST | `/auth/refresh` | Público com refresh token | Rotaciona refresh do Viewer |
| POST | `/auth/change-password` | Autenticado, inclusive challenge `PASSWORD_CHANGE` | Atualiza senha e emite sessão normal |
| POST | `/auth/logout` | Autenticado | Revoga sessão atual |
| POST | `/auth/password-reset/request` | Público | Resposta neutra; dispara e-mail elegível |
| POST | `/auth/password-reset/confirm` | Público com token | Consome token, troca senha e revoga sessões |
| POST | `/bootstrap/admin` | Público com `X-Bootstrap-Secret` | Revela uma vez a senha do Admin automaticamente provisionado no startup |

## Usuários

| Método | Rota | Acesso |
| --- | --- | --- |
| GET | `/users?page&size` | ADMIN/MANAGER; Manager vê somente VIEWER |
| GET | `/users/{id}` | ADMIN; Manager somente VIEWER |
| POST | `/users` | ADMIN cria MANAGER/VIEWER; Manager cria VIEWER |
| PUT | `/users/{id}` | ADMIN; Manager somente VIEWER |
| POST | `/users/{id}/resend-initial-password` | ADMIN; Manager somente VIEWER em `FIRST_ACCESS` |

## Operação 5S

| Método | Rota | Acesso |
| --- | --- | --- |
| GET | `/teams?page&size` | Todos os papéis |
| GET | `/teams/{id}` | Todos os papéis |
| POST | `/teams` | MANAGER |
| PUT | `/teams/{id}` | MANAGER |
| PATCH | `/teams/{id}/status` | VIEWER; request envia o status e a equipe vem do ID da rota |
| DELETE | `/teams/{id}` | MANAGER |
| GET | `/problems?page&size` | ADMIN/MANAGER |
| GET | `/problems/{id}` | ADMIN/MANAGER |
| GET | `/problems/options` | VIEWER; id/nome ativos para selecionar ocorrência |
| POST/PUT/DELETE | `/problems[/{id}]` | MANAGER |
| GET | `/materials?page&size` | ADMIN/MANAGER |
| GET | `/materials/{id}` | ADMIN/MANAGER |
| GET | `/materials/stock` | Todos; visão de estoque sem mutação |
| GET | `/materials/options` | VIEWER; id/nome ativos para selecionar ocorrência |
| POST/PUT/DELETE | `/materials[/{id}]` | MANAGER |
| POST | `/occurrences` | VIEWER; cria ocorrência/alerta sem alterar estoque |
| GET | `/occurrences?page&size` | ADMIN/MANAGER |

## Limites conhecidos

- A resposta de erro para validação, autorização, conflito e recurso inexistente deve manter status HTTP semanticamente correto.
- E-mail é disparado depois do commit e tratado como best effort; não há outbox/retry durável nesta versão.
- O bootstrap deve ser exposto somente em rede/control plane de provisionamento; segredo e resposta são altamente sensíveis.
- As rotas de consulta a problema/material para Viewer retornam somente dados mínimos de seleção, nunca detalhes completos.

# Contrato HTTP da Five Sense API

## Modo atual

Este contrato descreve a API usada na avaliação mobile. Login compara e-mail e senha diretamente. Consulte `login.md` para o fluxo de cadastro e login.

## Convenções

- Prefixo `/api/v1`; JSON UTF-8; identificadores UUID.
- Todas as operações são públicas. Os campos `role` e `status` são dados de usuário e não controlam acesso.
- Listagens aceitam `page` (padrão 0) e `size` (padrão 20, máximo 100).
- Erros de negócio usam `ProblemDetail`; validação retorna `400` com propriedade `errors`.
- Site navegável: [`site/index.html`](site/index.html).
- OpenAPI: `/v3/api-docs`; Swagger UI: `/swagger-ui/index.html`.

## Autenticação e usuários

| Método | Rota | Contrato | Resultado |
| --- | --- | --- | --- |
| POST | `/auth/login` | `{ "email": "ana@empresa.com", "password": "senha" }` | `{ "authenticated": true, "userId": "<uuid>", "name": "Ana", "email": "ana@empresa.com", "role": "VIEWER" }` |
| GET | `/users?page=0&size=20` | Query `page`, `size` | Página de usuários, todas as rotas públicas |
| GET | `/users/{id}` | Path `id` UUID | Usuário |
| POST | `/users` | `{ "name": "Ana", "email": "ana@empresa.com", "password": "senha", "role": "VIEWER" }` | `201 Created`, usuário ativo |
| PUT | `/users/{id}` | `{ "name": "Ana", "email": "ana@empresa.com", "status": "ACTIVE" }` | Usuário atualizado |

O cadastro público do primeiro usuário é também o onboarding inicial. A resposta do usuário não inclui senha.

## Operação 5S

| Método | Rota | Entrada/resultado |
| --- | --- | --- |
| GET | `/teams?page&size` | Página de equipes |
| GET | `/teams/{id}` | Equipe por UUID |
| POST | `/teams` | `UpsertRequest`; `201 Created` |
| PUT | `/teams/{id}` | `UpsertRequest` |
| PATCH | `/teams/{id}/status` | `{ "status": "DOING_5S" }` |
| DELETE | `/teams/{id}` | `204 No Content` |
| GET | `/problems?page&size` | Página de problemas |
| GET | `/problems/{id}` | Detalhe do problema |
| GET | `/problems/options` | Opções `id`/`name` para ocorrência |
| POST/PUT/DELETE | `/problems[/{id}]` | CRUD; create `201`, delete `204` |
| GET | `/materials?page&size` | Página de materiais |
| GET | `/materials/{id}` | Detalhe e indicador `lowStock` |
| GET | `/materials/stock` | Visão geral do estoque |
| GET | `/materials/options` | Opções `id`/`name` para ocorrência |
| POST/PUT/DELETE | `/materials[/{id}]` | CRUD; create `201`, delete `204` |
| POST | `/occurrences/images` | `multipart/form-data` com campo `image` (máx. 5MB); `201 Created` com `{ "imageId": "<uuid>" }` |
| POST | `/occurrences` | `{ "problemId": "<uuid>", "materialId": "<uuid>", "affectedQuantity": 2, "reportedByUserId": "<uuid>", "imageId": "<uuid>" }` (`imageId` opcional); `201 Created` |
| GET | `/occurrences?page&size` | Página de ocorrências |

Ocorrências não alteram estoque. E-mail permanece best effort conforme configuração SMTP. Quando a ocorrência informa `imageId`, a imagem é lida do arquivo temporário correspondente, anexada ao e-mail dos destinatários e do contato do problema, e o arquivo é excluído após o envio. A imagem nunca é persistida no banco.

## DTOs principais e validação

- `User CreateRequest`: `name`, `email`, `password` (até 255), `role` (`ADMIN`, `MANAGER` ou `VIEWER`). `UpdateRequest`: `name`, `email`, `status`.
- `Team UpsertRequest`: `name` ou `code` obrigatório; `representatives` e `schedule` texto até 255. `StatusRequest.status`: enum de status 5S.
- `Problem UpsertRequest`: nome até 20, e-mail relacionado, resposta padrão e `active` opcional.
- `Material UpsertRequest`: nome até 55, quantidades 0–999 e `active` opcional.
- `Occurrence CreateRequest`: IDs de problema, material e usuário; `affectedQuantity` positivo; `imageId` opcional, obtido no upload temporário.
- `Occurrence image upload`: `multipart/form-data` com campo `image` obrigatório (arquivo não vazio, limite de 5MB). Resposta `ImageUploadResponse { imageId }`.
- Respostas paginadas incluem `content`, `totalElements`, `totalPages`, `size` e `number`.

## Respostas e erros

- Criações retornam `201`; exclusões retornam `204`.
- Credencial incorreta retorna `400` com `ProblemDetail`.
- Validação retorna `400`; recurso ausente, `404`; conflito, `409`.

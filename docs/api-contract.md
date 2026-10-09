# Contrato HTTP da Five Sense API

## Modo atual

Este contrato descreve a API usada na avaliação mobile. Login compara `username` e senha diretamente. O acesso é controlado por perfil (`role`) enviado pelo cliente. Consulte `login.md` para o fluxo de cadastro e login.

## Convenções

- Prefixo `/api/v1`; JSON UTF-8; identificadores UUID.
- Exceto `/auth/**`, toda chamada deve enviar o header `X-User-Role` (`ADMIN`, `MANAGER` ou `VIEWER`), valor recebido no login. Header ausente ou inválido retorna `401`.
- `POST`, `PUT` e `DELETE` em `/users`, `/problems`, `/materials` e `/teams` exigem `ADMIN` ou `MANAGER`; `VIEWER` recebe `403`. Todo o resto (consultas, ocorrências, `PATCH /teams/{id}/status`, `PATCH /materials/{id}/stock`) aceita os três perfis.
- Controle didático: o header não é autenticado e pode ser forjado. Não use em produção.
- Listagens aceitam `page` (padrão 0) e `size` (padrão 20, máximo 100).
- Erros de negócio usam `ProblemDetail`; validação retorna `400` com propriedade `errors`.
- Site navegável: [`site/index.html`](site/index.html).
- OpenAPI: `/v3/api-docs`; Swagger UI: `/swagger-ui/index.html`.

## Autenticação e usuários

| Método | Rota | Contrato | Resultado |
| --- | --- | --- | --- |
| POST | `/auth/login` | `{ "username": "admin", "password": "admin123" }` | `{ "authenticated": true, "userId": "<uuid>", "name": "admin", "username": "admin", "email": "admin@gmail.com", "role": "ADMIN" }` |
| GET | `/users?page=0&size=20` | Query `page`, `size` | Página de usuários (qualquer perfil) |
| GET | `/users/{id}` | Path `id` UUID | Usuário |
| POST | `/users` | `{ "name": "Ana", "username": "ana", "email": "ana@empresa.com", "password": "senha", "role": "VIEWER" }` | `201 Created`, usuário ativo (ADMIN/MANAGER) |
| PUT | `/users/{id}` | `{ "name": "Ana", "username": "ana", "email": "ana@empresa.com", "status": "ACTIVE" }` | Usuário atualizado (ADMIN/MANAGER) |

O admin padrão (`admin` / `admin@gmail.com` / `admin123`) é criado pela migration V2 e é o onboarding inicial. `username` é normalizado em minúsculas e é único. O e-mail só tem o formato validado; não há confirmação por e-mail. A resposta do usuário não inclui senha.

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
| POST/PUT/DELETE | `/materials[/{id}]` | CRUD; create `201`, delete `204` (ADMIN/MANAGER) |
| PATCH | `/materials/{id}/stock` | `{ "delta": -3 }` (inteiro não zero; negativo retira, positivo repõe). Resultado deve ficar entre 0 e 999, senão `400`. Dispara alerta de estoque baixo ao cruzar o mínimo. Qualquer perfil. |
| POST | `/occurrences/images` | `multipart/form-data` com campo `image` (máx. 5MB); `201 Created` com `{ "imageId": "<uuid>" }` |
| POST | `/occurrences` | `{ "problemId": "<uuid>", "materialId": "<uuid>", "affectedQuantity": 2, "reportedByUserId": "<uuid>", "imageId": "<uuid>" }` (`imageId` opcional); `201 Created` |
| GET | `/occurrences?page&size` | Página de ocorrências |

Ocorrências não alteram estoque. E-mail permanece best effort conforme configuração SMTP. Quando a ocorrência informa `imageId`, a imagem é lida do arquivo temporário correspondente, anexada ao e-mail dos destinatários e do contato do problema, e o arquivo é excluído após o envio. A imagem nunca é persistida no banco.

## DTOs principais e validação

- `User CreateRequest`: `name`, `username`, `email`, `password` (até 255), `role` (`ADMIN`, `MANAGER` ou `VIEWER`). `UpdateRequest`: `name`, `username`, `email`, `status`. `LoginRequest`: `username`, `password`.
- `Team UpsertRequest`: `name` ou `code` obrigatório; `representatives` e `schedule` texto até 255. `StatusRequest.status`: enum de status 5S.
- `Problem UpsertRequest`: nome até 20, e-mail relacionado, resposta padrão e `active` opcional.
- `Material UpsertRequest`: nome até 55, quantidades 0–999 e `active` opcional.
- `Occurrence CreateRequest`: IDs de problema, material e usuário; `affectedQuantity` positivo; `imageId` opcional, obtido no upload temporário.
- `Occurrence image upload`: `multipart/form-data` com campo `image` obrigatório (arquivo não vazio, limite de 5MB). Resposta `ImageUploadResponse { imageId }`.
- Respostas paginadas incluem `content`, `totalElements`, `totalPages`, `size` e `number`.

## Respostas e erros

- Criações retornam `201`; exclusões retornam `204`.
- Credencial incorreta retorna `400` com `ProblemDetail`; `X-User-Role` ausente/inválido, `401`; perfil sem permissão, `403`.
- Validação retorna `400`; recurso ausente, `404`; conflito, `409`.

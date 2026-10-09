# Modelo de dados atual

Este é o modelo aprovado e implementado pela migração Flyway atual. Mudanças futuras exigem nova migração.

## User

- `id: UUID`
- `name: varchar(255)`, obrigatório
- `email: varchar(255)`, obrigatório e único, normalizado
- `password: varchar(255)`, obrigatório; valor salvo em texto puro por decisão expressa para avaliação mobile.
- `username` (único, minúsculas, usado no login)
- `role: ADMIN | MANAGER | VIEWER`
- `status: ACTIVE | INACTIVE`; usuários cadastrados pela API iniciam em `ACTIVE`.
- `VIEWER` não tem vínculo com Team; o totem envia o ID da equipe-alvo ao alterar o status 5S
- `createdAt`, `updatedAt`

## Team

- `id: UUID`
- `name: varchar(255)`, opcional se `code` existir
- `code: varchar(255)`, opcional se `name` existir
- `representatives: varchar(255)`, nomes para exibição no totem, mantidos pelo Gestor; não é relação com User nem entidade própria
- `schedule: varchar(255)`, horário exibido/organizado para a equipe como texto simples, sem eventos de calendário.
- `status: DOING_5S | NOT_DOING_5S`; padrão `NOT_DOING_5S`.
- `createdAt`, `updatedAt`

Constraint: pelo menos `name` ou `code`; `name` e `code` são únicos sem diferenciar maiúsculas/minúsculas quando informados.

## Problem

- `id: UUID`
- `name: varchar(20)`, obrigatório
- `relatedEmail: varchar(255)`, obrigatório
- `defaultResponse: varchar(255)`, obrigatório (limite padrão)
- `active: boolean`
- `createdAt`, `updatedAt`

## Material

- `id: UUID`
- `name: varchar(55)`, obrigatório
- `stockQuantity: integer`, entre 0 e 999
- `minimumStock: integer`, entre 0 e 999 e não maior que o estoque máximo adotado
- `active: boolean`
- `createdAt`, `updatedAt`

## Occurrence

- `id: UUID`
- `problemId: UUID`
- `materialId: UUID`
- `reportedByUserId: UUID`
- `affectedQuantity: integer`, maior que zero
- `createdAt`

Imagens de ocorrência/material não fazem parte do escopo atual da API, apesar de RF016/RN013/RN017. A quantidade afetada não altera `Material.stockQuantity`. Ocorrências são registros append-only sem ciclo de status. Usuários ativos `ADMIN` e `MANAGER` recebem notificação; o endereço associado ao problema também recebe sua resposta padrão.

## Relações

- `VIEWER` não possui relação de associação com Team. O endpoint recebe o ID da equipe a alterar.
- Problem 1:N Occurrence.
- Material 1:N Occurrence.
- User 1:N Occurrence como autor.
- O login não cria registros adicionais no banco.

## Migrações

- Flyway em `src/main/resources/db/migration`; `V1` define o schema atual.
- Uma migração aplicada nunca é editada; correções usam nova versão.
- Constraints e índices relevantes pertencem ao banco, não apenas ao Java.
- Seeds de desenvolvimento devem ser separados de migrações de produção.
- Alteração estrutural relevante atualiza este arquivo e, se duradoura, o decision log.

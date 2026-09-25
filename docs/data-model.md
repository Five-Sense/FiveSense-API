# Modelo de dados proposto

Este modelo é uma proposta para revisão. Campos marcados como pendentes não devem virar migração antes de decisão humana.

## User

- `id: UUID`
- `name: varchar`, obrigatório
- `email: varchar(255)`, obrigatório e único, normalizado
- `passwordHash: varchar`, obrigatório
- `role: ADMIN | MANAGER | VIEWER`
- `status: ACTIVE | INACTIVE | FIRST_ACCESS`
- `createdAt`, `updatedAt`

## PasswordResetToken

- `id: UUID`
- `userId: UUID`
- `tokenHash: varchar`, único
- `expiresAt`
- `usedAt`, opcional
- `createdAt`

O token bruto não deve ser persistido. Expiração: 10 minutos.

## Team

- `id: UUID`
- `name: varchar`, opcional se `code` existir
- `code: varchar`, opcional se `name` existir
- `status: DOING_5S | NOT_DOING_5S` (proposta)
- `createdAt`, `updatedAt`

Constraint: pelo menos `name` ou `code`.

## TeamRepresentative

- `teamId: UUID`
- `userId: UUID`
- `createdAt`

Depende da confirmação de que representantes são usuários e do limite por equipe.

## FiveSSchedule

- `id: UUID`
- `teamId: UUID`
- dia/turno, hora inicial/final e timezone: contrato pendente

## Problem

- `id: UUID`
- `name: varchar(20)`, obrigatório
- `relatedEmail: varchar(255)`, obrigatório
- `defaultResponse: text`, obrigatório
- `active: boolean`
- `createdAt`, `updatedAt`

## Material

- `id: UUID`
- `name: varchar(55)`, obrigatório
- `stockQuantity: integer`, entre 0 e 999
- `minimumStock: integer`, entre 0 e 999 e não maior que o estoque máximo adotado
- `imageKey`, `imageContentType`, `imageSize`: contrato pendente
- `active: boolean`
- `createdAt`, `updatedAt`

## Occurrence

- `id: UUID`
- `problemId: UUID`
- `materialId: UUID`
- `reportedByUserId: UUID`
- `affectedQuantity: integer`, maior que zero
- metadata da imagem opcional
- `createdAt`

O efeito sobre `Material.stockQuantity` e o status/ciclo de vida da ocorrência estão pendentes.

## Relações

- User N:N Team por TeamRepresentative.
- Team 1:N FiveSSchedule.
- Problem 1:N Occurrence.
- Material 1:N Occurrence.
- User 1:N Occurrence como autor.
- User 1:N PasswordResetToken.

## Migrações

- Flyway em `src/main/resources/db/migration`.
- Uma migração aplicada nunca é editada; correções usam nova versão.
- Constraints e índices relevantes pertencem ao banco, não apenas ao Java.
- Seeds de desenvolvimento devem ser separados de migrações de produção.
- Alteração estrutural relevante atualiza este arquivo e, se duradoura, o decision log.

# Modelo de dados proposto

Este é o modelo aprovado e implementado pela migração Flyway atual. Mudanças futuras exigem nova migração.

## User

- `id: UUID`
- `name: varchar(255)`, obrigatório
- `email: varchar(255)`, obrigatório e único, normalizado
- `passwordHash: varchar(255)`, obrigatório
- `role: ADMIN | MANAGER | VIEWER`
- `status: ACTIVE | INACTIVE | FIRST_ACCESS` (`FIRST_ACCESS` restringe a conta à troca obrigatória da senha temporária; conta permanece nesse estado até a troca)
- `VIEWER` não tem vínculo com Team; o totem envia o ID da equipe-alvo ao alterar o status 5S
- `createdAt`, `updatedAt`

## PasswordResetToken

- `id: UUID`
- `userId: UUID`
- `tokenHash: varchar`, único
- `expiresAt`
- `usedAt`, opcional
- `createdAt`

O token bruto não deve ser persistido. Expiração: 10 minutos.

## BootstrapState

- `id: 1`, singleton inserido pela migração.
- `consumed: boolean`, indica se a senha inicial já foi revelada.
- `initialPasswordCiphertext: text`, AES-GCM cifrado pelo segredo de bootstrap enquanto aguarda a primeira revelação; limpo no mesmo commit da revelação.
- O primeiro Admin é criado no startup a partir das variáveis de provisionamento e persiste com hash BCrypt; o valor cifrado temporário permite retomar a revelação após reiniciar o processo.

## Team

- `id: UUID`
- `name: varchar(255)`, opcional se `code` existir
- `code: varchar(255)`, opcional se `name` existir
- `representatives: varchar(255)`, nomes para exibição no totem, mantidos pelo Gestor; não é relação com User nem entidade própria
- `schedule: varchar(255)`, horário exibido/organizado para a equipe como texto simples, sem eventos de calendário.
- `status: DOING_5S | NOT_DOING_5S`; padrão `NOT_DOING_5S`.
- `createdAt`, `updatedAt`

Constraint: pelo menos `name` ou `code`; `name` e `code` são únicos sem diferenciar maiúsculas/minúsculas quando informados.

## AuthSession

- `id: UUID`
- `userId: UUID`
- `refreshTokenHash: varchar(255)`, único
- `createdAt`, `lastUsedAt`, `expiresAt`, `revokedAt`
- `sessionType`: `ACCESS_ONLY | REFRESHABLE | PASSWORD_CHANGE`
- Representa sessão revogável. Viewer tem refresh rotativo, limite ocioso de 8 horas, duração máxima de 30 dias e até cinco sessões ativas. Admin/Manager e challenge de troca obrigatória usam sessões curtas sem refresh.

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

- `VIEWER` não possui relação de associação com Team. O endpoint recebe o ID da equipe a alterar; sua permissão limita a operação à mudança do status 5S.
- Problem 1:N Occurrence.
- Material 1:N Occurrence.
- User 1:N Occurrence como autor.
- User 1:N PasswordResetToken.
- User 1:N AuthSession.

## Migrações

- Flyway em `src/main/resources/db/migration` (`V1` schema inicial; `V2` ciphertext temporário do bootstrap).
- Uma migração aplicada nunca é editada; correções usam nova versão.
- Constraints e índices relevantes pertencem ao banco, não apenas ao Java.
- Seeds de desenvolvimento devem ser separados de migrações de produção.
- Alteração estrutural relevante atualiza este arquivo e, se duradoura, o decision log.

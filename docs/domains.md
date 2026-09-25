# Domínios

Os domínios abaixo organizam pacotes de um único monólito. Eles não representam serviços separados.

## Auth

Login, JWT RS256, logout, alteração e recuperação de senha. Depende de Users e da infraestrutura de e-mail.

## Users

Usuários, estado ativo, papéis e administração de conta. É a fonte de identidade para Auth e para representantes de equipe, conforme decisão pendente.

## Teams

Equipes, representantes, horários, calendário e status de execução do 5S. Pode referenciar IDs de Users.

## Problems

Catálogo de problemas, e-mail relacionado e resposta padrão. É referenciado por Occurrences.

## Materials

Materiais, imagem, quantidade, limites de estoque e alertas. É referenciado por Occurrences.

## Occurrences

Registro de problema, material, quantidade afetada, imagem opcional, autor e notificação. O efeito sobre estoque ainda não foi definido.

## Matriz de dependência

| Origem | Pode depender de |
| --- | --- |
| Auth | Users, infraestrutura compartilhada de segurança/e-mail |
| Users | Shared |
| Teams | Users, Shared |
| Problems | Shared |
| Materials | Shared |
| Occurrences | Users, Problems, Materials, infraestrutura de e-mail/imagem |

Dependências inversas não devem surgir por acesso direto a repository. Comunicação entre domínios ocorre por service público ou identificadores.

## Regras

- Novo domínio exige necessidade concreta.
- Uma entidade tem um único domínio dono.
- Shared não pode virar depósito genérico.
- Ciclos são falha de arquitetura.

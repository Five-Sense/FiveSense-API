# Domínios

Os domínios abaixo organizam pacotes de um único monólito. Eles não representam serviços separados.

## Auth

Login, JWT RS256, logout, alteração e recuperação de senha. Depende de Users e da infraestrutura de e-mail.

## Users

Usuários, estado ativo, papéis e administração de conta. Admin cria contas MANAGER e VIEWER; Manager cria e gerencia VIEWER. Viewer não é associado a uma equipe.

## Teams

Equipes, representantes textuais (`varchar(255)`), horários organizados por equipe e status 5S. Não é calendário de eventos nem possui entidades de representante; Viewer envia o ID alvo ao alterar status.

## Problems

Catálogo de problemas, e-mail relacionado e resposta padrão. É referenciado por Occurrences.

## Materials

Materiais, quantidade, limites de estoque e alertas. Imagens fora do escopo atual. É referenciado por Occurrences.

## Occurrences

Registro de problema, material, quantidade afetada, autor e notificação. Imagens fora do escopo; quantidade afetada não altera estoque por decisão humana.

## Matriz de dependência

| Origem | Pode depender de |
| --- | --- |
| Auth | Users, infraestrutura compartilhada de segurança/e-mail |
| Users | Shared |
| Teams | Shared |
| Problems | Shared |
| Materials | Shared |
| Occurrences | Users, Problems, Materials, infraestrutura de e-mail |

Dependências inversas não devem surgir por acesso direto a repository. Comunicação entre domínios ocorre por service público ou identificadores.

## Regras

- Novo domínio exige necessidade concreta.
- Uma entidade tem um único domínio dono.
- Shared não pode virar depósito genérico.
- Ciclos são falha de arquitetura.

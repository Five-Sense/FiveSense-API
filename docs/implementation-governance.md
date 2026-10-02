# Governança de implementação

## Estado atual

A Fase 0 está concluída com escopo e decisões funcionais aprovados. A implementação do MVP está em revisão de qualidade; evolução dentro do escopo aprovado pode seguir. Mudanças de produto, arquitetura ou segurança que alterem decisões aceitas exigem nova decisão humana.

## Regras

- Implementar um fluxo vertical pequeno por issue.
- Não misturar feature, refatoração ampla e mudança arquitetural.
- Nova dependência exige justificativa e aprovação.
- Mudança de schema exige data model e Flyway.
- Mudança de contrato exige OpenAPI e testes.
- Toda implementação termina com unitários, integração e fitness functions.
- Sincronizar issue, plano e deployment log.

## Travas

Pausar quando:

- requisito é contraditório ou omisso;
- solução altera arquitetura, dado ou segurança;
- operação pode perder histórico;
- métrica exige exceção;
- escopo cresce além da issue;
- remoção documental foi proposta, mas não aprovada.

## Avanço de fase

Uma fase só conclui quando:

- checklist obrigatório está encerrado;
- decisões bloqueadoras foram aceitas;
- checks foram executados ou a impossibilidade foi aceita;
- issues estão atualizadas;
- entrega foi registrada.

O check de integração PostgreSQL não é considerado executado quando Testcontainers é ignorado por indisponibilidade do Docker; registrar como bloqueio ambiental e repetir antes do aceite técnico final.

## Remoções pendentes

Não remover `design-guidelines.md`, `editor.md`, `pdf-export.md`, `new-client-workflow.md`, `client-launch-checklist.md` ou `HELP.md` antes da aprovação humana. Após aprovação, limpar referências em README, AGENTS, índice e docs relacionados.

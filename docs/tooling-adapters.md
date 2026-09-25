# Adaptadores de ferramentas

`AGENTS.md` é o contrato universal; `docs/*` contém detalhes. Adaptadores apenas apontam ferramentas para essas fontes.

## Hierarquia

1. `AGENTS.md`
2. `docs/*`
3. Adaptador da ferramenta

## Regras

- Ser curto e específico.
- Não duplicar regras completas.
- Não contradizer documentos canônicos.
- Exigir aprovação da Fase 0 antes de código.
- Remoção de adaptador Markdown exige aprovação humana e limpeza de referências.

## Claude Code

`CLAUDE.md` está mantido como ponte. Se Claude Code não fizer parte do fluxo, sua remoção pode ser aprovada.

## Codex e outras ferramentas

Quando a ferramenta lê `AGENTS.md` nativamente, não criar adaptador adicional. Um novo adaptador só deve existir se resolver uma limitação real.

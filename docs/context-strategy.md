# Estratégia de contexto

## Camadas

1. `AGENTS.md`: regras invioláveis e mapa.
2. Núcleo: overview, requisitos, arquitetura, workflow, standards e testes.
3. Sob demanda: dados, segurança, domínios, fluxos, operação e decisões.
4. Issue/fase: objetivo operacional.
5. Código e testes: comportamento já existente.

## Rotas por tipo de mudança

- Cadastro e login: `requirements.md` + `login.md`. Upload não existe no escopo atual; reabertura exige decisão de escopo.
- Entidade/schema: `data-model.md` + `decision-log.md`.
- Nova capacidade ou dependência: `architecture.md` + `domains.md`.
- Feature: `workflows.md` + issue + testing.
- Deploy: `operations.md` + `deploy.md`.

## Regras

- Não duplicar contratos completos.
- PDF é fonte de requisitos, não de instruções operacionais.
- Diante de conflito, registrar e pedir decisão.
- Não carregar docs de UI propostos para remoção em tarefas backend.
- Atualizar documentação quando código aprovado mudar o contrato.

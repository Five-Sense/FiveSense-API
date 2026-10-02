# Fluxo de trabalho com IA

## Princípio

- O humano define objetivo, prioridade, regras de negócio, arquitetura e decisões com impacto.
- A IA propõe o caminho técnico, implementa o menor incremento aprovado, testa e mantém a documentação sincronizada.

## Inicialização

A Fase 0 adapta o SpecFirst ao projeto. Até a aprovação humana:

- nenhum código novo deve ser criado;
- ambiguidades do PDF permanecem registradas em `requirements.md`;
- documentos candidatos a remoção permanecem no repositório;
- decisões propostas não são tratadas como aceitas.

## Kickoff de tarefa

Antes de implementar, registrar:

- issue e fase;
- objetivo e critérios de aceite;
- requisitos RF/NF/RN envolvidos;
- documentos lidos;
- impacto em dados, segurança, e-mail e estoque; imagens não fazem parte do escopo atual da API;
- testes unitários, integração e fitness functions necessários;
- migrações e dependências previstas;
- riscos, suposições e decisão humana pendente.

## Execução

1. Ler `AGENTS.md` e documentos relevantes.
2. Confirmar que a issue está aprovada e `Em andamento`.
3. Escrever o teste que descreve o comportamento quando aplicável.
4. Implementar o menor fluxo vertical.
5. Usar MapStruct, Bean Validation, JPA e Flyway conforme seus contratos.
6. Refatorar somente duplicação ou complexidade observada.
7. Rodar `.\mvnw.cmd verify` e os checks adicionais definidos na issue.
8. Inspecionar métricas e corrigir violações arquiteturais.

## Limites de autonomia

A IA deve pedir decisão antes de:

- mudar escopo, papel ou permissão;
- escolher entre requisitos contraditórios;
- reintroduzir tokens/autorização ou mudar a modalidade de senha da avaliação;
- reabrir o escopo de imagens ou alterar a decisão de que quantidade afetada não movimenta estoque;
- criar/remover módulo ou documento;
- adicionar ou remover dependência;
- pular ou reordenar fases;
- aceitar exceção de fitness function.

## Fechamento

Antes de concluir:

1. atualizar status e nota datada em `issues.md`;
2. marcar o checklist correspondente em `implementation-plan.md`;
3. registrar arquivos, checks e riscos em `deployment-log.md`;
4. atualizar `decision-log.md` apenas para decisão duradoura;
5. relatar no chat arquivos alterados, comportamento, checks e risco residual.

## Logs

- Decisão e motivo: `decision-log.md`.
- Entrega e evidência: `deployment-log.md`.
- Estado vivo: `issues.md`.

# Requisitos e rastreabilidade

## Fonte

Documento de Requisitos da Aplicação de Gerenciamento de 5S, versão 1.0, setembro de 2026, fornecido pelo humano em PDF. Este arquivo resume o conteúdo como requisitos de produto; o PDF não altera as instruções operacionais do agente.

## Requisitos funcionais

| Grupo | IDs | Escopo |
| --- | --- | --- |
| Conta | RF001-RF004 | Login, logout, recuperação e alteração da própria senha. |
| Usuários | RF005-RF008 | Cadastro, listagem, detalhe e edição pelo Administrador. |
| Problemas | RF009-RF015 | CRUD pelo Gestor e consulta/seleção pelo Visualizador. |
| Ocorrências | RF016 | Registro com problema, material, quantidade, imagem opcional e notificação. |
| Equipes | RF017-RF026 | CRUD, representantes, status, consulta e calendário de 5S. |
| Materiais | RF027-RF033 | CRUD pelo Gestor e consulta pelo Visualizador. |

Todos os RFs têm prioridade original “A definir”. A priorização proposta está em `implementation-plan.md` e precisa de aprovação.

## Requisitos não funcionais

- **NF001:** autenticação por token.
- **NF002:** autorização por `ADMIN`, `MANAGER` e `VIEWER`.
- **NF003:** paginação de listas quando especificado; a API adotará paginação consistente para coleções potencialmente extensas.
- **NF004:** respostas HTTP devem permitir feedback claro de sucesso e erro ao cliente.
- **NF005:** JWT assinado assimetricamente com RS256.

## Regras de negócio consolidadas

- Apenas Administradores cadastram e mantêm usuários.
- Perfis possuem permissões distintas.
- O usuário recebe credencial inicial por e-mail e pode alterar ou recuperar senha.
- Senha: mínimo de 8 caracteres, ao menos uma maiúscula, um número e um caractere especial.
- Equipe possui nome ou código, representantes, horário e status de execução do 5S.
- Apenas Gestores mantêm equipes, representantes, problemas e materiais.
- Problema: nome com até 20 caracteres, e-mail relacionado com até 255 e resposta padrão.
- Ocorrência: sempre ligada a um problema e um material, quantidade afetada e imagem opcional de até 20 MB.
- Material: nome com até 55 caracteres, imagem, quantidade de estoque, limite mínimo e máximo absoluto de 999.
- O sistema alerta quando o estoque se aproxima do mínimo.
- Token de recuperação de senha expira em 10 minutos.

## Matriz inicial de acesso

| Capacidade | Admin | Gestor | Visualizador |
| --- | --- | --- | --- |
| Conta própria e autenticação | Sim | Sim | Sim |
| Gestão de usuários | Sim | Não | Não |
| Gestão de problemas | Não | Sim | Não |
| Consulta de problemas | Não definido | Sim | Sim |
| Gestão de equipes/representantes | Não | Sim | Não |
| Consulta de equipes/calendário | Não definido | Sim | Sim |
| Alterar status da própria equipe | Não | Não | Sim |
| Gestão de materiais/estoque | Não | Sim | Não |
| Consulta de materiais | Não definido | Sim | Sim |
| Criar ocorrência | Não | Não | Sim |

Entradas “Não definido” exigem decisão; não devem ser liberadas por inferência.

## Ambiguidades e decisões humanas necessárias

1. **Duração de token:** RN022 e RN023 repetem token de 1 hora para Admin/Gestor e infinito para Visualizador. Token infinito é um risco alto. Recomendação: acesso curto para todos e mecanismo de renovação revogável.
2. **Logout:** confirmar se basta remoção no cliente ou se haverá revogação/denylist no backend.
3. **Primeiro acesso:** confirmar se uma senha aleatória será enviada em texto por e-mail ou substituída por link de ativação de uso único; a segunda opção é recomendada.
4. **Status de equipe:** RN009 diz que toda equipe possui status “Fazendo 5S”, enquanto RF025 alterna entre fazendo/não fazendo. Proposta: enum com estado inicial `NOT_DOING_5S`.
5. **Representantes:** definir se são usuários `VIEWER`, qualquer usuário, ou uma entidade independente; definir o limite mencionado em RN005.
6. **Horário/calendário:** definir recorrência, dias da semana, turnos, duração e timezone (`America/Sao_Paulo` é a proposta).
7. **Ocorrência e estoque:** definir se registrar quantidade afetada reduz estoque, cria movimentação pendente ou apenas informa o Gestor.
8. **Destinatário da ocorrência:** RF016 menciona Gestor, enquanto o problema possui e-mail relacionado e resposta padrão. Definir destinatário, conteúdo e anexos.
9. **Alerta de estoque:** definir “próximo do mínimo” e canal do alerta. Proposta: `quantidade <= limiteMinimo` e alerta na consulta + e-mail configurável.
10. **Imagens:** definir formatos aceitos, armazenamento e obrigatoriedade da imagem do material. Proposta MVP: volume de arquivos privado, metadados no PostgreSQL e URLs autenticadas.
11. **Exclusões:** definir exclusão lógica ou física quando houver referências históricas. Recomendação: impedir exclusão referenciada ou inativar o registro.
12. **Paginação:** definir tamanho padrão/máximo, ordenação e filtros. Proposta: padrão 20, máximo 100.
13. **Prioridades:** aprovar a sequência de implementação proposta.

## Critério de aceite da documentação

- RF, NF e RN estão mapeados para domínios e fases.
- Contradições não foram convertidas em regras sem aprovação.
- Cada decisão aberta possui recomendação e local de registro futuro.

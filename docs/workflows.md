# Fluxos operacionais

## Login

1. Cliente envia o e-mail cadastrado e a senha para `POST /api/v1/auth/login`.
2. API compara o e-mail normalizado e a senha diretamente com os dados armazenados.
3. Login válido retorna `authenticated`, `userId`, nome, e-mail e papel. As chamadas da API são abertas.

## Cadastro inicial

1. O primeiro usuário e os demais são criados por `POST /api/v1/users` com `name`, `email`, `password` e `role`.
2. Contas iniciam em `ACTIVE`; o campo de papel é informativo.

## Ocorrência

1. Cliente seleciona problema/material e informa quantidade e `reportedByUserId`.
2. API valida referências e quantidade; não recebe imagem.
3. API persiste a ocorrência.
4. Quantidade afetada não altera estoque por decisão humana.
5. API notifica por e-mail todos os usuários ADMIN e MANAGER; sem anexo, pois imagens estão fora do escopo. RN012 também prevê resposta padrão ao e-mail relacionado ao problema.
6. Resposta padrão é enviada em toda ocorrência ao e-mail relacionado ao problema. Envio pós-commit é best effort, sem outbox durável ou retry automático.

## Equipe e 5S

1. Manager cria equipe com nome ou código, representantes e horário.
2. Viewer consulta equipes, status, representantes, horários organizados e estoque; não consulta usuários, dashboards ou catálogo de problemas fora da seleção necessária para ocorrência.
3. Viewer envia o `teamId` e altera o status 5S daquela equipe. Não possui relação persistida com equipe; permissão limitada à operação de status.
4. Manager consulta equipes e seus status.

## Material e estoque

1. Manager cria material com nome, quantidade inicial e mínimo; API não aceita imagem (decisão de escopo).
2. API valida intervalo 0..999.
3. Consultas indicam alerta quando quantidade é igual ou inferior ao mínimo. E-mail ocorre quando criado já baixo ou ao cruzar o mínimo; não se repete em atualizações que permanecem abaixo. Viewer pode disparar alertas criando ocorrência.
4. Atualização preserva histórico; exclusão responde 409 se material estiver referenciado por ocorrência.

## CRUDs

Problemas, equipes e materiais seguem: request validado -> service transacional -> repository -> MapStruct -> response HTTP. Exclusões de problema/material referenciados respondem 409; equipes não possuem referência de ocorrência e podem ser excluídas.

# Fluxos operacionais

## Login

1. Cliente envia o `username` cadastrado e a senha para `POST /api/v1/auth/login`.
2. API compara o e-mail normalizado e a senha diretamente com os dados armazenados.
3. Login válido retorna `authenticated`, `userId`, nome, username, e-mail e papel. O cliente passa a enviar o papel em `X-User-Role` nas demais chamadas.

## Cadastro inicial

1. O admin padrão já existe (migration V2). Admin e gestores criam os demais usuários por `POST /api/v1/users` com `name`, `username`, `email`, `password` e `role`.
2. Contas iniciam em `ACTIVE`; o campo de papel é informativo.

## Ocorrência

1. Opcional: cliente envia a imagem selecionada para `POST /api/v1/occurrences/images` (multipart/form-data). A API grava a imagem em arquivo temporário e devolve um `imageId`. A imagem não é gravada no banco.
2. Cliente seleciona problema/material e informa quantidade, `reportedByUserId` e, se houver, o `imageId` do passo anterior.
3. API valida referências e quantidade.
4. API persiste a ocorrência (sem a imagem).
5. Quantidade afetada não altera estoque por decisão humana.
6. API notifica por e-mail todos os usuários ADMIN e MANAGER e envia a resposta padrão ao e-mail relacionado ao problema. Quando a ocorrência tem `imageId`, a imagem temporária é anexada a esses e-mails. RN012 também prevê resposta padrão ao e-mail relacionado ao problema.
7. Após o envio, o arquivo temporário da imagem é excluído. O banco permanece sem a imagem.
8. Envio pós-commit é best effort, sem outbox durável ou retry automático. Se a imagem temporária já tiver expirado ou sido removida, o e-mail segue sem anexo.

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

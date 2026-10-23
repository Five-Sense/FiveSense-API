# Fluxos operacionais

## Login

1. Cliente envia o `username` cadastrado e a senha para `POST /api/v1/auth/login`.
2. API compara o e-mail normalizado e a senha diretamente com os dados armazenados.
3. Login válido retorna `authenticated`, `userId`, nome, username, e-mail e papel. O cliente passa a enviar o papel em `X-User-Role` nas demais chamadas.

## Cadastro inicial

1. O admin padrão já existe (migration V2). Admin e gestores criam os demais usuários por `POST /api/v1/users` com `name`, `username`, `email`, `password` e `role`.
2. Contas iniciam em `ACTIVE`; o campo de papel é informativo.

## Ocorrência

1. Cliente seleciona problema/material e informa quantidade, `reportedByUserId` e, se houver, até 5 imagens junto da própria requisição: JSON com `images[].data` em base64, ou multipart com partes `images` binárias ou em base64. Não há upload separado nem `imageId`.
2. API valida as imagens (tipo real, tamanho, quantidade) antes de qualquer acesso ao banco.
3. API valida referências (usuário ativo, problema ativo, material ativo) e quantidade.
4. API persiste a ocorrência (sem as imagens).
5. Quantidade afetada não altera estoque por decisão humana.
6. Depois do commit, a API enfileira os e-mails para todos os usuários ADMIN e MANAGER ativos e para o e-mail relacionado ao problema (resposta padrão, RN012). O envio roda em thread separada (`mailExecutor`): a resposta `201` não espera o SMTP.
7. Cada e-mail é um HTML formatado com Thymeleaf (`templates/email/occurrence.html`) e traz as imagens embutidas no corpo (`cid:`), com versão em texto simples como alternativa. As imagens ficam só em memória durante o envio; nada é gravado em disco ou no banco.
8. Envio é best effort, sem outbox durável ou retry automático; há timeouts de SMTP, e uma falha fica apenas nos logs. Se não houver ADMIN/MANAGER ativo, só o contato do problema é notificado e um aviso é registrado.

## Equipe e 5S

1. Manager cria equipe com nome ou código, representantes e o intervalo de horário (`startTime` e `endTime`, `HH:mm`, ex.: `07:30` e `09:00`).
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

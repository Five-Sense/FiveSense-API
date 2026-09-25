# Fluxos operacionais

## Login

1. Cliente envia e-mail e senha.
2. API valida usuário ativo e hash.
3. API emite JWT RS256 conforme política aprovada.
4. Cliente usa Bearer token; API valida assinatura, validade e permissão.

## Recuperação de senha

1. Cliente informa e-mail.
2. API responde de forma neutra.
3. Se existir usuário elegível, cria token de uso único com 10 minutos e envia link.
4. Cliente envia token, nova senha e confirmação.
5. API valida token e política, altera hash e invalida o token.

## Cadastro de usuário

1. Admin envia nome, e-mail e papel.
2. API valida unicidade e papel.
3. API cria estado de primeiro acesso.
4. API envia credencial inicial conforme decisão de segurança.

## Ocorrência

1. Viewer seleciona problema/material, informa quantidade e opcionalmente imagem.
2. API valida permissão, vínculos, quantidade e upload.
3. API persiste a ocorrência.
4. API aplica ou não efeito no estoque conforme decisão pendente.
5. API envia notificação ao destinatário aprovado.
6. Falha de e-mail não pode produzir duplicação em retry; política transacional precisa ser definida.

## Equipe e 5S

1. Manager cria equipe com nome/código, representantes e horário.
2. Viewer consulta sua equipe e calendário.
3. Somente representante autorizado alterna o status da própria equipe.
4. Manager consulta equipes e seus status.

## Material e estoque

1. Manager cria material com nome, quantidade, mínimo e imagem.
2. API valida intervalo 0..999.
3. Consultas indicam alerta conforme critério aprovado.
4. Atualização/exclusão preserva histórico conforme política pendente.

## CRUDs

Problemas, equipes e materiais seguem: request validado -> autorização -> service transacional -> repository -> MapStruct -> response HTTP. Exclusões referenciadas não serão implementadas até a política ser aprovada.

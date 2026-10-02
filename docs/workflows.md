# Fluxos operacionais

## Login

1. Cliente envia e-mail e senha.
2. API valida usuário ativo e hash.
3. API emite JWT assinado com RS256. Admin/Manager recebem access JWT de 1h. Viewer recebe access JWT de 15min e refresh token rotativo; sessão fica ociosa por 8h, expira em 30 dias e tem limite de cinco sessões ativas.
4. Cliente usa Bearer token; API valida assinatura, validade e permissão.

## Recuperação de senha

1. Cliente informa e-mail.
2. API responde de forma neutra.
3. Se existir usuário elegível, cria token de uso único com 10 minutos e envia link.
4. Cliente envia token, nova senha e confirmação.
5. API valida token e política, altera hash, invalida o token e revoga as sessões do usuário.

## Cadastro de usuário

1. Admin cria conta MANAGER ou VIEWER; Manager cria/gerencia somente VIEWER.
2. API valida unicidade e papel permitido pelo ator. VIEWER não é associado a equipe.
3. API cria estado de primeiro acesso.
4. API gera senha inicial aleatória, armazena somente o hash e envia a senha temporária ao e-mail da conta criada. Conta fica em `FIRST_ACCESS`; troca no primeiro login é obrigatória.
5. Para conta existente ainda em `FIRST_ACCESS`, Admin/Gestor autorizado pede reenvio. A API gera senha nova, invalida a anterior ao atualizar o hash e envia ao e-mail cadastrado, com intervalo mínimo de 60s e três envios por hora.
6. Até a troca obrigatória, a conta não acessa recursos de negócio. Se o envio falhar, a conta continua em `FIRST_ACCESS`; a credencial não é devolvida em resposta/log e pode ser substituída pelo fluxo de reenvio após a janela.

## Bootstrap do primeiro Admin

1. Na inicialização, se a instalação ainda não tiver ADMIN, a API cria o primeiro ADMIN em `FIRST_ACCESS` com e-mail/nome de `INITIAL_ADMIN_EMAIL`/`INITIAL_ADMIN_NAME` e senha aleatória.
2. A senha é persistida somente como BCrypt e, até revelação, fica também cifrada com AES-GCM por `BOOTSTRAP_ADMIN_SECRET` para sobreviver a reinícios.
3. `POST /api/v1/bootstrap/admin`, protegido pelo segredo configurado, revela a senha em uma única resposta e apaga o ciphertext. Restringir a rota à rede de provisionamento; remover/rotacionar o segredo após uso.

## Ocorrência

1. Viewer seleciona problema/material e informa quantidade.
2. API valida permissão, vínculos e quantidade; não recebe imagem.
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

Problemas, equipes e materiais seguem: request validado -> autorização -> service transacional -> repository -> MapStruct -> response HTTP. Exclusões de problema/material referenciados respondem 409; equipes não possuem referência de ocorrência e podem ser excluídas.

# Login e cadastro

Este documento descreve o fluxo de acesso usado na avaliação mobile.

- `POST /api/v1/auth/login` recebe `username` e `password`.
- O serviço procura o usuário pelo `username` (sem diferenciar maiúsculas) e compara a senha recebida diretamente com `app_user.password`. Usuário inativo não entra.
- Uma correspondência retorna `authenticated`, `userId`, `name`, `username`, `email` e `role`; dados incorretos retornam erro HTTP.
- Admin padrão criado pela migration `V2__username_and_default_admin.sql`: username `admin`, e-mail `admin@gmail.com`, senha `admin123`.

## Perfis (`role`)

| Perfil | Pode |
| --- | --- |
| `ADMIN` | Tudo. É igual ao `MANAGER`, mas já vem cadastrado. |
| `MANAGER` | Criar/editar/excluir usuários, problemas, materiais e equipes; visualizar; registrar ocorrências; mudar status de equipe; ajustar estoque. |
| `VIEWER` | Visualizar; registrar ocorrências (aviso de material com problema); mudar status da equipe; aumentar/diminuir estoque. Não cria nem edita usuários, problemas, materiais ou equipes. |

## Como o perfil é aplicado

- Após o login, o cliente guarda o `role` e o envia em toda chamada no header `X-User-Role`. Exceção: `/auth/**`.
- `RoleAccessInterceptor` (`auth/controller`) bloqueia `POST`/`PUT`/`DELETE` em `/users`, `/problems`, `/materials` e `/teams` para `VIEWER` (`403`). Header ausente ou inválido retorna `401`.
- `POST /api/v1/users` só é aceito para `ADMIN` e `MANAGER`. A lista de e-mails de alerta continua sendo `ADMIN` e `MANAGER` ativos.
- O e-mail é validado apenas no formato; não há confirmação. Endereços reais funcionam para o envio de alertas.

O comportamento é intencionalmente reduzido para uma atividade de avaliação mobile. O header é informado pelo cliente e pode ser forjado; a senha fica em texto puro e `forgot-password` é público. Não use esta configuração para dados reais ou implantação pública.

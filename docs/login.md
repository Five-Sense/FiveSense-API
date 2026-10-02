# Login e cadastro

Este documento descreve o fluxo de acesso usado na avaliação mobile.

- `POST /api/v1/auth/login` recebe `email` e `password`.
- O serviço procura o usuário pelo e-mail e compara a senha recebida diretamente com `app_user.password`.
- Uma correspondência retorna `authenticated`, `userId`, `name`, `email` e `role`; dados incorretos retornam erro HTTP.
- `POST /api/v1/users` cadastra usuários ativos e recebe senha como texto.
- Todas as rotas são abertas. O campo `role` é informativo.
- O cliente pode chamar os endpoints diretamente após o login.

O comportamento é intencionalmente reduzido para uma atividade de avaliação mobile. Não use esta configuração para dados reais ou implantação pública.

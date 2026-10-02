# Requisitos atuais

## Escopo

Five Sense é uma API REST para apoiar a avaliação mobile das atividades 5S de uma empresa. O backend mantém usuários, equipes, problemas, materiais, estoque e ocorrências.

## Fluxo de conta

- Criar usuário com nome, e-mail, senha e papel informativo.
- Entrar com e-mail e senha cadastrados; a API faz comparação direta e devolve os dados básicos do usuário.
- Todas as rotas ficam abertas para o cliente mobile da avaliação.

## Dados e operações

- Cada usuário tem e-mail único e estado ativo/inativo.
- Equipes mantêm representantes, horário e estado 5S em texto/campos simples.
- Materiais mantêm saldo e limite mínimo; ocorrências não alteram o saldo.
- Ocorrências referenciam problema, material e usuário responsável.
- Listagens aceitam paginação; erros seguem respostas HTTP documentadas.

## Configuração

- PostgreSQL no Supabase é o banco padrão. `DATABASE_URL`, `DATABASE_USERNAME` e `DATABASE_PASSWORD` são configurados no `.env` local.
- E-mail é opcional e usa variáveis `MAIL_*`.
- O inventário atualizado de métodos, rotas, requests e responses está em `api-contract.md` e `site/index.html`.

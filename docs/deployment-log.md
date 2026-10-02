# Registro de entregas

## [2026-10-02] — Login simples e configuração Supabase

- Login por e-mail e senha com comparação direta; cadastro cria conta ativa.
- Campo `app_user.password` recebe diretamente a senha fornecida no cadastro.
- Rotas abertas para a avaliação mobile; `role` permanece como informação da conta.
- Schema inicial do Flyway representa o modelo atual, sem tabelas de fluxo de login adicional.
- `.env.example` descreve a conexão Supabase e configurações opcionais de e-mail. `.env` local permanece ignorado pelo Git.
- Site estático de documentação disponível em `docs/site/index.html`.
- `git diff --check` executado. Build não validado: o ambiente possui Java 17 e o projeto exige Java 21. A conexão Supabase aguarda a senha do banco.

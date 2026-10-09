# Registro de entregas

## [2026-10-02] — Login simples e configuração Supabase

- Login por e-mail e senha com comparação direta; cadastro cria conta ativa.
- Campo `app_user.password` recebe diretamente a senha fornecida no cadastro.
- Rotas abertas para a avaliação mobile; `role` permanece como informação da conta.
- Schema inicial do Flyway representa o modelo atual, sem tabelas de fluxo de login adicional.
- `.env.example` descreve a conexão Supabase e configurações opcionais de e-mail. `.env` local permanece ignorado pelo Git.
- Site estático de documentação disponível em `docs/site/index.html`.
- `git diff --check` executado. Build não validado: o ambiente possui Java 17 e o projeto exige Java 21. A conexão Supabase aguarda a senha do banco.

## [2026-10-02] — Imagem temporária anexada no e-mail da ocorrência

- Novo endpoint `POST /api/v1/occurrences/images` (multipart, campo `image`, máx. 5MB) grava a imagem em arquivo temporário via `TemporaryImageStore` (`occurrences/infra`) e devolve `imageId`. O banco nunca armazena a imagem (decisão 0004).
- `OccurrenceDtos.CreateRequest` passou a aceitar `imageId` opcional. Ao criar a ocorrência, `OccurrenceService` recupera a imagem temporária, anexa ao e-mail dos ADMIN/MANAGER e do contato do problema e exclui o arquivo após o envio (pós-commit, best effort).
- `EmailService.sendWithAttachment` envia `MimeMessage` com anexo; mantém comportamento best effort (falha logada, nunca propagada).
- `application.properties`: `spring.servlet.multipart.*` (limite 5MB via `OCCURRENCE_IMAGE_MAX_SIZE`) e `occurrences.temp-image-dir` (via `OCCURRENCE_IMAGE_DIR`).
- Documentação atualizada: `architecture.md`, `workflows.md`, `api-contract.md`, `decision-log.md` (0004) e `site/index.html`.
- Verificação de e-mail solicitada: ocorrência e alerta de estoque enviam corretamente; fluxo "esqueci minha senha" não existe no escopo e não foi criado (depende de decisão humana sobre o login).
- Checks: `.\mvnw.cmd -Dtest="OccurrenceServiceTests,EmailServiceTests,TemporaryImageStoreTests,ArchitectureFitnessTests" test` com `JAVA_HOME` apontando para JDK 25 — BUILD SUCCESS, 15 testes, 0 falhas (inclui fitness functions ArchUnit). Testes de integração com Testcontainers não executados (exigem Docker).
- Risco residual: envio de e-mail e exclusão do arquivo temporário ocorrem pós-commit sem retry; se a imagem temporária for removida antes do envio, o e-mail segue sem anexo.
## [2026-10-09] — Login por username, perfis e ajuste de estoque
- Migration `V2__username_and_default_admin.sql`: coluna `app_user.username` única (usuários existentes recebem `<parte-local-do-email>-<8 chars do id>`) e inserção do admin padrão (`admin` / `admin@gmail.com` / `admin123`, texto puro) com `ON CONFLICT DO NOTHING`.
- `AppUser`, `UserDtos`, `UserService` e `UserRepository` ganharam `username` (normalizado em minúsculas, conflito retorna 409). Login passou a usar `username` e devolve `username` e `role`.
- Novo `RoleAccessInterceptor` (`auth/controller`) lê `X-User-Role`: 401 se ausente/inválido; 403 para VIEWER em POST/PUT/DELETE de `users`, `problems`, `materials` e `teams`. `/auth/**` fica fora.
- Novo `PATCH /api/v1/materials/{id}/stock` (`{ "delta": n }`) com limites 0–999 e alerta de estoque baixo.
- Docs atualizadas: `login.md`, `api-contract.md`, `workflows.md`, `data-model.md`, `requirements.md`, `decision-log.md` (0005) e `site/index.html`.
- Checks: `.\mvnw.cmd test` com JDK 25 — sucesso, inclui ArchUnit e novos testes (`AuthServiceTests`, `UserServiceTests`, `RoleAccessInterceptorTests`, `MaterialServiceTests`). Testes de integração com Testcontainers e a execução real da V2 no PostgreSQL não foram executados (Docker indisponível). Métricas de complexidade de `docs/testing.md` não executadas.
- Risco residual: o header de perfil é forjável e `forgot-password` continua público; o `.env`/banco existente precisa aplicar a V2 ao subir a aplicação.
## [2026-10-09] — Revisão do site de docs e rota /docs
- `docs/site/index.html`: exemplos de usuário com `username`; novo `POST /auth/forgot-password`; texto do `PATCH /materials/{id}/stock`, status de equipe e ocorrência revisados; painel "Autenticação e perfis"; bloco de headers (`X-User-Role`) por endpoint; etiqueta de acesso por endpoint (Público / ADMIN-MANAGER / qualquer perfil); link do topo aponta para o Swagger UI quando servido por HTTP.
- `pom.xml`: recurso `docs/site` copiado para `static/docs`; `Dockerfile` passa a copiar `docs/site`. Novo `DocsConfiguration` redireciona `/docs`, `/docs/` e `/api/docs` para `/docs/index.html` (decisão 0006).
- Checks: `.\mvnw.cmd package` com JDK 25 — sucesso (testes e ArchUnit); `static/docs/index.html` presente no jar; `node --check` no script da página — OK. Não executado: subir a aplicação e abrir `/docs` no navegador (sem banco/Docker disponível), então o redirect não foi exercitado de ponta a ponta.

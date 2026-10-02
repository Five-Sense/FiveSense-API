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

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

## [2026-10-16] — Carga inicial 5S (V3–V6)
- `V3__default_viewer_user.sql`: usuário `visualizador` / `visualizador@gmail.com` / `viewer123` (VIEWER, ACTIVE).
- `V4__seed_5s_materials.sql`: Panos reutilizáveis (10), Pás de sujeira (5), Vassouras (10), Borrifadores de álcool (15), Estopas (50); mínimos 3/2/3/5/15.
- `V5__seed_5s_teams.sql`: Produção A/B, Almoxarifado, Manutenção e Expedição, com representantes fictícios e horários em texto.
- `V6__seed_5s_problems.sql`: 10 problemas (vassoura, borrifador, pá, pano e estopa), nomes ≤ 20 caracteres, `related_email` = `admin@gmail.com`.
- Inserções com `ON CONFLICT DO NOTHING`; V1/V2 não foram alteradas. Docs atualizadas: `data-model.md`, `login.md`, `decision-log.md` (0007), `issues.md`, `implementation-plan.md`.
- Checks: revisão manual contra constraints de V1/V2 (tamanhos, CHECKs, unicidade). Não executado: Flyway/PostgreSQL, `.\mvnw.cmd verify` e testes de integração (Docker indisponível; ambiente com Java 17).
- Risco residual: `ON CONFLICT DO NOTHING` ignora silenciosamente linhas que colidam com dados já existentes (ex.: material de mesmo nome); a V3 falharia no insert silenciosamente se o e-mail/username já existisse.

## [2026-10-16] — CORS para localhost:8081
- Novo shared/config/CorsConfiguration e propriedade pp.cors.allowed-origins (CORS_ALLOWED_ORIGINS); operations.md e .env.example atualizados; decisão 0008.
- Teste CorsConfigurationTests (preflight com X-User-Role, requisição simples, origem não permitida).
- Checks: .\mvnw.cmd test com JDK 25 — BUILD SUCCESS, 54 testes, 0 falhas (inclui ArchUnit). Não executado: integração com Testcontainers (Docker indisponível), métricas de complexidade e teste com o app React Native real.
- Risco residual: celular físico/Expo Go acessa a API pela rede local; se o cliente for navegador em outro IP/porta, é preciso incluí-lo em CORS_ALLOWED_ORIGINS.

## [2026-10-23] — Responsável da ocorrência por e-mail (V7) e SMTP Gmail

- Migration `V7__set_occurrence_responsible_email.sql`: `UPDATE problem SET related_email = 'crisjesusdofigmamobile@gmail.com'` em todas as linhas (responsável que recebe a resposta padrão + imagem da ocorrência) e `UPDATE app_user SET email = 'crisjesusdofigmamobile@gmail.com'` no admin padrão (`username = 'admin'`, antes `admin@gmail.com`), para que a notificação ADMIN/MANAGER caia no mesmo endereço. V1–V6 não foram alteradas (preserva checksum do Flyway).
- Fluxo de imagem verificado por leitura de código: upload em `POST /api/v1/occurrences/images` → `TemporaryImageStore` grava arquivo temporário → `OccurrenceService.create` recupera via `images.locate(imageId)` → `EmailService.sendWithAttachment` anexa o arquivo como MIME `multipart/mixed` → `images.delete` remove após o envio (pós-commit, best effort). Nenhuma imagem é persistida no banco. Compatível com Gmail e demais provedores SMTP.
- `EmailService.sendWithAttachment`: `MimeMessageHelper` passou a usar codificação UTF-8 explícita (`MULTIPART_MODE_MIXED_RELATED` + charset UTF-8), preservando acentos nos assuntos/corpos em português vistos no Gmail.
- `.env.example` e `docs/operations.md`: configuração SMTP pronta para Gmail (`smtp.gmail.com:587`, `MAIL_SMTP_AUTH=true`, `MAIL_STARTTLS=true`, Senha de App de 16 dígitos). A config SMTP é 100% orientada por ambiente; o default local permanece seguro.
- Checks: `.\mvnw.cmd -Dtest="EmailServiceTests,OccurrenceServiceTests,ProblemServiceTests" test` com `JAVA_HOME` apontando para JDK 25 — BUILD SUCCESS, 15 testes, 0 falhas. Não executado: aplicação da V7 em PostgreSQL real e testes de integração Testcontainers (Docker indisponível no ambiente); envio real para a caixa do Gmail (depende de Senha de App configurada). Ambiente sem JDK 21 instalado; o projeto tem `release 21` e o JDK 25 compila.
- Risco residual: `app_user.email` é UNIQUE — o `UPDATE` do admin só é seguro porque nenhum outro usuário usa `crisjesusdofigmamobile@gmail.com` (seeds existentes: `admin@gmail.com`, `visualizador@gmail.com`). Problemas criados via API após a V7 continuam usando o `relatedEmail` informado na requisição; a V7 só reconfigura os registros existentes.

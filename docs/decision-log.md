# Registro de decisões

## 0001 — API de avaliação mobile

- **Data:** 2026-10-02
- **Estado:** vigente
- **Decisão:** manter a aplicação como API REST para a avaliação mobile. O login recebe e-mail e senha, compara os valores diretamente com o cadastro e devolve os dados básicos do usuário. A senha cadastrada é armazenada no campo `app_user.password` sem transformação. As rotas não exigem login; `role` é apenas um dado do usuário.
- **Cadastro:** `POST /api/v1/users` cria usuários ativos.
- **Banco:** PostgreSQL do Supabase, configurado por variáveis de ambiente e gerenciado por Flyway.

## 0002 — Site de referência dos endpoints

- **Data:** 2026-10-02
- **Estado:** vigente
- **Decisão:** manter `docs/site/index.html` como site estático independente para pesquisar, filtrar e consultar os contratos da API. Não faz parte do runtime Spring.

## 0003 — Domínio e persistência

- **Estado:** vigente
- **Decisão:** monólito Spring MVC com PostgreSQL, migrations Flyway, pacotes organizados por capacidade e contratos HTTP documentados em `api-contract.md`.

## 0004 — Imagem temporária na ocorrência (sem persistência)

- **Data:** 2026-10-02
- **Estado:** vigente
- **Decisão:** o humano aprovou receber uma imagem no fluxo de ocorrência exclusivamente para envio por e-mail. A imagem é enviada em `POST /api/v1/occurrences/images` (multipart), guardada apenas em arquivo temporário (`TemporaryImageStore` em `occurrences/infra`) e referenciada por um `imageId` opcional no `POST /api/v1/occurrences`. Ao criar a ocorrência, a imagem é anexada ao e-mail dos destinatários ADMIN/MANAGER e do contato do problema e, em seguida, o arquivo temporário é excluído. O banco de dados nunca armazena a imagem, preservando a decisão de não persistir imagens. Limite de 5MB configurável por `OCCURRENCE_IMAGE_MAX_SIZE`; diretório temporário por `OCCURRENCE_IMAGE_DIR`.
- **Escopo:** restrito à ocorrência. Materiais e demais recursos continuam sem imagem.
## 0005 — Login por username, perfis e admin padrão
- **Data:** 2026-10-09
- **Estado:** vigente (decisão humana)
- **Decisão:** o login passa a usar `username` + senha (texto puro, comparação direta). `app_user` ganha a coluna `username` única (migration V2), e a V2 insere o admin padrão (`admin` / `admin@gmail.com` / `admin123`). Os perfis `ADMIN`, `MANAGER` e `VIEWER` são aplicados por `RoleAccessInterceptor`, que lê o header `X-User-Role` enviado pelo cliente. Só ADMIN e MANAGER criam/editam/excluem usuários, problemas, materiais e equipes. VIEWER consulta, registra ocorrências, muda o status da equipe e ajusta estoque por `PATCH /materials/{id}/stock`.
- **Riscos aceitos:** o header não é autenticado e pode ser forjado; a senha do admin é conhecida; `forgot-password` continua público. Uso exclusivo para estudo.
- **Interpretação a confirmar:** equipes foram tratadas como recurso de gestão (VIEWER só altera o status).
## 0006 — Site de documentação servido em /docs
- **Data:** 2026-10-09
- **Estado:** vigente (pedido do humano; complementa a decisão 0002)
- **Decisão:** `docs/site/index.html` continua sendo a única fonte. O `pom.xml` o copia para `static/docs` no build (sem nova dependência) e `DocsConfiguration` redireciona `/docs`, `/docs/` e `/api/docs` para `/docs/index.html`. O `Dockerfile` copia `docs/site` para o estágio de build. A rota é pública e fica fora do interceptor de perfil (`/api/v1/**`).

## 0007 — Carga inicial de dados 5S por migrations
- **Data:** 2026-10-16
- **Estado:** vigente (pedido do humano; exceção consciente à recomendação de separar seeds de migrations de produção em `data-model.md`)
- **Decisão:** as migrations `V3`–`V6` inserem o usuário `VIEWER` padrão (`visualizador` / `viewer123`), 5 materiais (Panos reutilizáveis 10, Pás de sujeira 5, Vassouras 10, Borrifadores de álcool 15, Estopas 50; mínimos 3, 2, 3, 5 e 15), 5 equipes com representantes fictícios e 10 problemas relacionados aos materiais. Os mínimos de estoque e o `related_email` dos problemas (`admin@gmail.com`) foram escolhidos pela IA por não terem sido informados.
- **Riscos aceitos:** senha em texto puro e dados fictícios em qualquer banco que rode as migrations; não usar em produção real.

## 0008 — CORS para o cliente de desenvolvimento
- **Data:** 2026-10-16
- **Estado:** vigente (pedido do humano)
- **Decisão:** CorsConfiguration (shared/config) libera CORS em /** para as origens de pp.cors.allowed-origins (padrão http://localhost:8081 e http://127.0.0.1:8081, configurável por CORS_ALLOWED_ORIGINS), com métodos GET/POST/PUT/PATCH/DELETE/OPTIONS e qualquer header (inclui X-User-Role). Não usa Spring Security nem nova dependência; origens fora da lista continuam bloqueadas.


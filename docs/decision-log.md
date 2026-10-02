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

# Operação

## Banco Supabase

O padrão configurado para este ambiente é o PostgreSQL do projeto Supabase informado. Preencha `DATABASE_PASSWORD` no `.env` com a **senha do banco** obtida em Supabase Dashboard → Project Settings → Database. Ela não é a senha da conta Supabase. A URL JDBC usa SSL obrigatório e mantém a senha fora da URL.

```properties
DATABASE_URL=jdbc:postgresql://db.gsuivuscizpkbtwedhku.supabase.co:5432/postgres?sslmode=require
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=
```

Ao iniciar a API, Flyway aplica as migrações no banco remoto e Hibernate valida o schema. A conexão direta `db.<project-ref>.supabase.co:5432` depende de IPv6 ou do add-on IPv4 do Supabase; se a rede da máquina for somente IPv4, copie do painel Supabase a URL do **Session pooler** (porta 5432) e o username correspondentes e substitua `DATABASE_URL`/`DATABASE_USERNAME`. Não invente o host do pooler. A senha contém caracteres especiais? Como ela fica em variável separada da URL, não precisa de percent-encoding.

Fonte: [Supabase — Connect to your database](https://supabase.com/docs/guides/database/connecting-to-postgres).

## Ambiente local

Pré-requisitos:

- Java 21.
- Docker com suporte a Compose.
- Maven Wrapper do projeto.

Fluxo local:

1. Copiar `.env.example` para `.env` ou preencher o `.env` já criado. O `.env` é ignorado pelo Git.
2. Preencher a senha do banco Supabase. SMTP é opcional.
3. Executar `docker compose up --build` (Compose carrega `.env`; o PostgreSQL local também sobe, mas a API usa a URL Supabase configurada) ou `.\mvnw.cmd spring-boot:run` com Java 21.
4. No primeiro startup, Flyway prepara as tabelas no Supabase. Criar o primeiro usuário por `POST /api/v1/users` com `name`, `email`, `password` e `role`; em seguida, enviar `email` e `password` a `POST /api/v1/auth/login`.
5. Verificar `/actuator/health` e acessar OpenAPI em `/swagger-ui.html` ou `/v3/api-docs`.

Os artefatos `Dockerfile` e `compose.yaml` existem. `.env.example` documenta placeholders de ambiente.

## Variáveis previstas

| Variável | Uso |
| --- | --- |
| `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` | Conexão PostgreSQL Supabase (SSL obrigatório); senha do banco fica separada da URL. |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP; o usuário SMTP também é usado como remetente quando configurado. |
| `MAIL_SMTP_AUTH`, `MAIL_STARTTLS` | Controles SMTP. |

`.env.example` e `.env` incluem todas as variáveis consumidas pela aplicação. `application.properties` carrega `.env` em execução direta; Docker Compose também usa o arquivo para preencher o ambiente do container. SMTP pode ficar sem configuração se os fluxos de envio de e-mail não forem usados.

## Usuário inicial

Cadastre qualquer usuário por `POST /api/v1/users`. O campo `role` continua armazenado e retornado como dado da conta, mas não autoriza nem restringe endpoints.

## Backup

- PostgreSQL: dump consistente e teste periódico de restauração.
- Imagens estão fora do escopo atual.
- Secrets: gerenciados fora do backup de dados e com recuperação documentada.

## Checks

```powershell
.\mvnw.cmd clean verify
```

Também verificar migrações em banco vazio, OpenAPI, envio de e-mail simulado e health/readiness. Uploads não fazem parte do escopo atual.

## Observabilidade mínima

- Logs de inicialização, erro, login e operações relevantes; a aplicação não deve registrar o campo password.
- Health/readiness por Actuator sem detalhes sensíveis.
- Correlação por request ID é recomendada, sem plataforma de observabilidade complexa no MVP.

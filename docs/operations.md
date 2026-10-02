# Operação

## Ambiente local

Pré-requisitos:

- Java 21.
- Docker com suporte a Compose.
- Maven Wrapper do projeto.

Fluxo local:

1. Subir PostgreSQL 18 por Docker Compose.
2. Configurar variáveis por arquivo local não versionado.
3. Executar `.\mvnw.cmd spring-boot:run`.
4. Verificar `/actuator/health` e acessar OpenAPI em `/swagger-ui.html` ou `/v3/api-docs`.

Os artefatos `Dockerfile` e `compose.yaml` existem. `.env.example` documenta apenas valores locais de exemplo; produção exige secrets próprios.

## Variáveis previstas

| Variável | Uso |
| --- | --- |
| `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` | PostgreSQL. |
| `JWT_PRIVATE_KEY`, `JWT_PUBLIC_KEY` | Segredo RSA para RS256; obrigatório em produção. |
| `BOOTSTRAP_ADMIN_SECRET` | Segredo forte temporário, mínimo de 32 caracteres, para provisionar uma vez o primeiro Admin. |
| `INITIAL_ADMIN_EMAIL`, `INITIAL_ADMIN_NAME` | Identidade do Admin provisionado no primeiro startup; exigidos somente enquanto a conta ainda não existir. |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP; o usuário SMTP também é usado como remetente quando configurado. |
| `MAIL_SMTP_AUTH`, `MAIL_STARTTLS` | Controles SMTP. |

Os nomes efetivamente consumidos pela aplicação estão em `application.properties`, `.env.example` e `compose.yaml`.

## Dados iniciais

- No primeiro startup, a API cria automaticamente o primeiro Admin com senha aleatória e status `FIRST_ACCESS`, usando `INITIAL_ADMIN_EMAIL` e `INITIAL_ADMIN_NAME`.
- A senha em claro não pode ser gravada em log ou persistida sem cifragem; o hash BCrypt é a credencial permanente e um ciphertext AES-GCM temporário é apagado na revelação única. Faça `POST /api/v1/bootstrap/admin` com `X-Bootstrap-Secret` por rede controlada e remova/rotacione o segredo após provisionar.
- Seeds de desenvolvimento não rodam automaticamente em produção.

## Backup

- PostgreSQL: dump consistente e teste periódico de restauração.
- Sessões refresh: manter hashes/sessões no backup consistente do banco e documentar revogação emergencial. Imagens estão fora do escopo atual.
- Secrets: gerenciados fora do backup de dados e com recuperação documentada.

## Checks

```powershell
.\mvnw.cmd clean verify
```

Também verificar migrações em banco vazio, OpenAPI, envio de e-mail simulado e health/readiness. Uploads não fazem parte do escopo atual.

## Observabilidade mínima

- Logs de inicialização, erro, autenticação e operações relevantes sem dados sensíveis.
- Health/readiness por Actuator sem detalhes sensíveis.
- Correlação por request ID é recomendada, sem plataforma de observabilidade complexa no MVP.

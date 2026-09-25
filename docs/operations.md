# Operação

## Ambiente local proposto

Pré-requisitos:

- Java 21.
- Docker com suporte a Compose.
- Maven Wrapper do projeto.

Fluxo alvo da Fase 1:

1. Subir PostgreSQL 18 por Docker Compose.
2. Configurar variáveis por arquivo local não versionado.
3. Executar `.\mvnw.cmd spring-boot:run`.
4. Acessar health check e OpenAPI.

Os artefatos Docker ainda não existem e não serão criados antes da aprovação da documentação.

## Variáveis previstas

| Variável | Uso |
| --- | --- |
| `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` | PostgreSQL. |
| `JWT_PRIVATE_KEY`, `JWT_PUBLIC_KEY` | RS256. |
| `JWT_ACCESS_TTL` | Duração aprovada do acesso. |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | SMTP. |
| `PASSWORD_RESET_BASE_URL` | Link consumido pelo cliente. |
| `FILE_STORAGE_PATH` | Volume privado de imagens, se aprovado. |
| `CORS_ALLOWED_ORIGINS` | Clientes autorizados. |

Nomes finais podem ser ajustados na Fase 1.

## Dados iniciais

- Deve existir estratégia segura para criar o primeiro Admin.
- Não versionar senha padrão.
- Seeds de desenvolvimento não rodam automaticamente em produção.

## Backup

- PostgreSQL: dump consistente e teste periódico de restauração.
- Imagens: backup coordenado com metadados do banco.
- Secrets: gerenciados fora do backup de dados e com recuperação documentada.

## Checks

```powershell
.\mvnw.cmd clean verify
```

Também verificar migrações em banco vazio, OpenAPI, envio de e-mail simulado, limites de upload e health check.

## Observabilidade mínima

- Logs de inicialização, erro, autenticação e operações relevantes sem dados sensíveis.
- Health/readiness para aplicação e banco.
- Correlação por request ID é recomendada, sem plataforma de observabilidade complexa no MVP.

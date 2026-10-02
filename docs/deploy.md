# Deploy

## Ambientes

- **Local:** aplicação + PostgreSQL 18 em Docker.
- **Staging:** recomendado antes de produção; configuração e dados isolados.
- **Produção:** uma instância lógica da empresa, com HTTPS, secrets e backup.

Hospedagem e infraestrutura ainda não foram escolhidas.

## Processo alvo

1. Executar `.\mvnw.cmd clean verify`.
2. Construir imagem imutável da API.
3. Fazer backup quando houver migração com risco.
4. Aplicar Flyway no startup/deploy controlado.
5. Subir a nova versão com secrets do ambiente.
6. Verificar `/actuator/health`, login e fluxo crítico.
7. Monitorar erros e e-mail.

## Rollback

- Aplicação: retornar à imagem anterior.
- Banco: preferir migrações compatíveis para frente; rollback destrutivo exige plano específico e backup validado.
- Imagens: não aplicável no escopo atual da API.

## Verificação pós-deploy

- Health/readiness do Actuator respondem sem expor detalhes sensíveis.
- Flyway está na versão esperada.
- Conexão PostgreSQL e SMTP funcionam.
- Login simples retorna a identidade cadastrada; rotas ficam abertas.
- Fluxo de ocorrência notifica todos os usuários ADMIN e MANAGER por e-mail, sem anexo de imagem.
- Não há segredo ou stack trace em logs/respostas.

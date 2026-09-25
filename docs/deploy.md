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
6. Verificar health, login, permissões e fluxo crítico.
7. Monitorar erros e e-mail.

## Rollback

- Aplicação: retornar à imagem anterior.
- Banco: preferir migrações compatíveis para frente; rollback destrutivo exige plano específico e backup validado.
- Imagens: preservar volume/objeto e metadata sincronizados.

## Verificação pós-deploy

- Health/readiness respondem.
- Flyway está na versão esperada.
- Conexão PostgreSQL e SMTP funcionam.
- JWT RS256 valida corretamente.
- Papéis negam e permitem endpoints conforme matriz.
- Upload e consulta de imagem funcionam.
- Não há segredo ou stack trace em logs/respostas.

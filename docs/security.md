# Segurança

## Perfis

- `ADMIN`: gestão de usuários.
- `MANAGER`: gestão de problemas, equipes, representantes, materiais e estoque.
- `VIEWER`: consultas, ocorrência e alteração do status da própria equipe.

Permissões não definidas em `requirements.md` permanecem negadas por padrão.

## Autenticação

- E-mail e senha.
- Senhas armazenadas somente como hash forte; algoritmo final precisa ser aprovado.
- JWT assinado com RS256.
- Chave privada apenas em secret; chave pública pode ser distribuída conforme necessidade.
- Claims mínimos: subject imutável, papel, emissão, expiração e identificador do token.
- Token de reset aleatório/UUIDv4, armazenado por hash, uso único e expiração de 10 minutos.

## Decisões abertas de alto risco

- Rejeitar ou aceitar token infinito do Visualizador. A recomendação é token curto para todos com renovação revogável.
- Definir logout: somente cliente ou revogação.
- Definir envio de senha inicial versus link de ativação. Link de uso único é recomendado.
- Escolher algoritmo de hash e biblioteca JWT.

## Autorização

- Deny by default.
- Validar papel no backend e propriedade da equipe ao alterar status.
- Não confiar em ID/papel recebido no corpo da requisição.
- Operações administrativas e destrutivas exigem autorização explícita e teste negativo.

## Validação

- Bean Validation nos DTOs e invariantes no service/domínio.
- E-mails normalizados e validados.
- Paginação limitada para evitar abuso.
- Upload de ocorrência limitado a 20 MB; formatos e tipos permitidos ainda serão definidos.
- Validar conteúdo real, gerar nome/chave no servidor e impedir path traversal.

## Secrets

- Banco, SMTP, chaves JWT e storage por variáveis/secret manager.
- `.env.example` poderá documentar nomes, nunca valores reais.
- Não versionar chaves privadas ou credenciais.

## Dados e logs

- Senha, hash, token, chave, conteúdo de imagem e credenciais SMTP nunca aparecem em resposta ou log.
- Dados pessoais devem ser minimizados nas respostas.
- Mensagem de recuperação não confirma se o e-mail existe.
- Logs de autenticação registram evento e resultado sem credencial.

## Controles operacionais

- CORS deve listar origens aprovadas.
- HTTPS é obrigatório fora do ambiente local.
- Erros não expõem stack trace.
- Dependências e imagens Docker devem ser verificadas no pipeline futuro.

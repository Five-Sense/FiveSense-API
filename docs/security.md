# Segurança

## Perfis

- `ADMIN`: administra usuários e cria contas `MANAGER` e `VIEWER`.
- `MANAGER`: cria e gerencia contas `VIEWER`, além de problemas, equipes (incluindo o texto dos representantes), materiais e estoque.
- `VIEWER`: usuário de baixa permissão usado no totem; consulta equipes, status 5S, representantes e estoque; registra ocorrência/alerta e altera somente status 5S enviando `teamId`. Não consulta dados de usuários, dashboards nem edita equipes/materiais/estoque. Não possui vínculo persistido com equipe.

Permissões não definidas em `requirements.md` permanecem negadas por padrão.

## Autenticação

- E-mail e senha.
- Senhas armazenadas somente como hash unidirecional pelo `PasswordEncoder` do Spring Security. RS256 é exclusivo para assinatura/verificação JWT.
- JWT assinado com RS256 (algoritmo confirmado pelo humano e requerido por NF005).
- Chave privada apenas em secret; chave pública pode ser distribuída conforme necessidade.
- Claims mínimos: subject imutável, papel, emissão, expiração e identificador do token.
- `ADMIN`/`MANAGER`: access JWT com duração de 1 hora.
- `VIEWER`: access JWT de 15 minutos + refresh token opaco/aleatório, rotativo, armazenado no servidor somente como hash e revogável. Timeout de inatividade de 8 horas, duração absoluta de 30 dias e máximo de cinco sessões simultâneas. Reuso do token anterior revoga a sessão.
- Token de reset aleatório/UUIDv4, armazenado por hash, uso único e expiração de 10 minutos.

## Controles de risco definidos

- Logout/revogação: logout revoga a sessão identificada; mudança/reset de senha revoga todas as sessões do usuário.
- Primeiro acesso: troca da senha temporária é obrigatória; enquanto a conta estiver `FIRST_ACCESS`, Admin/Gestor autorizado pode reenviar nova senha temporária, invalidando a anterior. Reenvio tem intervalo de 60 segundos e máximo de três e-mails por hora.
- Enquanto estiver em `FIRST_ACCESS`, a autenticação não deve conceder acesso aos recursos de negócio: só permitir concluir a troca obrigatória de senha e encerrar/reiniciar o fluxo de autenticação.
- Hash de senha usa BCrypt com custo 12 via `PasswordEncoder` Spring Security. RS256 assina JWTs e não é usado para proteger senhas.
- O artifact `spring-boot-starter-oauth2-resource-server` integra o decoder/encoder JWT do Spring Security.

## Autorização

- Deny by default.
- Validar papel no backend e aplicar a matriz de `requirements.md`. VIEWER altera somente status 5S por `teamId`, sem associação de propriedade; leitura limitada a equipe/status/representantes/estoque. Dados de outros usuários só são acessíveis a Admin/Gestor; a API não fornece dashboards.
- Não confiar em ID/papel recebido no corpo da requisição.
- Operações administrativas e destrutivas exigem autorização explícita e teste negativo.

## Validação

- Bean Validation nos DTOs e invariantes no service/domínio.
- E-mails normalizados e validados.
- Paginação limitada para evitar abuso.
- Uploads de imagem não são suportados no escopo atual da API, apesar de requisitos do PDF que os mencionam.

## Secrets

- Banco, SMTP e chaves JWT por variáveis/secret manager.
- `.env.example` poderá documentar nomes, nunca valores reais.
- Não versionar chaves privadas ou credenciais.

## Dados e logs

- Senha, hash, token, chave e credenciais SMTP nunca aparecem em logs. Senha temporária de usuário é enviada ao e-mail da conta criada; persistir somente hash. A senha inicial aleatória do primeiro Admin fica cifrada por AES-GCM até a revelação única autorizada; a resposta é acessível somente por segredo de bootstrap e rede controlada, e a cifra é apagada após revelação.
- Dados pessoais devem ser minimizados nas respostas. Consultas de outros usuários são restritas a Admin/Gestor.
- Mensagem de recuperação não confirma se o e-mail existe.
- Logs de autenticação registram evento e resultado sem credencial.

## Controles operacionais

- CORS deve listar origens aprovadas.
- HTTPS é obrigatório fora do ambiente local.
- Erros não expõem stack trace.
- Dependências e imagens Docker devem ser verificadas no pipeline futuro.

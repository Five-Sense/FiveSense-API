# Decision Log

Estados: `Proposta`, `Aceita`, `Substituída` ou `Rejeitada`.

## 0001 - Usar SpecFirst como contrato do projeto

- **Data:** 2026-09-18
- **Estado:** Aceita

### Contexto

O projeto será implementado por humanos e agentes e precisa preservar escopo, regras e evidências.

### Decisão

`AGENTS.md` é o contrato universal; `docs/*` é a fonte canônica detalhada. Issue, plano e deployment log devem ser sincronizados.

### Consequências

- Implementação começa somente após aprovação documental.
- Decisões e riscos deixam rastro versionável.

## 0002 - Adotar monólito Spring MVC simples

- **Data:** 2026-09-18
- **Estado:** Aceita

### Contexto

O MVP atende uma única empresa e não exige distribuição ou arquitetura sofisticada.

### Decisão

Usar Java 21, Spring Boot 4.1.1, Spring MVC, PostgreSQL 18, Flyway, Docker e Maven em um único deploy. Não adotar multi-tenancy, microservices ou modular monolith formal.

### Consequências

- Menor custo operacional e cognitivo.
- Fronteiras internas continuam testadas para impedir acoplamento acidental.

## 0003 - Exigir testes por service, fluxo e arquitetura

- **Data:** 2026-09-18
- **Estado:** Aceita

### Contexto

O humano exige teste unitário para cada método de service, teste de integração banco-controller para cada fluxo e fitness functions após implementações.

### Decisão

JUnit/Mockito cobrem services; integração usa PostgreSQL 18 real; ArchUnit verifica ciclos/dependências e as métricas são revisadas manualmente conforme `testing.md`.

### Consequências

- `verify` será o gate de conclusão.
- Testcontainers PostgreSQL e ArchUnit estão aprovados e configurados; métricas são verificadas por revisão/relatório do build até uma ferramenta dedicada ser aprovada.

## 0004 - Organizar por capacidade com subcamadas

- **Data:** 2026-09-18
- **Estado:** Aceita

### Contexto

Uma estrutura apenas por camada global tende a misturar capacidades, enquanto um modular monolith seria excessivo.

### Decisão

Usar pacotes `auth`, `users`, `teams`, `problems`, `materials` e `occurrences`, cada um com `controller`, `app`, `infra`, `dto` e `domain` somente quando necessário.

### Consequências

- Navegação simples e coesão por capacidade.
- Sem módulos Maven, isolamento de runtime ou eventos internos obrigatórios.
- Foi adotada para o scaffold e implementação.

## 0005 - Estratégia inicial de segurança e imagens (substituída)

- **Data:** 2026-09-18
- **Estado:** Substituída

### Decisão proposta

- Tokens de acesso finitos para todos os papéis, com renovação revogável em vez de token infinito.
- Link de ativação de uso único em vez de senha em texto por e-mail.
- Imagens em volume privado, metadata no PostgreSQL e acesso autenticado.
- Exclusão bloqueada/inativação quando houver histórico.

### Consequências

- Histórico: a política inicial sem expiração para Viewer foi substituída pela recomendação da decisão 0013: sessão renovável e revogável; fallback de 8h, sem JWT permanente.
- A senha por link foi substituída pela decisão 0007, e a proposta de armazenamento de imagens foi substituída pela decisão 0008.
- Política final aprovada na decisão 0022: exclusão de problema/material referenciado retorna conflito; não há exclusão de ocorrência.

## 0006 - Algoritmo de assinatura JWT RS256

- **Data:** 2026-09-25
- **Estado:** Aceita

### Contexto

NF005 exige RS256 e o humano confirmou que a stack autorizada pode utilizá-lo.

### Decisão

Usar RS256 para assinatura/verificação dos JWTs e suporte JWT do Spring Security. Os detalhes de validade, renovação, revogação e armazenamento são definidos separadamente pelas decisões 0013 e pelas pendências de `requirements.md`.

### Consequências

- A chave privada deve ficar em secret; a chave pública pode ser disponibilizada conforme necessidade.
- A validade por papel e os limites de sessão Viewer foram aprovados na decisão 0013 e estão implementados.
- O suporte está implementado pelo `spring-boot-starter-oauth2-resource-server`.

## 0007 - Senha inicial enviada ao usuário cadastrado

- **Data:** 2026-09-25
- **Estado:** Aceita

### Contexto

RF005/RN003 pedem senha aleatória inicial por e-mail. O cadastro é feito por Admin/Gestor autorizado, não pelo próprio titular.

### Decisão

Gerar senha inicial temporária e enviá-la ao endereço de e-mail da conta recém-criada. Persistir somente o hash; a senha em claro existe apenas transitoriamente para compor o envio. Link de ativação não substitui esse requisito.

### Consequências

- A troca da senha temporária no primeiro login é obrigatória; conta não validada permanece em `FIRST_ACCESS`.
- Admin/Gestor autorizado pode reenviar senha temporária para conta existente em `FIRST_ACCESS`; cada reenvio invalida a senha anterior.
- E-mail de credenciais é enviado ao usuário criado; notificações de ocorrência são enviadas a todos ADMIN e MANAGER.

## 0008 - Ocorrências sem imagens e sem baixa de estoque

- **Data:** 2026-09-25
- **Estado:** Aceita

### Contexto

RF016/RN013/RN014/RN017 descrevem anexos e imagens. O PDF também não esclarece o efeito da quantidade afetada sobre o estoque.

### Decisão

Imagens não fazem parte do escopo atual da Five Sense API: não haverá upload, armazenamento, consulta nem anexo de imagem. A quantidade afetada pela ocorrência não altera o estoque; a ocorrência apenas registra/informa.

### Consequências

- Requisitos de imagem do PDF ficam explicitamente fora do escopo atual, sem apagar sua rastreabilidade.
- E-mail de ocorrência a todos ADMIN e MANAGER não contém anexo. Regras de alerta de estoque continuam separadas e pendentes.

## 0009 - Representantes como texto da equipe

- **Data:** 2026-09-25
- **Estado:** Aceita

### Decisão

Representantes não são contas de usuário nem uma entidade relacionada. Gestores mantêm um campo `varchar(255)` na equipe com os nomes que o totem deve representar. O formato interno (texto livre ou lista delimitada) ainda precisa de especificação.

## 0010 - Contas e permissões de criação de usuários

- **Data:** 2026-09-25
- **Estado:** Aceita

### Decisão

Admin administra usuários e pode criar contas MANAGER e VIEWER. Manager pode criar e gerenciar contas VIEWER. VIEWER é uma conta de baixa permissão utilizada no totem.

## 0011 - Primeiro acesso e senha do Admin inicial

- **Data:** 2026-09-25
- **Estado:** Aceita

### Decisão

Toda conta criada recebe senha inicial aleatória no e-mail da conta e precisa trocá-la no primeiro login. Conta permanece em `FIRST_ACCESS` até a troca. Admin/Gestor autorizado pode reenviar uma senha nova para conta ainda nesse estado, invalidando a anterior. O primeiro ADMIN é exceção de canal: pré-cadastrado com senha aleatória, revelada uma única vez ao operador autorizado por bootstrap seguro. Persistir somente hash e não gravar senha em logs.

### Implementação

No primeiro startup, a API provisiona o ADMIN de `INITIAL_ADMIN_EMAIL`/`INITIAL_ADMIN_NAME` com credencial aleatória. O hash BCrypt é permanente; AES-GCM protege temporariamente a senha até o endpoint de bootstrap revelá-la uma única vez. Reenvio de credencial tem intervalo de 60 segundos e até três envios por hora. SMTP é best effort, sem retry durável.

## 0012 - Destinatários de notificações de ocorrência

- **Data:** 2026-09-25
- **Estado:** Aceita

### Decisão

Enviar notificações de ocorrência para todos os usuários com papel ADMIN ou MANAGER. VIEWER é autor da ocorrência, não destinatário da notificação.

## 0013 - Tokens por papel e usuário de totem

- **Data:** 2026-09-25
- **Estado:** Aceita pelo pedido de implementação de 2026-09-25

### Decisão

ADMIN/MANAGER usam access JWT com duração de uma hora, conforme RN022/RN023. VIEWER usa access JWT de 15 minutos e refresh token opaco rotativo, armazenado apenas por hash no servidor, com timeout de inatividade de 8 horas e duração absoluta de 30 dias. Até cinco sessões simultâneas por Viewer. Logout, mudança/reset de senha ou reutilização de refresh token revoga a sessão. O cliente do totem armazena refresh token em armazenamento seguro e usa HTTPS. JWT Viewer de 8 horas é fallback apenas se a renovação não puder ser implementada.

### Implementação

RS256 continua sendo apenas o algoritmo de assinatura JWT. A renovação adota rotação estrita com revogação ao detectar reutilização e o sistema limita cada Viewer a cinco sessões simultâneas, revogando a mais antiga ao criar outra.

## 0014 - Hash de senha

- **Data:** 2026-09-25
- **Estado:** Aceita

### Decisão

Senhas são armazenadas como hash unidirecional por mecanismo `PasswordEncoder` do Spring Security. RS256 é usado somente para assinatura/verificação JWT; não é criptografia nem hash de senha.

### Implementação

BCrypt com custo 12 foi adotado via `PasswordEncoder` do Spring Security.

## 0015 - Alteração do status 5S por VIEWER

- **Data:** 2026-09-25
- **Estado:** Aceita

### Decisão

VIEWER é o papel que altera o status 5S. Não existe associação persistida entre VIEWER e equipe; o request informa o `teamId` alvo e a permissão é limitada à alteração do status, sem checagem de propriedade da equipe.

## 0016 - Permissões de consulta por perfil

- **Data:** 2026-09-25
- **Estado:** Aceita

### Decisão

Admin e Gestor podem consultar dados operacionais; detalhes de outros usuários são restritos a esses dois perfis. A API não oferece dashboards/analytics. Viewer consulta equipes, status 5S, representantes e estoque; pode alterar somente o status 5S e gerar alertas por meio do fluxo de ocorrência RF016. O endpoint de seleção da ocorrência devolve somente identificadores e nomes ativos de problema/material necessários à operação.

## 0017 - Horários como organização por equipe

- **Data:** 2026-09-25
- **Estado:** Aceita

### Decisão

O recurso chamado informalmente de calendário serve apenas para exibir/organizar o horário de cada equipe; não modela eventos ou compromissos. O horário é propriedade `varchar(255)` de Team.

## 0018 - Troca inicial obrigatória e reenvio

- **Data:** 2026-09-25
- **Estado:** Aceita

### Decisão

A troca da senha inicial no primeiro login é obrigatória. “Não validada” significa que a conta permanece em `FIRST_ACCESS`, ainda sem concluir a troca; não há confirmação por link. A conta permanece até troca ou desativação. Reenvio por Admin/Gestor autorizado tem intervalo mínimo de 60 segundos e limite de 3 por hora; nova senha invalida a anterior.

## 0019 - Limite de representantes

- **Data:** 2026-09-25
- **Estado:** Aceita

### Decisão

O campo textual `Team.representatives` tem limite de 255 caracteres (`varchar(255)`). Limites explícitos do PDF para outros campos continuam prevalecendo sobre o padrão 255.

## 0020 - Tamanho padrão de campos textuais

- **Data:** 2026-09-25
- **Estado:** Aceita

### Decisão

Usar `varchar(255)` quando o PDF não define tamanho máximo para um campo textual. Limites específicos da fonte prevalecem.

## 0021 - Semântica dos alertas e e-mails

- **Data:** 2026-09-25
- **Estado:** Aceita por autorização de implementação

### Decisão

“Gerar alerta” pelo Viewer significa criar ocorrência RF016. Cada ocorrência notifica todos os Admins e Gestores e envia a resposta padrão do problema ao e-mail relacionado. A quantidade afetada não movimenta estoque. Alerta de estoque é derivado quando `stockQuantity <= minimumStock`; e-mail é enviado quando material é criado já abaixo do limite ou cruza para baixo, sem duplicar em atualizações que permanecem abaixo.

## 0022 - Exclusão e estado inicial de equipe

- **Data:** 2026-09-25
- **Estado:** Aceita por autorização de implementação

### Decisão

Exclusão física só é permitida se não houver referências de ocorrência; caso contrário responder conflito (`409`). Equipes iniciam com status `NOT_DOING_5S`. Equipes sem relacionamento histórico podem ser excluídas fisicamente.

## 0023 - Contrato HTTP e paginação

- **Data:** 2026-09-25
- **Estado:** Aceita por autorização de implementação

### Decisão

Rotas sob `/api/v1`, erros como RFC 9457 `ProblemDetail`, paginação padrão 20 e máxima 100, ordenação permitida por campos explicitamente suportados. Todos os RFs entram no MVP salvo imagens e dashboards.

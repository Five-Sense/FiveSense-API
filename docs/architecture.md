# Arquitetura

## Visão geral

A Five Sense API será um monólito Spring Boot com Spring MVC e PostgreSQL. A arquitetura deve permanecer direta: controllers HTTP chamam services de aplicação; services aplicam regras e coordenam persistência/integrações; MapStruct converte DTOs e entidades; infraestrutura concentra JPA, e-mail e arquivos.

Módulos por capacidade são apenas pastas de organização. Não haverá isolamento de runtime, comunicação remota, event bus ou infraestrutura de modular monolith.

## Estrutura proposta

```text
com.fivesense.api
├── auth
├── users
├── teams
├── problems
├── materials
├── occurrences
└── shared
    ├── error
    ├── config
    └── pagination
```

Dentro de cada capacidade:

- `controller`: endpoints, status HTTP e delegação.
- `dto`: requests e responses versionáveis.
- `app`: services, transações e mappers MapStruct.
- `domain`: entidades, value objects, enums e invariantes.
- `infra`: repositories JPA e integrações externas.

## Dependências permitidas

```text
controller -> dto + app
app        -> dto + domain + infra
infra      -> domain + configuração compartilhada
dto        -> tipos simples e enums estáveis
domain     -> Java/JPA/Validation estritamente necessários
```

Para manter o MVP simples, `app` pode depender das interfaces Spring Data declaradas em `infra`. Se essa direção gerar acoplamento real, uma interface de repositório pode ser extraída, mas ports/adapters não são o padrão inicial.

Dependência entre capacidades deve ocorrer pelo service público da capacidade dona ou por identificadores, evitando acesso direto ao repository de outro módulo.

## Dependências proibidas

- `domain` dependendo de `controller`, `dto`, `app` ou `infra`.
- `infra` chamando `controller`.
- controller acessando repository diretamente.
- um módulo usando entidade JPA mutável de outro módulo para alterar seu estado.
- ciclos entre pacotes ou capacidades.
- mapeadores manuais quando MapStruct atende o caso.
- regras de negócio em controller, mapper, repository ou entidade de request.
- abstrações, eventos ou camadas criadas sem consumidor real.

## Persistência e transações

- PostgreSQL 18 é o banco oficial.
- Flyway é a única fonte de evolução do schema.
- Services de aplicação definem fronteiras transacionais.
- Listagens potencialmente extensas são paginadas.
- Datas são persistidas em UTC; regras de agenda usam timezone de negócio aprovado.
- Exclusões com referências históricas dependem da política ainda pendente.

## Integrações

- **E-mail:** Spring Mail; templates Thymeleaf somente se houver benefício real.
- **Imagens:** contrato pendente; metadata no banco e conteúdo fora do banco é a proposta.
- **JWT:** RS256 com chaves em secrets; biblioteca/abordagem ainda será aprovada.
- **OpenAPI:** springdoc documenta endpoints, autenticação e erros.

## Fitness functions

Testes automatizados devem verificar:

- ausência de ciclos entre pacotes;
- controllers sem acesso direto a repositories;
- domínio isolado das camadas externas;
- nomenclatura e localização das classes por camada;
- limites de complexidade ciclomática, acoplamento e coesão de `docs/testing.md`.

## Decisões base

- Monólito MVC simples: aceita pelo humano.
- Organização por capacidades com subpastas por camada: proposta para aprovação.
- Sem multi-tenancy: aceita para o MVP.
- Ferramentas de fitness functions e integração: pendentes de aprovação porque adicionam dependências.

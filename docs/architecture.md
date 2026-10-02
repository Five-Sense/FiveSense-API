# Arquitetura

## Visão geral

A Five Sense API será um monólito Spring Boot com Spring MVC e PostgreSQL. A arquitetura deve permanecer direta: controllers HTTP chamam services de aplicação; services aplicam regras e coordenam persistência/integrações; MapStruct converte DTOs e entidades; infraestrutura concentra JPA e e-mail. Upload e armazenamento de arquivos estão fora do escopo atual.

Exceção aprovada pelo pedido humano de 2026-10-02: `docs/site/index.html` é um site estático somente para navegação da documentação da API. Não faz parte da aplicação Spring, não consome endpoints em runtime, não adiciona dependências e não amplia o produto para uma UI operacional. Também em 2026-10-02 o humano substituiu o desenho de autenticação/autorização SpecFirst por login simples para avaliação mobile.

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
- Problemas e materiais referenciados por ocorrências são protegidos por FK e exclusão responde 409; equipes sem referências podem ser excluídas.

## Integrações

- **E-mail:** Spring Mail; templates Thymeleaf somente se houver benefício real.
- **Imagens:** fora do escopo atual da API, apesar de RF/RN do PDF.
- **Login:** e-mail/senha comparados diretamente. Todas as rotas são abertas; `role` é informativo.
- **OpenAPI:** springdoc documenta endpoints públicos e erros.

## Fitness functions

Testes automatizados devem verificar:

- ausência de ciclos entre pacotes;
- controllers sem acesso direto a repositories;
- domínio isolado das camadas externas;
- nomenclatura e localização das classes por camada;
- limites de complexidade ciclomática, acoplamento e coesão de `docs/testing.md`.

## Decisões base

- Monólito MVC simples: aceita pelo humano.
- Organização por capacidades com subpastas por camada: adotada.
- Sem multi-tenancy: adotada para o MVP.
- Testcontainers PostgreSQL e ArchUnit: adotados; a execução de integração exige Docker.

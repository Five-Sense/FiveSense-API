# Recipes técnicas

Recipes são padrões repetíveis, não abstrações obrigatórias.

## Fluxo REST

**Entrada:** request DTO validado.

**Sequência:** controller -> service transacional -> repository/integração -> mapper MapStruct -> response DTO.

**Regras:**

- controller sem regra de negócio;
- entidade JPA nunca é resposta HTTP;
- erros usam contrato global;
- autorização e casos negativos têm testes.

## Mapper MapStruct

- Interface `*Mapper` em `<modulo>.app`.
- `componentModel = "spring"`.
- Campos ignorados precisam ser explícitos.
- Atualizações parciais não sobrescrevem campo ausente sem contrato.
- Não consultar banco nem aplicar regra dentro do mapper.

## Migração Flyway

- Nome `V<versao>__<descricao>.sql`.
- Migration já aplicada é imutável.
- Incluir constraints, índices e defaults deliberados.
- Validar em PostgreSQL 18 vazio e em upgrade.

## Service unit test

- Instanciar service com mocks de repository/integração.
- Cobrir cada método público: sucesso, entrada inválida, não encontrado, permissão/regra e falha externa relevante.
- Verificar efeito e ausência de efeitos indevidos, sem acoplar ao detalhe interno.

## Flow integration test

- PostgreSQL 18 em container + Flyway.
- Request HTTP real via camada MVC.
- Assert de status, contrato e estado persistido.
- SMTP substituído por double verificável. Não há armazenamento de arquivos no escopo atual.
- Dados únicos e isolamento por teste.

## Fora do escopo

- Base controllers/services genéricos.
- Repository genérico além do fornecido pelo Spring Data.
- Event bus, mediator ou abstrações sem demanda concreta.

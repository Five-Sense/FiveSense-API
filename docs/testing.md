# Estratégia de testes

## Objetivo

Provar comportamento, integração real com PostgreSQL 18 e preservação da arquitetura simples.

## Pirâmide

### Testes unitários

- JUnit 5 e Mockito.
- Todo método público de cada service deve ter casos de sucesso, validação, autorização/regra e falha relevante.
- Não subir Spring quando um teste puro resolve.
- Mappers MapStruct com conversões não triviais devem ser testados.

### Testes de integração

- Cada fluxo entregue deve ter ao menos um teste do banco ao controller.
- Usar `@SpringBootTest`/MockMvc ou suporte MVC equivalente e PostgreSQL real em container.
- Não substituir PostgreSQL por H2.
- Executar migrações Flyway no banco do teste.
- Simular apenas fronteiras externas, como SMTP e armazenamento, mantendo seus contratos verificáveis.
- Nome recomendado: `*IT`; dados independentes e limpeza previsível.

### Testes de arquitetura e fitness functions

- A ferramenta proposta para regras estruturais é ArchUnit.
- A ferramenta de métricas de código ainda deve ser aprovada na Fase 1.
- Executar no `verify` após toda implementação.

Limites iniciais propostos:

| Métrica | Cálculo | Alvo | Falha |
| --- | --- | --- | --- |
| Complexidade ciclomática | 1 + pontos de decisão por método | <= 10 | > 15 sem exceção aprovada |
| Acoplamento (CBO) | classes externas diretamente acopladas por classe | <= 12 | > 20 ou qualquer ciclo |
| Coesão (LCOM4) | componentes desconectados de métodos/campos por classe | 1 | > 2 em service/domínio |

Valores entre alvo e falha exigem revisão. Exceções devem citar a issue e explicar por que dividir o código pioraria o domínio.

Fitness functions obrigatórias:

- nenhum ciclo entre pacotes;
- `domain` não depende de `controller`, `dto`, `app` ou `infra`;
- controller não acessa repository;
- nomes `*Controller`, `*Service`, `*Repository` e `*Mapper` ficam nas pastas corretas;
- repositories são acessados pela camada de aplicação;
- métricas geram relatório arquivado pelo build/CI.

## Comandos

No Windows:

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
```

O comando oficial final será `.\mvnw.cmd clean verify` quando Testcontainers, ArchUnit e a ferramenta de métricas forem aprovados/configurados.

## Definition of Done de qualidade

- Todos os testes passam.
- Cada método público de service alterado tem cobertura comportamental.
- Cada fluxo alterado tem integração banco-controller.
- PostgreSQL 18 e Flyway participam dos testes de integração.
- Fitness functions passam e o relatório de métricas foi revisado.
- Falha ou check não executado está registrado na issue e no deployment log.

# Padrões de código

## Java

- Usar Java 21 e nomes em inglês no código.
- Classes e APIs públicas devem ter responsabilidade única e nomes explícitos.
- Preferir imutabilidade em DTOs com `record` quando compatível com o framework.
- Não usar `Optional` em campos de entidade ou parâmetros.
- Não retornar `null` silenciosamente; modelar ausência e erro de forma explícita.
- Comentários explicam motivo ou tradeoff, não repetem o código.

## Spring MVC

- Controllers traduzem HTTP, validam request e delegam ao service.
- Services concentram casos de uso e fronteiras transacionais.
- Repositories concentram consultas; não contêm regra de negócio.
- Entidades protegem invariantes locais, sem conhecer HTTP ou integrações.
- Exceções são convertidas por um handler global para um contrato de erro consistente.
- Paginação usa contratos consistentes e limites aprovados.

## DTOs e MapStruct

- Nunca expor entidades JPA diretamente.
- Separar DTO de entrada e saída quando os contratos diferirem.
- Usar Bean Validation em requests.
- Usar MapStruct para mapeamentos; configurações compartilhadas devem falhar em campos não mapeados relevantes.
- Mapeamento não deve executar regra de negócio ou acesso a banco.

## Persistência

- Toda mudança de schema exige migração Flyway versionada.
- Nomes SQL usam `snake_case`; Java usa `camelCase`.
- Constraints de unicidade, nulidade e intervalo devem existir no banco quando aplicáveis.
- Evitar `EAGER` por padrão e consultas N+1.
- Não usar `ddl-auto=create` ou `update` fora de teste descartável.

## Segurança e observabilidade

- Autorização é validada no backend, próxima ao endpoint/service.
- Senhas somente como hash forte; tokens e chaves nunca em logs.
- Logs estruturados usam IDs, ação e resultado, sem dados sensíveis.
- Uploads, caso sejam aprovados em uma futura alteração de escopo, validam tamanho, tipo real e nome gerado pelo servidor. A API atual não recebe arquivos nem imagens.

## Organização

- Cada capacidade usa `controller`, `dto`, `app`, `domain` e `infra` somente quando precisar.
- Não criar pacote vazio ou classe genérica `Utils`.
- Código compartilhado só entra em `shared` após existir uso real em mais de uma capacidade.

## Checklist

- Testes unitários de todos os métodos públicos de service alterados.
- Teste de integração do fluxo completo.
- MapStruct usado nos mapeamentos.
- Migração, validação, autorização e OpenAPI atualizados.
- Sem ciclos; métricas dentro dos limites.
- Issue, plano e deployment log sincronizados.

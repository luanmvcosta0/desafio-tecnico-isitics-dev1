# Arquitetura e padrões de código — LigaHub (backend)

Objetivo: atender ao roteiro da API (B1) e às rubricas de qualidade (B2) e banco (B3) do barema. Cada decisão
abaixo existe por um motivo ligado a essas rubricas; ver [barema-backend.md](barema-backend.md).

## 1. Estrutura de pacotes (package-by-feature)

```
com.dev1_isitics.ligahub_be
├── LigahubBeApplication.java
├── shared/                         # código transversal, sem regra de negócio de módulo
│   ├── config/                     # JacksonConfig, CorsConfig, ClockConfig, OpenApiConfig
│   ├── exception/                  # ApiException e subclasses, GlobalExceptionHandler, ErrorResponse
│   ├── logging/                    # RequestLoggingFilter
│   ├── money/                      # Money (arredondamento HALF_UP, 2 casas)
│   ├── text/                       # TextNormalizer (V4), AlphabeticalComparator (ordem das Premissas)
│   ├── time/                       # BrasiliaDateTime (parse estrito RN01, formatação das respostas)
│   └── web/                        # parsers de query params e de ids da URL, LocationHeader
└── modules/
    ├── players/
    │   ├── api/                    # PlayerController, DTOs de request/response (records)
    │   ├── application/            # PlayerService (casos de uso), PlayerQueryService (listagem)
    │   ├── domain/                 # Player (entidade), Position, Condition, PlayerAttributes,
    │   │                           # MarketValueCalculator, OverallCalculator, BaseValueCalculator
    │   └── infrastructure/         # PlayerRepository (Spring Data JPA)
    ├── clubs/                      # Club, ClubService, ClubController, SquadValuation...
    ├── market/                     # Contract, Transaction, PurchaseService, SaleService,
    │                               # PurchaseFeeCalculator, LoyaltyPenaltyCalculator, SaleCommissionCalculator,
    │                               # TransactionCsvExporter
    └── league/                     # LineupEntry, Formation, LineupService, Match, MatchDuel,
                                    # MatchEngine, MatchService, StandingsService
```

Decisões já implementadas:
- `Player` referencia o clube por id (`clubId`), sem `@ManyToOne`: cada agregado (jogadores, clubes) fica
  independente, e os serviços buscam o que precisam pelos repositórios.
- Os seis atributos ficam no `@Embeddable TechnicalAttributes`; valor de mercado e overall não são campos da
  entidade (são calculados).
- Instantes como `Instant` em Java e `TIMESTAMPTZ` no banco; conversão para `-03:00` só na borda (DTOs).
  `criado_em`/`atualizado_em` de auditoria por `@CreationTimestamp`/`@UpdateTimestamp`; instantes de regra
  de negócio (compra, venda, partida, exclusão) sempre do `Clock` injetado.
- Contrato JSON em `shared/config/JacksonConfig` + `shared/json/StrictIntegerDeserializer`; ids da URL por
  `shared/web/PathIds.ID` (usar em toda rota com id: `@GetMapping("/players/" + PathIds.ID)`); erros em
  `shared/exception/GlobalExceptionHandler`.

## 2. Camadas e responsabilidades

| Camada | Pode | Não pode |
| --- | --- | --- |
| **Controller** (`api`) | Receber DTO validado, chamar **um** método de serviço, montar `ResponseEntity` (status, `Location`) | Calcular regra de negócio, acessar repositório, conter `if` de validação |
| **Service** (`application`) | Orquestrar caso de uso, abrir transação (`@Transactional`), checar existência (404) e regras de estado (409), chamar calculadoras de domínio e repositórios | Conhecer HTTP (`ResponseEntity`, status) |
| **Domain** | Entidades, enums, calculadoras puras (sem Spring, sem I/O), invariantes | Depender de repositório ou de web |
| **Infrastructure** | Repositórios Spring Data, consultas com parâmetros nomeados | Regra de negócio |

Barema B2 "Camadas": **cada** método de controller que calcula regra ou acessa banco conta como ocorrência
(3+ ocorrências zeram o critério).

## 3. Regras de negócio em um único lugar (B2 "Regras de negócio")

Os 6 cálculos avaliados ficam **cada um em uma única classe**, reutilizada por todos os pontos de uso:

| Cálculo | Classe | Usado em |
| --- | --- | --- |
| Valor de mercado (RN02–RN05) | `MarketValueCalculator` | resposta de jogador, listagem/filtros, compra, venda, valor do elenco |
| Overall (RN06) | `OverallCalculator` | resposta de jogador, cálculo do valor base |
| Valor base (RN06) | `BaseValueCalculator` | cadastro |
| Taxa de compra (RN13) | `PurchaseFeeCalculator` | compra |
| Multa — dias restantes + percentual + valor (RN15) | `LoyaltyPenaltyCalculator` | venda **e** contrato no `GET /clubs/{id}` |
| Comissão (RN16) | `SaleCommissionCalculator` | venda |
| Placar da partida (RN20) | `MatchEngine` | partida |

Calculadoras são classes puras e stateless (`@Component` ou métodos estáticos de uma classe final), recebem
valores e devolvem valores — facilitam o teste unitário com as tabelas do enunciado. Datas: receber o
"agora" como parâmetro (vindo de um `Clock` injetado) para testar os dias restantes de forma determinística.

Tabelas fixas (pesos por posição, faixas de multa, setores das formações) ficam **no próprio enum** (ex.:
`Position.weights()`, `Formation.requiredDefenders()`), aplicando Open/Closed: nova posição ou formação não
altera as calculadoras.

## 4. Princípios aplicados

- **SRP:** uma classe por cálculo; um serviço por caso de uso agregado (compra ≠ venda ≠ listagem); exportação
  CSV numa classe própria.
- **OCP:** comportamento por posição/condição/formação nos enums; ordenações como `Comparator` compostos a
  partir de um enum `PlayerSort`.
- **LSP/ISP:** interfaces pequenas apenas quando há mais de uma implementação real; não criar interface
  "por cerimônia" para cada serviço.
- **DIP:** serviços dependem de repositórios (interfaces Spring Data) e de `Clock` injetado, nunca de
  `LocalDateTime.now()` direto.
- **Clean Code:** nomes em **inglês** e nas convenções do Java (`camelCase`, `PascalCase`, `UPPER_SNAKE`);
  métodos curtos; sem código comentado nem morto; sem `System.out`; sem números mágicos (constantes nomeadas:
  `INITIAL_CASH`, `MAX_SQUAD_SIZE`, `MAX_CLUBS`, `PENALTY_CAP`…). Exceções aos nomes: campos JSON snake_case,
  valores de lista do enunciado (`APTO`, `COMPRA`, `GOL`…) e nomes de tabelas/colunas do banco (mapeados com
  `@Table`/`@Column`).
- Barema B2 "Código morto e padrão": identificadores misturando idiomas contam como fora do padrão — por isso o
  código é todo em inglês e o português aparece só em mapeamentos de banco e valores do enunciado.

## 5. Validação de entrada (B2 "Validação e erros")

Todos os endpoints validam por **mecanismo declarado** (Bean Validation nos DTOs, conversores/parsers
reutilizáveis); `if` dentro do controller não conta. Pontos críticos no Spring:

### Jackson (corpo JSON)
- `PropertyNamingStrategies.SNAKE_CASE` (ou `@JsonNaming` nos DTOs).
- `FAIL_ON_UNKNOWN_PROPERTIES = false` → V7 (campos extras ignorados).
- `MapperFeature.ALLOW_COERCION_OF_SCALARS = false` e coerção de texto desabilitada → `"15"` em campo inteiro é
  400 (V2). Booleans também não aceitam string.
- **Inteiros:** `50.0` deve ser aceito e `50.5` recusado. `ACCEPT_FLOAT_AS_INT=false` recusaria os dois, então
  usar um deserializador próprio para `Integer` (aceita número inteiro ou float com parte fracionária zero;
  recusa texto, booleano e fração).
- `FAIL_ON_NUMBERS_FOR_ENUMS = true`; enums são *case-sensitive* por padrão (V5). Formação (`"2-1-1"`) usa
  `@JsonValue`/`@JsonCreator`.
- **Raiz não-objeto** (`[]`, `"x"`, `10`) e JSON malformado → `HttpMessageNotReadableException` → 400 (V1).
  Garantir que records/DTOs não tenham construtores de delegação que aceitem um escalar.
- **V4 (normalização de nomes):** aplicar a normalização (trim, colapsar espaços) **antes** das validações de
  tamanho — por um deserializador de String dedicado (`@JsonDeserialize(using = NormalizedTextDeserializer)`)
  ou normalizando no construtor compacto do record. Caracteres de controle → 400. Texto vazio após normalizar
  → tratado como ausente (`@NotNull` falha → 400).
- Validações cruzadas (soma ≤ 480, `potential` ≥ maior atributo, `potential` = maior atributo com 33+,
  agilidade ≤ 85 com 35+, sigla × nome) via **constraint de classe** (`@ValidPlayerAttributes`,
  `@AcronymMatchesName`) — continuam sendo mecanismo declarado.

### Ids na URL (V8)
- Id inválido (`abc`, `0`, `-1`, `1.5`, `01`, > 2147483647) → **404**, não 400. Implementado com a regex de
  rota `PathIds.ID` (`{id:[1-9][0-9]*}`): o que não casa vira `NoResourceFoundException` → 404; o overflow
  vira `MethodArgumentTypeMismatchException` em `@PathVariable` → 404 no `GlobalExceptionHandler`.

### Query params
- Parser dedicado por endpoint (`PlayerListQueryParser`, `TransactionFilterParser`…) que recebe os valores
  brutos (`String`) e aplica os regex de inteiro/decimal, listas case-sensitive, vazio → 400, contraditórios →
  400. Parâmetros desconhecidos ignorados.

### Datas (RN01)
- `BrasiliaDateTime.parseRequest(String)`: regex `^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}-03:00$` +
  `DateTimeFormatter` com `ResolverStyle.STRICT` (`uuuu-MM-dd'T'HH:mmXXX`).
- `BrasiliaDateTime.format(...)`: `uuuu-MM-dd'T'HH:mm:ssXXX` sempre em `-03:00`.
- Configurar `loyalty_until` como `String` no DTO e converter no parser, para que a mensagem e o status sejam
  controlados.

### Ordem das verificações
Sempre **400 (formato) → 404 (existência) → 409 (regra de negócio)**, como no fluxograma da RN20.

## 6. Tratamento central de erros

- Hierarquia: `ApiException` (abstrata, com `HttpStatus`) → `InvalidRequestException` (400),
  `ResourceNotFoundException` (404), `BusinessRuleException` (409).
- `@RestControllerAdvice GlobalExceptionHandler` converte para `ErrorResponse(String message)`:
  - `MethodArgumentNotValidException`, `HttpMessageNotReadableException`, `ConstraintViolationException`,
    `InvalidRequestException` → 400;
  - `NoResourceFoundException` / `NoHandlerFoundException` e `ResourceNotFoundException` → 404;
  - `HttpRequestMethodNotSupportedException` → 405 (ou 404);
  - `HttpMediaTypeNotSupportedException` → 400 (corpo sem JSON);
  - `DataIntegrityViolationException` em unicidade de clube → 409 (rede de segurança para concorrência);
  - `Exception` → 500 com mensagem genérica, logando o stack trace **apenas no log**.
- `server.error.include-stacktrace=never`, `server.error.include-message=never`; nenhuma resposta traz stack
  trace. Mensagens em português, claras, com os valores relevantes formatados.

## 7. Transações do banco (B2 "Consistência dos dados")

`@Transactional` no método de serviço de cada operação atômica, englobando **todas** as gravações:

| Operação | Gravações na mesma transação |
| --- | --- |
| Compra | caixa, `jogadores.clube_id`, contrato `ATIVO`, transação `COMPRA` |
| Venda | caixa, jogador livre, contrato `RESCINDIDO`/`ENCERRADO`, transação `VENDA`, saída da escalação |
| Partida | partida + 6 duelos |
| Escalação | `clubes.formacao` + substituição dos 5 titulares |
| Condição → `DOENTE` | condição + saída da escalação |

Serviços de leitura com `@Transactional(readOnly = true)`. Recusas lançam exceção antes de gravar (rollback
automático). Considerar `@Lock(PESSIMISTIC_WRITE)` ao carregar o clube na compra/venda para evitar condições de
corrida no caixa.

## 8. Listagens e ordenação

- **Ordem alfabética** sempre pelo `AlphabeticalComparator` (Java), não pela collation do PostgreSQL — a
  collation do banco não segue a regra das Premissas (code point após NFD sem Mn e minúsculas).
- `GET /players`: filtrar no banco o que é coluna simples (não excluído, posição, condição, clube/livre, idade)
  e, em memória, calcular valor de mercado, aplicar `search` (normalizado), faixa de valor, ordenação e
  paginação. Volume do desafio é pequeno; registrar a decisão no README.
- `GET /clubs` por patrimônio, elenco (RN11), escalação, standings: ordenações em memória com comparadores
  compostos reutilizáveis.

## 9. Respostas HTTP

- `POST` de criação → `ResponseEntity.created(URI.create("/api/v1/players/" + id))`.
- `DELETE` → `204` sem corpo.
- Valores monetários como `BigDecimal` com escala 2 (`setScale(2)`) — serializar como número JSON
  (`WRITE_BIGDECIMAL_AS_PLAIN`).
- CSV: `ResponseEntity<byte[]>` com `MediaType("text", "csv", UTF_8)` e bytes UTF-8 sem BOM.
- Sem barra final nas rotas (padrão do Spring Boot 3: trailing slash não casa).

## 10. Testes

- **Unitários** (JUnit 5 + AssertJ, `@ParameterizedTest` com `@CsvSource`) para cada calculadora, usando as
  tabelas de exemplo do enunciado: valor de mercado, overall, valor base, taxa/limite de 50%, multa/dias
  restantes, comissão/crédito, motor da partida, ordem alfabética, parse RN01.
- **Integração** (`@SpringBootTest` + MockMvc + Testcontainers PostgreSQL) cobrindo os fluxos e códigos de erro
  do roteiro: validações V1–V9, compra/venda, invariante `300 + Σ cash_change = caixa`, CSV byte a byte.
- Comando único que termina sozinho: `./mvnw test`. Pelo menos um teste do valor de mercado (exigência do B5).

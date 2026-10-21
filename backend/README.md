# LigaHub — Backend

API REST do estudo de caso SENAI/PE – CÓD. 237 (mercado de jogadores de uma liga fictícia): jogadores, clubes,
compra e venda com contrato de fidelidade, histórico de transações com exportação CSV, escalação, partidas e
classificação.

- Origem da API: **`http://localhost:8080`** (todas as rotas em `/api/v1`)
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## 1. Pré-requisitos

| Ferramenta | Versão | Observação |
| --- | --- | --- |
| Java (JDK) | 21 | `openjdk-21-jdk` no Ubuntu 24.04 |
| Maven | 3.9 (via wrapper) | não precisa instalar: o `./mvnw` versionado baixa o Maven |
| Docker Engine + Compose v2 | 24 ou superior | sobe o PostgreSQL (e, se quiser, a aplicação inteira) |
| PostgreSQL | 16 | imagem pública `postgres:16`, via Docker Compose |

## 2. Banco de dados

O esquema é criado pelas migrations do Flyway (`src/main/resources/db/migration`), aplicadas automaticamente
quando a API sobe. O banco começa vazio, sem dados de exemplo.

Criar o banco (a partir da raiz do repositório):

```bash
docker compose up -d db
```

Recriar do zero (apaga o volume; o Flyway recria o esquema na próxima vez que a API subir):

```bash
docker compose down -v
docker compose up -d db
```

## 3. Instalar e executar (Ubuntu 24.04)

```bash
sudo apt-get update
sudo apt-get install -y openjdk-21-jdk git
git clone https://github.com/luanmvcosta0/desafio-tecnico-isitics-dev1.git
cd desafio-tecnico-isitics-dev1
docker compose up -d db
cd backend
cp .env.example .env
./mvnw spring-boot:run
```

A API fica em `http://localhost:8080` (origem, sem `/api/v1`). O arquivo de trava das dependências é o próprio
`pom.xml` (versões fixas, herdadas do `spring-boot-starter-parent` 3.5.16) junto com o wrapper do Maven
versionado em `.mvn/wrapper`.

Alternativa com Docker para tudo (banco, API e frontend), a partir da raiz: `docker compose up --build -d`.
Ver o [README da raiz](../README.md).

## 4. Variáveis de ambiente

Modelo em [`.env.example`](.env.example). A API lê o arquivo `.env` desta pasta (opcional) e as variáveis do
ambiente; os valores do exemplo funcionam com o banco do `docker-compose.yml`.

| Variável | Exemplo | Uso |
| --- | --- | --- |
| `DB_HOST` | `localhost` | host do PostgreSQL |
| `DB_PORT` | `5432` | porta do PostgreSQL |
| `DB_NAME` | `ligahub` | nome do banco |
| `DB_USER` | `ligahub` | usuário do banco |
| `DB_PASSWORD` | `ligahub` | senha do banco (só por variável de ambiente) |
| `SERVER_PORT` | `8080` | porta HTTP da API |
| `CORS_ALLOWED_ORIGIN` | `http://localhost:5173` | única origem liberada no CORS (a do frontend) |

## 5. Estrutura e bibliotecas

Organização por funcionalidade (*package-by-feature*). Cada módulo tem `api` (controllers e DTOs),
`application` (serviços e transações), `domain` (entidades, enums e calculadoras puras) e `infrastructure`
(repositórios).

```
src/main/java/com/dev1_isitics/ligahub_be/
├── modules/
│   ├── players/   # cadastro, edição, condição, exclusão lógica, busca e valores (RN02–RN09)
│   ├── clubs/     # clubes, elenco, valor do elenco e patrimônio (RN10, RN11)
│   ├── market/    # compra, venda, contratos, histórico e CSV (RN01, RN12–RN18)
│   └── league/    # escalação, motor da partida, partidas e classificação (RN19–RN21)
└── shared/        # config (CORS, Jackson, Clock), exceções e handler global, dinheiro, datas,
                   # texto normalizado, validações e log de requisições
src/main/resources/
├── application.yaml
└── db/migration/  # migrations do Flyway
src/test/java/...  # testes unitários das calculadoras e de integração (MockMvc + Testcontainers)
docs/              # regras de negócio, contrato da API, banco, arquitetura e infraestrutura
```

| Biblioteca | Motivo |
| --- | --- |
| `spring-boot-starter-web` | API REST (Spring MVC, Jackson, Tomcat) |
| `spring-boot-starter-data-jpa` | persistência com JPA/Hibernate e repositórios com consultas parametrizadas |
| `spring-boot-starter-validation` | Bean Validation dos DTOs e constraints customizadas |
| `flyway-core` + `flyway-database-postgresql` | migrations versionadas que criam e recriam o esquema |
| `postgresql` | driver JDBC do PostgreSQL |
| `springdoc-openapi-starter-webmvc-ui` | documentação OpenAPI e Swagger UI |
| `lombok` | reduz código repetitivo nas entidades (getters, construtores, logger) |
| `spring-boot-devtools` | reinício automático durante o desenvolvimento (não entra no jar de produção) |
| `spring-boot-starter-test` | JUnit 5, AssertJ e MockMvc nos testes |
| `spring-boot-testcontainers` + `testcontainers` (`junit-jupiter`, `postgresql`) | testes de integração contra um PostgreSQL 16 real em contêiner |
| `maven-checkstyle-plugin` (Checkstyle 10) | lint (`checkstyle.xml`) |
| `spotless-maven-plugin` (google-java-format) | verificação e aplicação da formatação |

## 6. Decisões e premissas

- **Datas e horas:** instantes guardados como `TIMESTAMPTZ` no banco e `Instant` em Java; a conversão para o
  horário de Brasília (`-03:00`) acontece só na entrada e na saída da API. A hora atual vem de um `Clock`
  injetado, o que deixa as regras de tempo testáveis.
- **Valores calculados nunca são gravados:** valor de mercado, overall, valor do elenco, patrimônio, dias
  restantes, % de multa, pontos e posição são calculados a cada consulta. O valor base é calculado no cadastro e
  gravado, porque não muda depois (RN08).
- **Cada cálculo em uma única classe de domínio:** `MarketValueCalculator`, `OverallCalculator`,
  `BaseValueCalculator`, `PurchaseFeeCalculator`, `LoyaltyPenaltyCalculator`, `SaleCommissionCalculator`,
  `MatchEngine` e `StandingsCalculator`.
- **Ordem alfabética em Java, não no banco:** a collation do PostgreSQL não segue a regra do enunciado
  (minúsculas, sem acentos, por code point, empate por id), então a ordenação usa o `AlphabeticalComparator`.
- **Listagem de jogadores:** o banco filtra o que é coluna simples (excluído, posição, condição, clube/livre,
  idade); busca sem acentos, faixa de valor de mercado, ordenação e paginação são feitas em memória. O volume da
  liga (no máximo 20 clubes de 12 jogadores, mais os livres) é pequeno.
- **Transações e concorrência:** compra, venda, escalação, partida (com os 6 duelos) e mudança de condição
  rodam cada uma numa única transação; uma recusa desfaz tudo. Na compra e na venda, o clube e o jogador são
  carregados com trava pessimista (`PESSIMISTIC_WRITE`) para que duas operações simultâneas não alterem o mesmo
  caixa.
- **Nome único do clube:** índice único funcional em `lower(nome)`, que ignora maiúsculas e minúsculas mas
  considera acentos. Por isso a imagem é `postgres:16` (Debian, locale UTF-8) e não a alpine.
- **Agregados independentes:** `Player` referencia o clube pelo id, sem `@ManyToOne`; os serviços buscam o que
  precisam nos repositórios.
- **Ids inválidos na URL** (`abc`, `0`, `-1`, `01`, `1.5`, maior que 2.147.483.647) respondem 404, como
  recurso inexistente.
- **Logs:** uma linha por requisição (`RequestLoggingFilter`) com data/hora, método, caminho e status.

## 7. Funcionalidades não implementadas

Nenhuma.

## 8. Uso de inteligência artificial

Ver a seção "Uso de inteligência artificial" do [README da raiz](../README.md#8-uso-de-inteligência-artificial).

## Comandos dos diferenciais

```bash
./mvnw test               # testes unitários e de integração (precisa do Docker para o Testcontainers)
./mvnw checkstyle:check   # lint
./mvnw spotless:check     # verifica a formatação (./mvnw spotless:apply corrige)
./mvnw -B package         # build do jar
```

- Swagger UI: `http://localhost:8080/swagger-ui.html` (OpenAPI em `/v3/api-docs`)
- Docker: `docker compose up --build -d` na raiz sobe banco, API e frontend

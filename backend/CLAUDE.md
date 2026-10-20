# CLAUDE.md — LigaHub Backend

API REST do estudo de caso **SENAI/PE – CÓD. 237 Desenvolvedor I (Fullstack)**: mercado de jogadores de uma
liga de futebol fictícia (jogadores, clubes, compra/venda com contrato de fidelidade, histórico, escalação,
partidas e classificação). Enunciado original: https://github.com/isi-tics/desafio-isi-dev-1.

A correção é feita por **script automatizado** que confere código HTTP, corpo e cabeçalhos item a item
(tudo ou nada) e por **leitura de código** com rubrica. Precisão nas regras vale mais que qualquer extra.

## Documentação de referência (ler antes de implementar a parte correspondente)

| Arquivo | Conteúdo |
| --- | --- |
| [docs/regras-de-negocio.md](docs/regras-de-negocio.md) | Premissas, validações V1–V9, limites dos campos, RN01–RN21 com as tabelas de exemplo (= casos de teste) |
| [docs/contrato-api.md](docs/contrato-api.md) | Endpoints, query params, formatos JSON de request/response, erros |
| [docs/banco-de-dados.md](docs/banco-de-dados.md) | As 7 tabelas obrigatórias, colunas, PKs/FKs, unicidades |
| [docs/arquitetura.md](docs/arquitetura.md) | Pacotes, camadas, SOLID, validação, erros, transações, testes |
| [docs/infraestrutura.md](docs/infraestrutura.md) | Docker, env vars, CORS, logs, Swagger, lint, README e entrega |
| [docs/barema-backend.md](docs/barema-backend.md) | Checklist de pontuação B1–B5 e eliminações |
| [docs/fluxo-git.md](docs/fluxo-git.md) | Branches, Conventional Commits, commits atômicos, PRs e roteiro de PRs |

Se algo aqui divergir do enunciado original, **o enunciado prevalece** — corrija a doc.

## Stack (obrigatória — tecnologia fora da lista elimina)

- Java **21**, Spring Boot **3.5.x**, Maven (`./mvnw`)
- Spring Web, Spring Data JPA (Hibernate), Bean Validation, springdoc-openapi, Lombok
- PostgreSQL **16** (H2/SQLite/HSQLDB proibidos como banco da aplicação; nos testes, liberado)
- Flyway para migrations; Testcontainers nos testes de integração; Checkstyle + Spotless para lint/formatação

## Comandos

```bash
docker compose up -d db               # sobe o PostgreSQL
./mvnw spring-boot:run                # API em http://localhost:8080 (prefixo /api/v1)
./mvnw test                           # testes (unitários + integração); termina sozinho
./mvnw checkstyle:check               # lint
./mvnw spotless:check                 # verifica formatação (spotless:apply corrige)
./mvnw -B package                     # build do jar
docker compose down -v                # recria o banco do zero (remove o volume)
```
Swagger UI: `http://localhost:8080/swagger-ui.html`.

## Regras inegociáveis do contrato

- Base `/api/v1`, **sem barra final**; JSON em **snake_case** (corpo, respostas, query params).
- Toda recusa: corpo `{"message": "..."}` não vazio, `Content-Type: application/json`, **sem stack trace**,
  **nada gravado**.
- Códigos: formato inválido **400** → inexistente **404** → regra de negócio **409** (nessa ordem de checagem).
- Id inválido na URL (`abc`, `0`, `-1`, `01`, `1.5`, > 2147483647) → **404**, não 400.
- Campos desconhecidos ou controlados pelo sistema no corpo são **ignorados** (V7); `"15"` em campo inteiro é
  400; `50.0` é aceito como 50; `50.5` é 400; enums *case-sensitive*.
- `POST /players`, `/clubs`, `/matches` → **201 + `Location`**; `DELETE` → **204** sem corpo.
- Datas de entrada estritamente `AAAA-MM-DDTHH:MM-03:00`; saída `AAAA-MM-DDTHH:MM:SS-03:00`.
- Moeda: `BigDecimal`, 2 casas, `RoundingMode.HALF_UP`, arredondar **só no resultado final** de cada cálculo.
- Ordem alfabética: minúsculas + NFD sem marcas Mn, comparação por code point, empate por id — em Java, não
  pela collation do banco.
- Posições: `GOL, ZAG, LAT, MEI, ATA` (essa é também a ordem de elenco/listagem). Condições: `APTO,
  MAL_CONDICIONADO, DOENTE`.
- Valor de mercado, overall, valor do elenco, patrimônio, dias restantes, % de multa, pontos e posição na
  classificação **nunca** são gravados no banco.
- CORS só para a origem do frontend vinda de `CORS_ALLOWED_ORIGIN` (`*` proibido).

## Convenções de código

- **Package-by-feature** em `modules/{players,clubs,market,league}` com subpacotes `api` (controllers + DTOs),
  `application` (serviços), `domain` (entidades, enums, calculadoras) e `infrastructure` (repositórios);
  transversais em `shared/`. Detalhes em `docs/arquitetura.md`.
- Controller só recebe DTO validado, chama um serviço e monta a resposta. **Nada de regra de negócio nem
  repositório no controller.**
- Cada cálculo (valor de mercado, overall, valor base, taxa, multa, comissão, placar) existe em **uma única
  classe** de domínio, pura e testável. Nunca duplicar fórmula.
- Validação por mecanismo declarado (Bean Validation, constraints customizadas, parsers reutilizáveis), nunca
  `if` solto no controller. Erros via exceções (`InvalidRequestException`, `ResourceNotFoundException`,
  `BusinessRuleException`) tratadas no `GlobalExceptionHandler`.
- `@Transactional` nos serviços de compra, venda, partida, escalação e alteração de condição.
- Hora atual sempre de um `Clock` injetado (nunca `now()` sem clock).
- DTOs como `record`. Entidades JPA mapeiam tabelas/colunas em português via `@Table`/`@Column`; **todo o
  código Java em inglês** (misturar idiomas em identificadores perde ponto). Mensagens de erro em português.
- Sem código comentado, sem código morto, sem `System.out`, sem números mágicos (constantes nomeadas).
- Testes: calculadoras com `@ParameterizedTest` usando as tabelas do enunciado; integração com MockMvc +
  Testcontainers.
## Git, commits e PRs (detalhes em `docs/fluxo-git.md`, idêntico no backend e no frontend)

- Nunca commitar direto na `main`: uma branch por funcionalidade, `<tipo>/<escopo>-<descricao>`
  (ex.: `feat/players-registration`, `feat/players-table`, `fix/market-fee-rounding`).
- Conventional Commits em português: `<tipo>(<escopo>): <descrição no imperativo>`, com `Refs: RNxx, Bxx, Fxx`
  quando atender regras ou itens do roteiro. Escopos de domínio: `players`, `clubs`, `market`, `league`;
  técnicos: `shared`, `db` (backend), `ui`, `api`, `layout` (frontend), `infra`, `docs`.
- Mensagens genéricas ("fix", "update", "wip", "ajustes") são proibidas.
- Commits **atômicos**: uma mudança lógica por commit, testes, lint e build verdes em cada um, teste junto do
  código testado, sem misturar refatoração/formatação com funcionalidade, sem misturar backend e frontend.
- PR por funcionalidade, título em Conventional Commits, corpo com regras/itens atendidos, como testar e
  checklist.
- Merge por rebase ou merge commit — **nunca squash** (o barema conta os commits de cada pasta).
- Push pelo menos uma vez por dia; nunca `push --force` na `main`.
- Só commitar/abrir PR quando o usuário pedir.

## Contexto da entrega

- Prazo: **23/10/2026 às 20h (Brasília)**.
- O repositório entregue deve ter `backend/` e `frontend/` na raiz (a pasta-pai deste projeto é a raiz do
  monorepo). O barema conta commits que alteram `backend/` e pushes em ≥ 3 dias no repositório entregue.
- O `mvnw` precisa ficar versionado como executável (modo 100755), senão `./mvnw` falha na máquina da banca.
- O README precisa da seção "Uso de inteligência artificial" descrevendo onde e como o Claude foi usado.

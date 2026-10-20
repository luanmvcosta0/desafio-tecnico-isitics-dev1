# Infraestrutura, configuração e entrega — LigaHub (backend)

## 1. Ambiente da banca (seguir à risca)

- Máquina limpa **Ubuntu 24.04 LTS** (amd64), 4 vCPU, 8 GB, Docker Engine + Compose v2 sem `sudo`, fuso
  `America/Sao_Paulo`, Chrome. A banca instala só os runtimes pedidos no README (**Java 21 + Maven**, ou usa o
  `./mvnw`) e executa **literalmente** os comandos do README, na ordem, em até **30 minutos**.
- A única ação extra permitida: `cp .env.example .env` sem alterar valores. Comando com valor a preencher sem
  valor no README = comando que falhou.
- Tudo deve ser construído a partir do repositório, com pacotes e **imagens públicas** (nada de imagem própria,
  submódulo ou serviço hospedado).
- O banco é **recriado do zero** antes de cada roteiro pelos comandos de recriação do README.
- O script chama `<origem informada no README>/api/v1`. Informar a origem **sem** `/api/v1`
  (ex.: `http://localhost:8080`).
- Se o backend não subir, zera B1 e os diferenciais que dependem da aplicação no ar.

## 2. Stack e versões

| Ferramenta | Versão |
| --- | --- |
| Java | 21 (permitido: 17 ou 21) |
| Spring Boot | 3.5.x (exigido: Spring Boot 3) |
| Maven | wrapper `./mvnw` (versionado) |
| PostgreSQL | 16 (imagem pública `postgres:16`, base Debian — **não usar a alpine**: a unicidade do nome do clube depende de `lower()` tratar letras acentuadas, o que exige locale UTF-8 da glibc) |
| Flyway | migrations versionadas (gerenciado pelo Spring Boot) |
| springdoc-openapi | 2.8.x (Swagger UI) |
| Lombok | redução de boilerplate em entidades |

Toda biblioteca complementar deve constar no README **com o motivo do uso** (biblioteca listada que não está nas
dependências conta como item incorreto).

## 3. Variáveis de ambiente (`.env.example` obrigatório)

| Variável | Exemplo | Uso |
| --- | --- | --- |
| `DB_HOST` | `localhost` | host do PostgreSQL |
| `DB_PORT` | `5432` | porta |
| `DB_NAME` | `ligahub` | banco |
| `DB_USER` | `ligahub` | usuário |
| `DB_PASSWORD` | `ligahub` | senha (segredo: **só** por variável de ambiente) |
| `SERVER_PORT` | `8080` | porta da API |
| `CORS_ALLOWED_ORIGIN` | `http://localhost:5173` | origem do frontend (CORS restrito; `*` proibido) |

`application.yaml` lê com `${DB_HOST:localhost}` etc. Nenhuma credencial real versionada (senha de banco local
no compose e valores de exemplo não contam). Toda variável usada pelo código precisa estar listada no README.

## 4. Docker

- `Dockerfile` multi-stage no backend: build com `maven:3.9-eclipse-temurin-21` (`./mvnw -B -DskipTests
  package`) e runtime com `eclipse-temurin:21-jre`.
- `docker-compose.yml` na **raiz do monorepo** com os serviços `db` (postgres:16, volume nomeado,
  healthcheck `pg_isready`), `backend` (`depends_on: db: condition: service_healthy`) e `frontend`.
- Diferencial B5: **um único comando** (`docker compose up --build -d`) sobe interface, API e banco, e a lista de
  jogadores abre na URL do frontend.
- Recriar o banco: `docker compose down -v && docker compose up -d db` (o Flyway recria o esquema ao subir a API).

## 5. Diferenciais técnicos (B5 — 0,5 cada, até 2,0)

| Diferencial | Como atender | Verificação da banca |
| --- | --- | --- |
| Testes automatizados | JUnit 5 + Testcontainers; `./mvnw test` | termina sozinho em ≤ 10 min, sem falhas, com ≥ 1 teste do valor de mercado |
| Docker + Compose | `docker compose up --build -d` | sobe front, API e banco num comando |
| Swagger/OpenAPI | springdoc; URL `http://localhost:8080/swagger-ui.html` | abre com **todos** os endpoints do enunciado |
| Logs de requisição | `OncePerRequestFilter` + SLF4J/Logback | **uma única linha** por requisição com data/hora legível, método, caminho e status |
| Lint e formatação | Checkstyle (`./mvnw checkstyle:check`) + Spotless/google-java-format (`./mvnw spotless:check`) | terminam sem erro; o de formatação só verifica |

Swagger: ao configurar o springdoc, garantir que os schemas apareçam em snake_case (mesmo `ObjectMapper`) e
que as rotas do Swagger não conflitem com o tratamento de 404.

Logs: exemplo de linha
`2026-10-20T14:03:12.511-03:00 INFO  RequestLoggingFilter : GET /api/v1/players 200`.

## 6. CORS

`WebMvcConfigurer.addCorsMappings("/api/**")` com `allowedOrigins(<CORS_ALLOWED_ORIGIN>)`, métodos `GET, POST,
PUT, PATCH, DELETE, OPTIONS` e `exposedHeaders("Location", "Content-Disposition")`. Nunca `*` nem origem
refletida.

## 7. Estrutura do repositório de entrega (atenção!)

O regulamento exige **um único repositório Git público** com as pastas **`backend/`** e **`frontend/`** na raiz.
Commit sem nenhuma das duas pastas **elimina**. O B4 conta só commits que **alteram a pasta `backend/`**, e os
**dias de envio (push) no repositório entregue** (mínimo 10 commits em 3 dias diferentes, no máximo metade com
mensagens genéricas).

Prazo: **23/10/2026, 20h (Brasília)**. Repositório público até 26/10/2026 12h.

## 8. Requisitos do README (raiz, podendo apontar para `backend/README.md`)

Cada item em trecho próprio:
1. Pré-requisitos com nome e versão (Java 21, Maven via wrapper, Docker, PostgreSQL 16 via Docker).
2. Passo a passo para **criar** o banco e **recriá-lo do zero** (migrations versionadas; banco começa vazio).
3. Passo a passo para instalar e executar no Ubuntu 24.04, só com comandos copiáveis; **origem da API** (sem
   `/api/v1`) e URL do frontend; arquivos de trava versionados.
4. Variáveis de ambiente, com `.env.example`.
5. Estrutura de pastas e lista de bibliotecas com o motivo de cada uma.
6. "Decisões e premissas" (detalhes não definidos pelo enunciado).
7. "Funcionalidades não implementadas" (ou "Nenhuma").
8. "Uso de inteligência artificial": ferramentas, **onde** (arquivos/funcionalidades), **como e por que**, e o
   que foi aceito, alterado ou descartado. (Obrigatório.)

Mais: comandos dos diferenciais (testes, lint, formatação, Swagger, Docker).

## 9. Commits

- Mensagens descritivas em Conventional Commits (`feat(players): ...`, `fix(market): ...`), nunca genéricas
  ("update", "fix", "wip", "ajustes" sozinhas contam como genéricas).
- Commits pequenos e coerentes, enviados (push) em vários dias.

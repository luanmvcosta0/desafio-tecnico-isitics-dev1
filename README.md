# LigaHub

Estudo de caso **SENAI/PE – CÓD. 237 Desenvolvedor I (Fullstack)**: mercado de jogadores de uma liga de futebol
fictícia, com jogadores, clubes, compra e venda com contrato de fidelidade, histórico de transações com
exportação CSV, escalação, partidas e classificação.
Enunciado: https://github.com/isi-tics/desafio-isi-dev-1

| Pasta | Stack | Documentação |
| --- | --- | --- |
| [`backend/`](backend) | Java 21, Spring Boot 3.5, PostgreSQL 16, Flyway | [backend/README.md](backend/README.md) |
| [`frontend/`](frontend) | React 19, Vite 8, TypeScript, Tailwind CSS 4, React Router (modo Declarative) | [frontend/README.md](frontend/README.md) |

- **Origem da API:** `http://localhost:8080` (rotas em `/api/v1`)
- **URL do frontend:** `http://localhost:5173`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`

## 1. Pré-requisitos

| Ferramenta | Versão | Para quê |
| --- | --- | --- |
| Docker Engine + Compose v2 | 24 ou superior | banco (sempre) e, na opção A, também API e frontend |
| Git | qualquer recente | clonar o repositório |
| Java (JDK) | 21 | só na opção B (API fora do Docker); Maven 3.9 vem pelo wrapper `./mvnw` |
| Node.js | 22 LTS (≥ 22.12.0) | só na opção B (frontend fora do Docker) |
| PostgreSQL | 16 | imagem pública `postgres:16`, sobe pelo Docker Compose |

## 2. Banco de dados

O esquema é criado pelas migrations versionadas do Flyway (`backend/src/main/resources/db/migration`) quando a
API sobe. O banco começa vazio.

- **Criar:** acontece no passo a passo abaixo (`docker compose up ...`).
- **Recriar do zero** (opção A), a partir da raiz:

  ```bash
  docker compose down -v
  docker compose up --build -d
  ```

- **Recriar do zero** (opção B): `docker compose down -v` e `docker compose up -d db`, e então subir a API de
  novo (detalhes no [README do backend](backend/README.md#2-banco-de-dados)).

## 3. Instalar e executar (Ubuntu 24.04)

Escolha **uma** das opções.

### Opção A (recomendada): tudo com Docker, um comando

```bash
git clone https://github.com/luanmvcosta0/desafio-tecnico-isitics-dev1.git
cd desafio-tecnico-isitics-dev1
docker compose up --build -d
```

Sobe o banco, a API (`http://localhost:8080`) e o frontend (`http://localhost:5173`). A primeira subida baixa
as dependências e leva alguns minutos; a API fica disponível quando `docker compose logs backend` mostrar
`Started LigahubBeApplication`.

### Opção B: banco no Docker, API e frontend locais

Siga o passo a passo do [README do backend](backend/README.md#3-instalar-e-executar-ubuntu-2404) e, em outro
terminal, o do [README do frontend](frontend/README.md#instalar-e-executar-ubuntu-2404).

Arquivos de trava versionados: `backend/pom.xml` com o wrapper `backend/.mvn/wrapper` e
`frontend/package-lock.json`.

## 4. Variáveis de ambiente

Cada parte tem o seu `.env.example`; os valores do exemplo funcionam sem alteração.

- Backend ([`backend/.env.example`](backend/.env.example)): `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`,
  `DB_PASSWORD`, `SERVER_PORT`, `CORS_ALLOWED_ORIGIN`. Tabela completa no
  [README do backend](backend/README.md#4-variáveis-de-ambiente).
- Frontend ([`frontend/.env.example`](frontend/.env.example)): `VITE_API_ORIGIN`. Ver o
  [README do frontend](frontend/README.md#variáveis-de-ambiente).
- `docker-compose.yml`: usa os mesmos nomes, com padrões embutidos (`${DB_NAME:-ligahub}` etc.), então a
  opção A não precisa de `.env`.

## 5. Estrutura de pastas e bibliotecas

```
.
├── docker-compose.yml   # db (postgres:16), backend e frontend
├── backend/             # API Spring Boot (package-by-feature: players, clubs, market, league + shared)
│   └── docs/            # regras de negócio, contrato da API, banco, arquitetura, infraestrutura
└── frontend/            # SPA React (pages → features → services/api → httpClient)
    └── docs/            # requisitos das telas, integração com a API, arquitetura, infraestrutura
```

As bibliotecas, com o motivo de cada uma, estão no
[README do backend](backend/README.md#5-estrutura-e-bibliotecas) e no
[README do frontend](frontend/README.md#bibliotecas).

## 6. Decisões e premissas

As decisões do backend estão no [README do backend](backend/README.md#6-decisões-e-premissas). No frontend:

- A interface **não recalcula** nenhum valor de negócio (valores, taxas, multas, totais, placar, classificação):
  só formata o que a API devolve. A única conta na tela é o custo total da compra (valor + taxa).
- A ordem das listas é sempre a da API.
- Recusas da API (400/404/409) mostram o `message` devolvido. A tela só impede antes de enviar o que é escolha
  da própria interface: clube com "só livres", `DOENTE` → `APTO`, posição de jogador com clube, idade menor e
  as opções da escalação.
- Datas exibidas no horário de Brasília; datas digitadas são enviadas como `AAAA-MM-DDTHH:MM-03:00`.
- Cada ação desabilita o botão enquanto envia e ignora o segundo clique de um clique duplo.

## 7. Funcionalidades não implementadas

Nenhuma.

## 8. Uso de inteligência artificial

**Ferramenta:** Claude Code (Anthropic), no terminal, com acesso ao repositório.

**Onde e como:**

- **Documentação de apoio** (`backend/docs/`, `frontend/docs/` e os `CLAUDE.md`): a partir do enunciado, o
  Claude ajudou a organizar as regras de negócio (V1–V9, RN01–RN21 com as tabelas de exemplo), o contrato da
  API, o modelo do banco, a arquitetura e os baremas em checklists. Esses arquivos serviram de referência
  durante todo o desenvolvimento; quando havia divergência, valia o enunciado e a doc era corrigida.
- **Backend:** apoio na implementação das calculadoras de domínio, validações, serviços e controllers, e na
  escrita dos testes parametrizados com as tabelas do enunciado e dos testes de integração com Testcontainers.
  Também gerou o filtro de log de requisições, a configuração do Checkstyle e do Spotless e o README.
- **Frontend:** apoio nos componentes compartilhados (`DataTable`, `FormField`, diálogos), na camada
  `services/api`, nos formulários com React Hook Form + Zod, nas telas da liga e nos testes com Testing Library.
- **Revisão:** análise do que faltava para a entrega comparando o código com os baremas.

**Por quê:** acelerar a escrita de código repetitivo e de testes, e conferir sistematicamente cada regra e cada
item do barema contra o código.

**Aceito, alterado e descartado:** todo código sugerido foi lido e rodado antes do commit, com os testes, o lint
e o build verdes em cada commit. Sugestões que divergiam do enunciado ou das convenções do projeto (por
exemplo, cálculo fora da classe de domínio ou recálculo na tela) foram alteradas ou descartadas. Os commits em
que o Claude participou diretamente trazem a linha `Co-Authored-By: Claude` na mensagem.

## Comandos dos diferenciais

| O quê | Backend (`cd backend`) | Frontend (`cd frontend`) |
| --- | --- | --- |
| Testes | `./mvnw test` | `npm test` |
| Lint | `./mvnw checkstyle:check` | `npm run lint` |
| Formatação (só verifica) | `./mvnw spotless:check` | `npm run format:check` |
| Docker (na raiz) | `docker compose up --build -d` | idem |
| Swagger | `http://localhost:8080/swagger-ui.html` | — |

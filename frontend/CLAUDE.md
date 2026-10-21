# CLAUDE.md — LigaHub Frontend

Interface web do estudo de caso **SENAI/PE – CÓD. 237 Desenvolvedor I (Fullstack)**: mercado de jogadores de uma
liga fictícia (jogadores, clubes, compra/venda, histórico, escalação, partidas e classificação). Enunciado:
https://github.com/isi-tics/desafio-isi-dev-1. O backend fica em `../backend` (Spring Boot, API em
`http://localhost:8080/api/v1`).

A banca percorre a interface no Chrome (1366 × 768) com roteiro fixo, gravando tela e HAR; cada item vale tudo
ou nada. Também lê o código com rubrica (componentes, camada de API, formulários, código morto).

## Documentação de referência (ler antes de implementar a parte correspondente)

| Arquivo                                                    | Conteúdo                                                                |
| ---------------------------------------------------------- | ----------------------------------------------------------------------- |
| [docs/requisitos-frontend.md](docs/requisitos-frontend.md) | Telas, campos, ações, mensagens, estados e formatação exigidos          |
| [docs/integracao-api.md](docs/integracao-api.md)           | Endpoints por tela, tipos TypeScript da API, formações, CSV             |
| [docs/arquitetura.md](docs/arquitetura.md)                 | Stack, pastas, camadas, componentes reutilizáveis, formulários, padrões |
| [docs/infraestrutura.md](docs/infraestrutura.md)           | Versões, scripts, env, Docker, README e entrega                         |
| [docs/barema-frontend.md](docs/barema-frontend.md)         | Checklist de pontuação F1–F5                                            |
| [docs/fluxo-git.md](docs/fluxo-git.md)                     | Conventional Commits, commits atômicos e ordem sugerida de commits      |
| `../backend/docs/contrato-api.md`                          | Contrato completo da API (fonte da verdade dos formatos)                |

Se algo aqui divergir do enunciado original, **o enunciado prevalece** — corrija a doc.

## Stack (obrigatória — tecnologia fora da lista elimina)

- **React 19 + TypeScript**, projeto criado com **Vite 8** (template `react-ts`), Node.js **22 LTS**
- **React Router no modo Declarative** (`BrowserRouter`/`Routes`/`Route`, pacote `react-router`).
  **Nunca** o modo framework: sem `@react-router/dev`, sem `react-router.config.ts`, sem plugin `reactRouter()`,
  sem loaders/actions/SSR. Remix, CRA, Next, Angular e Vue são proibidos.
- **Tailwind CSS v4** via `@tailwindcss/vite` (tokens em `src/index.css`)
- React Hook Form + Zod (validação), Vitest + Testing Library (testes), oxlint + Prettier (lint/format)
- Instalação sempre com `npm ci` funcionando sem flags (nada de `--legacy-peer-deps`)

## Comandos

```bash
npm ci                 # instala pelo lockfile
npm run dev            # http://localhost:5173
npm test               # vitest run
npm run lint           # oxlint --deny-warnings
npm run format:check   # prettier --check .   (npm run format corrige)
npm run build          # tsc -b + vite build
```

## Regras inegociáveis

- **Nunca recalcular na tela** valores de negócio (valor base, valor de mercado, overall, taxa, multa, comissão,
  lucro, crédito, valor do elenco, patrimônio, dias restantes, % multa, summary, placar, duelos, classificação).
  Só formatar. Exceção: custo total = valor + taxa no resultado da compra.
- **Ordem das listas = ordem da API.** Não reordenar no cliente.
- **Nenhum `fetch` em componente**: tudo via `src/services/api/*Api.ts` → `httpClient`.
- Toda ação: mensagem de sucesso ou o **`message` da API** (nunca mensagem genérica no lugar); botão
  desabilitado durante o envio e **sem envio duplicado**; dados atualizados pela API sem recarregar a página.
- Recusas 404/409 **não** são impedidas na tela (envia e mostra o `message`), exceto as permitidas: só livres +
  clube, DOENTE → APTO, mudar posição de jogador com clube, idade menor e escolhas da escalação.
- Toda lista com estados de **carregamento, vazio e erro** (componente `DataTable` compartilhado).
- Formulários validam obrigatórios e limites de um campo antes de enviar, com **mensagem junto ao campo**;
  números enviados como `number` (a API recusa `"15"`).
- Moeda com `formatMoney` (2 casas, vírgula); variação do caixa com `formatSignedMoney` (+/−); datas com
  `formatDateTime` (Brasília); datas digitadas enviadas com `toApiDateTime` (`AAAA-MM-DDTHH:MM-03:00`).
- Query params e corpos em **snake_case**, valores de lista exatamente como na API (`MAL_CONDICIONADO`,
  `value_desc`, `true`).
- Jogadores paginados com `limit=10` e total de `meta.total_items`; jogador sem clube mostra **"Livre"**.

## Convenções de código

- Pastas: `app/` (rotas), `pages/` (uma por rota, só compõem), `features/<dominio>/` (componentes e hooks do
  domínio), `components/ui` e `components/layout` (compartilhados), `services/api/` (HTTP), `hooks/`,
  `types/`, `utils/`, `config/`. Caminhos de rota centralizados em `app/routes.ts`.
- Tabela, campo de formulário e avisos **nunca duplicados** entre telas: sempre os componentes de
  `components/ui`.
- Identificadores em **inglês**, `PascalCase` para componentes, `camelCase` para funções/variáveis; textos da
  interface em português. Campos da API ficam em snake_case nos tipos.
- Exports nomeados; um componente por arquivo; sem código comentado, sem código sem uso, sem `console.log`.
- Classes Tailwind mobile-first; tabelas dentro de `overflow-x-auto` (alvo: 375 px sem rolagem horizontal da
  página).
- Testes junto do código (`*.test.ts(x)`), com Testing Library e `fetch` mockado.

## Git e commits (detalhes em `docs/fluxo-git.md`, idêntico no backend e no frontend)

- Todos os commits direto na `main`, no repositório único da raiz. Um commit nunca mistura `backend/` e
  `frontend/`.
- Conventional Commits em português: `<tipo>(<escopo>): <descrição no imperativo>`, com `Refs: RNxx, Bxx, Fxx`
  quando atender regras ou itens do roteiro. Escopos de domínio: `players`, `clubs`, `market`, `league`;
  técnicos: `shared`, `db` (backend), `ui`, `api`, `layout` (frontend), `infra`, `docs`.
- Mensagens genéricas ("fix", "update", "wip", "ajustes") são proibidas.
- Commits **atômicos**: uma mudança lógica por commit, testes, lint e build verdes em cada um, teste junto do
  código testado, sem misturar refatoração/formatação com funcionalidade.
- Push pelo menos uma vez por dia; nunca `push --force` nem reescrever commits enviados.
- Só commitar ou dar push quando o usuário pedir.

## Contexto da entrega

- Prazo: **23/10/2026 às 20h (Brasília)**. A pasta-pai é a raiz do monorepo, que precisa ter `backend/` e
  `frontend/`; o barema conta commits que alteram `frontend/` e pushes em ≥ 3 dias.
- O README precisa da seção "Uso de inteligência artificial" descrevendo onde e como o Claude foi usado.

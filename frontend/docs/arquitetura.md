# Arquitetura e padrões de código — LigaHub (frontend)

Objetivo: atender ao roteiro da interface (F1) e às rubricas de qualidade (F2) e usabilidade (F3).
Ver [barema-frontend.md](barema-frontend.md).

## 1. Stack e restrições

| Item        | Escolha                                                                   | Observação                                                                                                                                                                                                                                                     |
| ----------- | ------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Projeto     | **Vite** (template `react-ts`)                                            | obrigatório: Vite ou Next.js                                                                                                                                                                                                                                   |
| UI          | **React 19 + TypeScript**                                                 |                                                                                                                                                                                                                                                                |
| Rotas       | **React Router no modo Declarative** (`BrowserRouter`, `Routes`, `Route`) | **Proibido o modo framework**: não instalar `@react-router/dev`, não criar `react-router.config.ts` nem `app/routes.ts` de framework, não usar o plugin `reactRouter()` no Vite, sem loaders/actions/SSR. Remix, CRA, Next, Angular e Vue também são proibidos |
| Estilo      | **Tailwind CSS v4** (`@tailwindcss/vite`)                                 | tokens em `src/index.css` (`@theme`)                                                                                                                                                                                                                           |
| Formulários | **React Hook Form + Zod** (`@hookform/resolvers`)                         | validação antes do envio, mensagem junto ao campo                                                                                                                                                                                                              |
| Estado      | `useState`/`useEffect` em hooks próprios                                  | Context só se necessário; sem biblioteca de estado por enquanto                                                                                                                                                                                                |
| HTTP        | `fetch` encapsulado em `src/services/api/httpClient.ts`                   | nada de `fetch` em componentes                                                                                                                                                                                                                                 |
| Testes      | **Vitest + Testing Library + jsdom**                                      | `npm test` termina sozinho                                                                                                                                                                                                                                     |
| Lint/format | **oxlint** + **Prettier** (com `prettier-plugin-tailwindcss`)             | `npm run lint`, `npm run format:check`                                                                                                                                                                                                                         |

Toda biblioteca complementar precisa constar no README com o motivo do uso.

## 2. Estrutura de pastas

```
src/
├── main.tsx                 # monta <BrowserRouter><AppRoutes/></BrowserRouter>
├── index.css                # Tailwind + tokens de tema
├── app/
│   ├── AppRoutes.tsx        # árvore de rotas (declarativa)
│   └── routes.ts            # caminhos centralizados (nunca escrever '/players' solto)
├── config/env.ts            # variáveis VITE_*
├── components/
│   ├── layout/              # AppLayout (nav), PageHeader
│   └── ui/                  # DataTable, FormField, Button, ConfirmDialog, Alert/Toast, Pagination,
│                            # LoadingState, EmptyState, ErrorState, Badge, Tabs, Modal
├── features/                # por domínio: componentes e hooks específicos
│   ├── players/             # PlayerTable, PlayerForm, PlayerFilters, ConditionForm, PurchaseForm,
│   │                        # usePlayers, playerSchemas.ts
│   ├── clubs/               # ClubTable, ClubForm, SquadTable, SaleDialog, useClub...
│   ├── transactions/        # TransactionTable, TransactionSummary, TransactionFilters
│   └── league/              # LineupForm, MatchForm, DuelList, StandingsTable
├── pages/                   # uma página por rota; compõem features, sem lógica de API
├── services/api/            # httpClient + um módulo por recurso: playersApi, clubsApi,
│                            # transactionsApi, lineupApi, matchesApi, standingsApi
├── types/                   # tipos da API (ver integracao-api.md)
├── hooks/                   # hooks genéricos: useAsync/useApiQuery, useMutation, useDebounce
├── utils/                   # format.ts (moeda, data, toApiDateTime), download.ts
└── test/setup.ts
```

## 3. Camadas (F2 "Acesso à API")

```
pages → features (componentes + hooks) → services/api/<recurso>Api → httpClient → fetch
```

- **Nenhum componente chama `fetch`/HTTP diretamente** — cada componente que fizer isso conta uma ocorrência.
- Os módulos `services/api/*Api.ts` expõem funções tipadas (`listPlayers(filters)`, `createClub(input)`,
  `buyPlayer(id, input)`…), sem estado e sem React.
- Os hooks (`usePlayers`, `useClub`…) guardam `data`, `isLoading`, `error` e expõem `reload()`; mutações expõem
  `isSubmitting` e `submit()` que **ignora novas chamadas enquanto a anterior está pendente** (anti duplo
  clique, além do `disabled` no botão).
- Depois de uma mutação bem-sucedida, chamar `reload()` das consultas afetadas (dados atualizados sem recarregar
  a página).

## 4. Componentes reutilizáveis (F2 "Componentes")

Três coisas **nunca** podem ser duplicadas entre telas — devem vir de componentes compartilhados:

1. **Tabela** com cabeçalho, linhas e os estados de carregamento, vazio e erro → `DataTable` (recebe colunas,
   linhas, `isLoading`, `error`, `emptyMessage`, `onRetry`).
2. **Campo de formulário** com rótulo e mensagem de erro → `FormField` (+ `TextInput`, `NumberInput`, `Select`,
   `DateTimeInput`).
3. **Avisos** de sucesso e erro → `Alert`/`Toast` (um provider de notificações via Context é aceitável).

Usar o mesmo componente com colunas/campos diferentes não é duplicação.

## 5. Formulários

- Um schema Zod por formulário em `features/<dominio>/<x>Schemas.ts`, com mensagens em português.
- Validar no cliente só: obrigatórios, tamanho dos textos (nome do jogador 3–100, nome do clube 3–60, sigla 3
  letras) e faixas de um campo (agilidade 1–100, idade 16–45, atributos/potencial 1–99). Regras cruzadas e
  formato de nome/sigla ficam com a API.
- Inputs numéricos convertidos para `number` antes de enviar (`valueAsNumber`/`z.coerce`), porque a API recusa
  `"15"`.
- Datas: `<input type="datetime-local">` interpretado como Brasília, enviado com `toApiDateTime` (`-03:00`).
- Mensagem de erro **junto ao campo**; o `message` da API num `Alert` no formulário.

## 6. Regras de exibição

- **Nunca** calcular valores de negócio no cliente (lista em `requisitos-frontend.md`). Só formatar.
- Moedas: `formatMoney` / `formatSignedMoney`; datas: `formatDateTime` (fuso `America/Sao_Paulo`).
- Ordem das listas: exatamente a da API.
- Rótulos de enums para exibição em um único mapa (`positionLabels`, `conditionLabels`), mas enviando sempre o
  valor original (`MAL_CONDICIONADO`).

## 7. Clean Code

- Componentes em `PascalCase`, funções/variáveis em `camelCase`, constantes em `UPPER_SNAKE_CASE`, arquivos de
  componente com o nome do componente.
- Identificadores em **inglês** (misturar idiomas perde ponto). Exceções: campos/query params da API em
  snake_case, valores de lista do enunciado (`APTO`, `COMPRA`) e textos exibidos (português).
- Um componente = uma responsabilidade; páginas só compõem.
- Sem código comentado, sem código sem uso (o lint e o `noUnusedLocals` ajudam), sem `console.log`.
- Exports nomeados (sem `export default`), exceto onde ferramenta exigir.

## 8. Responsividade (diferencial F5)

Jogadores, página do clube e histórico **sem rolagem horizontal da página com 375 px**; tabelas rolam dentro do
próprio quadro (`overflow-x-auto` no contêiner da tabela), e todas as ações continuam acessíveis. Usar
classes mobile-first do Tailwind (`sm:`, `lg:`).

## 9. Testes

- Utilitários (`format`, `httpClient`) com testes unitários.
- Componentes com Testing Library: `DataTable` (estados), formulários (validação junto ao campo, envio
  bloqueado durante submit), fluxo de compra com a API mockada (`vi.spyOn(globalThis, 'fetch')`).
- Rotas com `MemoryRouter`.

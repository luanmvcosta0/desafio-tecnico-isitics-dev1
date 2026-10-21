# LigaHub — Frontend

Interface web do estudo de caso SENAI/PE – CÓD. 237 (mercado de jogadores de uma liga fictícia): jogadores,
clubes, compra e venda, histórico de transações, escalação, partidas e classificação.

- URL do frontend: **`http://localhost:5173`**
- Consome a API em `http://localhost:8080/api/v1` (ver o [README do backend](../backend/README.md))

## Pré-requisitos

| Ferramenta | Versão                    | Observação                                                                          |
| ---------- | ------------------------- | ----------------------------------------------------------------------------------- |
| Node.js    | 22 LTS (≥ 22.12.0)        | versão em `.nvmrc`; exigida por Vite 8 e Vitest 5                                   |
| npm        | o que acompanha o Node 22 | instala pelo `package-lock.json` versionado                                         |
| Backend    | —                         | API rodando em `http://localhost:8080` (CORS liberado para `http://localhost:5173`) |

## Instalar e executar (Ubuntu 24.04)

Com a API já rodando (passo a passo no [README do backend](../backend/README.md)), em outro terminal, a
partir da raiz do repositório:

```bash
curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt-get install -y nodejs
cd frontend
cp .env.example .env
npm ci
npm run dev
```

A interface abre em `http://localhost:5173`. O `npm ci` instala exatamente o `package-lock.json` versionado.

Alternativa com Docker para tudo (banco, API e frontend), a partir da raiz: `docker compose up --build -d`
(frontend servido pelo nginx também em `http://localhost:5173`).

## Variáveis de ambiente

Modelo em [`.env.example`](.env.example).

| Variável          | Padrão                  | Uso                                                     |
| ----------------- | ----------------------- | ------------------------------------------------------- |
| `VITE_API_ORIGIN` | `http://localhost:8080` | origem da API, **sem** `/api/v1` (o cliente acrescenta) |

Variáveis `VITE_*` são embutidas no build; no Docker, o valor vem do build arg `VITE_API_ORIGIN`.

## Estrutura de pastas

```
src/
├── main.tsx           # monta <BrowserRouter> com as rotas
├── index.css          # Tailwind e tokens do tema
├── app/               # AppRoutes (rotas declarativas) e routes.ts (caminhos centralizados)
├── config/            # leitura das variáveis VITE_*
├── components/
│   ├── layout/        # AppLayout (navegação) e PageHeader
│   └── ui/            # componentes compartilhados: DataTable, FormField, TextInput, Select, Button,
│                      # Alert, Modal, ConfirmDialog, Pagination, Tabs, SummaryList
├── features/          # componentes, hooks e schemas de cada domínio
│   ├── players/       # tabela, filtros, cadastro, edição e condição
│   ├── clubs/         # lista, criação, página do clube e elenco
│   ├── market/        # compra e venda (formulários e resultados)
│   ├── transactions/  # histórico, filtros, totais e exportação CSV
│   └── league/        # escalação, partida, duelos, histórico de partidas e classificação
├── pages/             # uma página por rota; só compõem as features
├── services/api/      # httpClient e um módulo por recurso (playersApi, clubsApi, marketApi, ...)
├── hooks/             # useApiQuery (leitura) e useMutation (envio sem duplo clique)
├── types/             # tipos das respostas e pedidos da API (snake_case)
├── utils/             # formatação de moeda e datas (Brasília) e download de arquivo
└── test/              # setup do Vitest, mock da API e fixtures
docs/                  # requisitos, integração com a API, arquitetura e infraestrutura
```

Fluxo das camadas: `pages → features → services/api/<recurso>Api → httpClient → fetch`. Nenhum componente
chama `fetch` diretamente, e nenhum valor de negócio é recalculado na tela: a interface só formata o que a API
devolve.

## Bibliotecas

| Biblioteca                                                                                                   | Motivo                                                                                    |
| ------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------- |
| `react` + `react-dom` (19)                                                                                   | biblioteca de interface                                                                   |
| `react-router` (modo Declarative)                                                                            | rotas com URLs reais (`/clubs/1`, `/matches/3`) via `BrowserRouter`/`Routes`/`Route`      |
| `react-hook-form`                                                                                            | estado e envio dos formulários sem re-renderizar a tela inteira                           |
| `zod` + `@hookform/resolvers`                                                                                | schemas de validação dos formulários (obrigatórios e limites) com mensagem junto ao campo |
| `vite` + `@vitejs/plugin-react`                                                                              | servidor de desenvolvimento e build                                                       |
| `typescript`                                                                                                 | tipagem do código e dos contratos da API                                                  |
| `tailwindcss` + `@tailwindcss/vite`                                                                          | estilos utilitários e layout responsivo                                                   |
| `vitest` + `jsdom`                                                                                           | executor de testes com DOM simulado                                                       |
| `@testing-library/react`, `@testing-library/dom`, `@testing-library/user-event`, `@testing-library/jest-dom` | testes de componentes pelo comportamento do usuário                                       |
| `@types/react`, `@types/react-dom`, `@types/node`                                                            | tipos para o TypeScript                                                                   |
| `oxlint`                                                                                                     | lint                                                                                      |
| `prettier` + `prettier-plugin-tailwindcss`                                                                   | formatação (inclusive a ordem das classes do Tailwind)                                    |

## Comandos

| Comando                | O que faz                                              |
| ---------------------- | ------------------------------------------------------ |
| `npm test`             | testes (Vitest), termina sozinho                       |
| `npm run lint`         | lint (oxlint, falha com aviso)                         |
| `npm run format:check` | verifica a formatação (Prettier), sem alterar arquivos |
| `npm run format`       | corrige a formatação                                   |
| `npm run build`        | checagem de tipos e build de produção                  |

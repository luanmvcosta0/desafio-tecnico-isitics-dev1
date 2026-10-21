# Infraestrutura e entrega — LigaHub (frontend)

## Ambiente da banca

Ubuntu 24.04 com Docker, **Node.js 22 LTS** instalado pela banca conforme o README (versão permitida: Node 20
ou 22 LTS). A banca executa literalmente os comandos do README, em até 30 minutos, e abre a URL do frontend no
Chrome (1366 × 768).

## Versões

| Ferramenta   | Versão                                                                      |
| ------------ | --------------------------------------------------------------------------- |
| Node.js      | 22 LTS (`.nvmrc`; `engines.node >= 22.12.0`, exigido por Vite 8 e Vitest 5) |
| npm          | o que acompanha o Node 22                                                   |
| Vite         | 8.x                                                                         |
| React        | 19.x                                                                        |
| React Router | 8.4.x (modo Declarative)                                                    |
| Tailwind CSS | 4.x                                                                         |
| TypeScript   | 7.x                                                                         |

`package-lock.json` **versionado** (a banca roda `npm ci`). Não usar `--legacy-peer-deps`: a instalação precisa
funcionar com `npm ci` puro (por isso `@hookform/resolvers` está na v5).

## Scripts

```bash
npm ci                 # instala exatamente o lockfile
npm run dev            # http://localhost:5173
npm run build          # tsc -b + vite build
npm run preview        # serve o build
npm test               # vitest run (termina sozinho)
npm run lint           # oxlint --deny-warnings
npm run format:check   # prettier --check . (só verifica)
npm run format         # prettier --write . (corrige)
```

## Variáveis de ambiente (`.env.example`)

| Variável          | Padrão                  | Uso                                                     |
| ----------------- | ----------------------- | ------------------------------------------------------- |
| `VITE_API_ORIGIN` | `http://localhost:8080` | origem da API, **sem** `/api/v1` (o cliente acrescenta) |

Variáveis `VITE_*` são embutidas no build. O backend precisa liberar CORS para `http://localhost:5173`.

## Docker (diferencial do backend B5, mas inclui a interface)

- `Dockerfile` multi-stage: `node:22-alpine` (`npm ci` + `npm run build` com `VITE_API_ORIGIN` como build arg)
  → `nginx:alpine` servindo `dist/` com fallback de SPA (`try_files $uri /index.html`), porque o React Router
  usa URLs reais (`/clubs/1`).
- O `docker-compose.yml` fica na raiz do monorepo e sobe `db`, `backend` e `frontend` num comando.

## README (raiz, apontando para `frontend/README.md`)

Itens obrigatórios na parte do frontend: pré-requisitos com versões; passo a passo para instalar e executar
(com a URL do frontend); variáveis de ambiente com `.env.example`; estrutura de pastas e bibliotecas com o
motivo de cada uma. Mais os comandos de testes, lint e formatação.

## Entrega

Prazo: **23/10/2026, 20h (Brasília)**. Um único repositório público com `backend/` e `frontend/` na raiz.

# LigaHub — Frontend

Interface do estudo de caso SENAI/PE – CÓD. 237 (mercado de jogadores de uma liga fictícia).

## Pré-requisitos

- Node.js 22 LTS (com npm)
- API do backend rodando em `http://localhost:8080`

## Executar

```bash
cp .env.example .env
npm ci
npm run dev
# Interface em http://localhost:5173
```

## Comandos

| Comando                | O que faz                                              |
| ---------------------- | ------------------------------------------------------ |
| `npm test`             | testes (Vitest), termina sozinho                       |
| `npm run lint`         | lint (oxlint)                                          |
| `npm run format:check` | verifica a formatação (Prettier), sem alterar arquivos |
| `npm run build`        | checagem de tipos e build de produção                  |

## Variáveis de ambiente

| Variável          | Padrão                  | Uso                          |
| ----------------- | ----------------------- | ---------------------------- |
| `VITE_API_ORIGIN` | `http://localhost:8080` | origem da API, sem `/api/v1` |

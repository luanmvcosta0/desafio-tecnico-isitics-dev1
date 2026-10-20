# CLAUDE.md — LigaHub (monorepo)

Estudo de caso **SENAI/PE – CÓD. 237 Desenvolvedor I (Fullstack)**: mercado de jogadores de uma liga fictícia.
Enunciado: https://github.com/isi-tics/desafio-isi-dev-1. Prazo: **23/10/2026 às 20h (Brasília)**.

Este é o repositório de entrega: um único git na raiz, com as pastas exigidas pelo regulamento.

| Pasta | Stack | Documentação |
| --- | --- | --- |
| `backend/` | Java 21, Spring Boot 3, PostgreSQL 16, Flyway | `backend/CLAUDE.md` e `backend/docs/` |
| `frontend/` | React 19, Vite, TypeScript, Tailwind v4, React Router (modo biblioteca) | `frontend/CLAUDE.md` e `frontend/docs/` |

Ao trabalhar numa parte, ler o `CLAUDE.md` dela antes.

## Git

- Um único repositório, na raiz; todos os commits direto na `main`. Comandos de build/teste rodam dentro de
  cada pasta (`cd backend && ./mvnw test`, `cd frontend && npm test`).
- **Um commit nunca mistura `backend/` e `frontend/`**: o barema conta commits por pasta.
- Conventional Commits e commits atômicos: `backend/docs/fluxo-git.md` (idêntico a
  `frontend/docs/fluxo-git.md`).
- Só commitar ou dar push quando o usuário pedir. Nunca `push --force`.

## Raiz (a criar)

- `docker-compose.yml`: `db` (postgres:16), `backend` e `frontend`, subindo tudo com um comando.
- `README.md`: os 8 itens exigidos pelo enunciado (pode apontar para `backend/README.md` e `frontend/README.md`).

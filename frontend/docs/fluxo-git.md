# Fluxo de Git — branches, commits e PRs

Este documento é **idêntico no backend (`backend/docs/fluxo-git.md`) e no frontend
(`frontend/docs/fluxo-git.md`)**. Ao mudar um, copiar para o outro.

O barema avalia o histórico de cada parte (B4 no backend, F4 no frontend): **≥ 10 commits que alteram a pasta
da parte (`backend/` ou `frontend/`)**, enviados (push) em **≥ 3 dias diferentes**, com **no máximo metade das
mensagens genéricas**. Commits reescritos com push forçado contam só no dia desse push. Merges sem alteração
própria não contam.

## 1. Branches (GitHub Flow)

- **`main`**: sempre compila, passa nos testes e sobe. Nunca recebe commit direto, nunca recebe `push --force`.
- Uma branch curta por funcionalidade, criada a partir da `main` atualizada:

```
<tipo>/<escopo>-<descricao-curta-em-kebab-case>
```

| Tipo        | Uso                                       | Exemplo backend                  | Exemplo frontend                  |
| ----------- | ----------------------------------------- | -------------------------------- | --------------------------------- |
| `feat/`     | nova funcionalidade                       | `feat/players-registration`      | `feat/players-table`              |
| `fix/`      | correção de bug                           | `fix/market-fee-rounding`        | `fix/market-purchase-date-offset` |
| `refactor/` | mudança interna sem alterar comportamento | `refactor/shared-error-handling` | `refactor/ui-data-table`          |
| `test/`     | só testes                                 | `test/market-sale-scenarios`     | `test/players-form-validation`    |
| `docs/`     | só documentação                           | `docs/backend-readme`            | `docs/frontend-readme`            |
| `chore/`    | build, dependências, configuração         | `chore/backend-lint-setup`       | `chore/frontend-lint-setup`       |
| `ci/`       | pipelines                                 | `ci/backend-tests`               | `ci/frontend-tests`               |

### Escopos

| Escopo    | Onde               | O quê                                                      |
| --------- | ------------------ | ---------------------------------------------------------- |
| `players` | backend e frontend | jogadores (cadastro, edição, condição, exclusão, listagem) |
| `clubs`   | backend e frontend | clubes (criação, listagem, página do clube)                |
| `market`  | backend e frontend | compra, venda, histórico de transações, CSV                |
| `league`  | backend e frontend | escalação, partidas, classificação                         |
| `shared`  | backend            | código transversal (erros, JSON, validação, utilitários)   |
| `db`      | backend            | migrations e mapeamento do banco                           |
| `ui`      | frontend           | componentes compartilhados (tabela, campos, avisos)        |
| `api`     | frontend           | cliente HTTP e módulos de acesso à API                     |
| `layout`  | frontend           | layout, navegação e rotas                                  |
| `infra`   | backend e frontend | Docker, lint, formatação, testes, CORS, logs               |
| `docs`    | backend e frontend | documentação e README                                      |

## 2. Commits (Conventional Commits)

```
<tipo>(<escopo>): <descrição no imperativo, minúscula, sem ponto final>

<corpo opcional: o porquê da mudança, regras atendidas>

<rodapé opcional: Refs: RN05, F06 | BREAKING CHANGE: ... | Co-Authored-By: ...>
```

- Tipos: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `build`, `ci`, `style`, `perf`.
- Descrição em português, verbo no presente ("adiciona", "corrige", "extrai"), até ~72 caracteres.
- Escopo da tabela acima.
- Citar no corpo ou rodapé as regras (`RNxx`) e os itens do roteiro (`Bxx`, `Fxx`) atendidos.
- **Proibido** mensagem genérica: nada de `fix`, `update`, `ajustes`, `wip`, `teste`, `commit`, `final`
  sozinhos — o barema conta como genérica.

Exemplos:

```
feat(players): adiciona cálculo do valor de mercado

Aplica desconto por condição ajustado pela agilidade, ajuste por idade,
teto de -40% e piso de 1,00, arredondando só no resultado final.

Refs: RN02, RN03, RN04, RN05, B05
```

```
feat(players): adiciona tabela paginada de jogadores

Mostra overall, valores base e de mercado e clube ("Livre" quando nulo),
com 10 itens por página e total encontrado vindo de meta.total_items.

Refs: F01, RN07
```

```
fix(market): limita a multa por fidelidade a 10,00
```

```
fix(market): envia o fim da fidelidade com o offset -03:00
```

```
chore(db): cria migration inicial com as 7 tabelas do enunciado
```

```
chore(ui): cria DataTable com estados de carregamento, vazio e erro
```

## 3. Commits atômicos

Um commit = **uma mudança lógica completa**, pequena o bastante para ser revisada sozinha e revertida sem
quebrar nada.

- **Fazer:**
  - cada commit compila e mantém os testes, o lint e o build verdes;
  - o teste vai **no mesmo commit** do código que ele testa;
  - separar por camada lógica dentro da funcionalidade quando cada parte faz sentido sozinha:
    - backend: calculadora de domínio + testes → serviço → endpoint;
    - frontend: módulo da API → hook → componente → página;
  - migration de banco e componente compartilhado em commit próprio, antes do commit que os usa;
  - usar `git add -p` para montar o commit só com o que pertence a ele.
- **Não fazer:**
  - misturar refatoração com funcionalidade, ou formatação com lógica;
  - commit "guarda-chuva" com vários módulos (ou backend e frontend juntos);
  - commit que deixa o build quebrado para "corrigir no próximo";
  - código comentado, `System.out`/`console.log` ou arquivos de debug no commit.

### Verificação antes de cada commit

| Parte    | Comandos                                                            |
| -------- | ------------------------------------------------------------------- |
| Backend  | `./mvnw test`, `./mvnw checkstyle:check`, `./mvnw spotless:check`   |
| Frontend | `npm test`, `npm run lint`, `npm run format:check`, `npm run build` |

## 4. Pull Requests

- Um PR por funcionalidade (uma branch), pequeno e focado, aberto contra a `main`. Backend e frontend em PRs
  separados.
- **Título** no formato Conventional Commits: `feat(market): compra de jogadores com contrato de fidelidade`.
- **Corpo** (modelo):

```markdown
## O que muda

Resumo curto da funcionalidade (com print da tela, se for frontend).

## Regras e itens do roteiro atendidos

- RN12, RN13, RN14 · B10, B11 · F06

## Como testar

- Comandos de teste da parte (ver tabela de verificação)
- Passo manual: `curl ...` na API ou caminho na tela (ex.: abrir /players e comprar um jogador livre)

## Checklist

- [ ] Testes cobrindo as tabelas de exemplo do enunciado / o comportamento da tela
- [ ] Testes, lint, formatação e build sem erros
- [ ] Backend: recusas com 400/404/409 + `message`, sem gravar nada; Swagger atualizado
- [ ] Frontend: carregamento, vazio e erro nas listas; sucesso e `message` da API nas ações; botão
      desabilitado durante o envio; nenhum valor de negócio recalculado; testado em 1366 × 768 e 375 px
- [ ] Sem código comentado, sem código morto
- [ ] Docs atualizados
```

- Antes de abrir: `git fetch && git rebase origin/main` (na própria branch, que ainda não foi compartilhada).
- **Merge: "Rebase and merge" ou merge commit (`--no-ff`). Nunca "Squash and merge"** — o squash junta os
  commits atômicos num só e reduz a contagem do barema.
- Apagar a branch depois do merge.

## 5. Ritmo de envio

- `git push` ao fim de cada sessão de trabalho, no mínimo **uma vez por dia** até o prazo (23/10/2026, 20h).
- Pelo menos 3 dias diferentes com push de commits que alteram `backend/` e 3 com commits que alteram
  `frontend/`.

## 6. Roteiro de branches/PRs

Ordem sugerida (segue a ordem dos roteiros da banca). Cada linha é um PR; os commits listados são o mínimo
esperado, cada um atômico.

### Backend

| #   | Branch                               | Commits atômicos                                                                                                                    |
| --- | ------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------- |
| 1   | `chore/shared-api-foundation`        | migration inicial; contrato JSON (Jackson); tratamento central de erros; ids na URL                                                 |
| 2   | `feat/players-registration`          | calculadora de overall; calculadora de valor base; calculadora de valor de mercado; normalização de texto (V4); cadastro e consulta |
| 3   | `feat/players-edit-condition-delete` | edição (`PUT`); alteração de condição (`PATCH`); exclusão lógica (`DELETE`)                                                         |
| 4   | `feat/players-listing`               | parser de query params; comparador alfabético; listagem com filtros, ordenação e paginação                                          |
| 5   | `feat/clubs`                         | criação de clube (nome, sigla, limite 20); consulta com elenco e valores; listagem ordenada                                         |
| 6   | `feat/market-purchase`               | calculadora de taxa e limite; parse de data RN01; compra com contrato e transação                                                   |
| 7   | `feat/market-sale`                   | calculadora de multa; calculadora de comissão; venda                                                                                |
| 8   | `feat/market-history`                | histórico com `summary`; exportação CSV                                                                                             |
| 9   | `feat/league-lineup`                 | formações; escalação (`PUT`/`GET`); saída de titular na venda e ao ficar `DOENTE`                                                   |
| 10  | `feat/league-matches`                | motor da partida; `POST /matches`; histórico de partidas; classificação                                                             |
| 11  | `chore/infra-differentials`          | logs de requisição; CORS; Swagger; lint/formatação; Dockerfile e compose                                                            |
| 12  | `docs/backend-readme`                | README com todos os itens exigidos                                                                                                  |

### Frontend

| #   | Branch                   | Commits atômicos                                                                                                             |
| --- | ------------------------ | ---------------------------------------------------------------------------------------------------------------------------- |
| 1   | `chore/frontend-setup`   | projeto Vite + TS; Tailwind; React Router (modo biblioteca) com layout; httpClient e formatadores; Vitest; oxlint + Prettier |
| 2   | `feat/ui-components`     | DataTable com estados; FormField e inputs; Alert/Toast; ConfirmDialog; Pagination                                            |
| 3   | `feat/players-table`     | tipos e playersApi; hook de listagem; tabela paginada (F01)                                                                  |
| 4   | `feat/players-filters`   | busca; filtros combináveis; ordenação; bloqueio "só livres" + clube (F02)                                                    |
| 5   | `feat/players-forms`     | schemas Zod; cadastro; edição; alteração de condição; exclusão com confirmação (F03, F04)                                    |
| 6   | `feat/clubs`             | clubsApi; listagem com ordenação; criação (F05)                                                                              |
| 7   | `feat/market-purchase`   | formulário de compra com data em Brasília; resultado da compra (F06)                                                         |
| 8   | `feat/club-details`      | página do clube com elenco e contrato; venda com confirmação e resultado (F07, F08)                                          |
| 9   | `feat/market-history`    | tabela do histórico; filtros; summary; exportação CSV (F09–F11)                                                              |
| 10  | `feat/league-lineup`     | aba de escalação por vagas da formação; aviso de incompleta (F12)                                                            |
| 11  | `feat/league-matches`    | nova partida com duelos; histórico com filtro; detalhes; classificação (F13, F14)                                            |
| 12  | `feat/responsive-layout` | ajustes para 375 px (F5)                                                                                                     |
| 13  | `chore/frontend-docker`  | Dockerfile + nginx com fallback de SPA                                                                                       |
| 14  | `docs/frontend-readme`   | README com todos os itens exigidos                                                                                           |

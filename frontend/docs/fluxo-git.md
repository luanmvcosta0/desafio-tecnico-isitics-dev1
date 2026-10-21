# Fluxo de Git — commits

Este documento é **idêntico no backend (`backend/docs/fluxo-git.md`) e no frontend
(`frontend/docs/fluxo-git.md`)**. Ao mudar um, copiar para o outro.

O barema avalia o histórico de cada parte (B4 no backend, F4 no frontend): **≥ 10 commits que alteram a pasta
da parte (`backend/` ou `frontend/`)**, enviados (push) em **≥ 3 dias diferentes**, com **no máximo metade das
mensagens genéricas**. Commits reescritos com push forçado contam só no dia desse push. Merges sem alteração
própria não contam. Só conta o histórico do commit informado na entrega.

## 1. Onde commitar

- Todos os commits vão direto na **`main`**, num único repositório na raiz (com `backend/` e `frontend/`).
- O `git` pode ser usado de qualquer pasta; o que define a parte do commit são os arquivos adicionados
  (`git add backend/...` ou `git add frontend/...`).
- **Um commit nunca mistura `backend/` e `frontend/`**.
- Nunca `push --force` nem reescrever commits já enviados.

## 2. Mensagens (Conventional Commits)

```
<tipo>(<escopo>): <descrição no imperativo, minúscula, sem ponto final>

<corpo opcional: o porquê da mudança, regras atendidas>

<rodapé opcional: Refs: RN05, F06 | BREAKING CHANGE: ... | Co-Authored-By: ...>
```

- Tipos: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `build`, `ci`, `style`, `perf`.
- Descrição em português, verbo no presente ("adiciona", "corrige", "extrai"), até ~72 caracteres.
- Citar no corpo ou rodapé as regras (`RNxx`) e os itens do roteiro (`Bxx`, `Fxx`) atendidos.
- **Proibido** mensagem genérica: nada de `fix`, `update`, `ajustes`, `wip`, `teste`, `commit`, `final`
  sozinhos — o barema conta como genérica.

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

### Exemplos

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

## 4. Ritmo de envio

- `git push` ao fim de cada sessão de trabalho, no mínimo **uma vez por dia** até o prazo (23/10/2026, 20h).
- Pelo menos 3 dias diferentes com push de commits que alteram `backend/` e 3 com commits que alteram
  `frontend/`.
- Na entrega, informar o hash completo (40 caracteres) do último commit da `main`.

## 5. Ordem sugerida de funcionalidades e commits

Segue a ordem dos roteiros da banca. Cada linha é uma funcionalidade; os commits listados são o mínimo
esperado, cada um atômico.

### Backend

| #   | Funcionalidade              | Commits atômicos                                                                                                                    |
| --- | --------------------------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| 1   | Base da API                 | migration inicial; contrato JSON (Jackson); tratamento central de erros; ids na URL                                                 |
| 2   | Cadastro de jogadores       | calculadora de overall; calculadora de valor base; calculadora de valor de mercado; normalização de texto (V4); cadastro e consulta |
| 3   | Edição, condição e exclusão | edição (`PUT`); alteração de condição (`PATCH`); exclusão lógica (`DELETE`)                                                         |
| 4   | Listagem de jogadores       | parser de query params; comparador alfabético; listagem com filtros, ordenação e paginação                                          |
| 5   | Clubes                      | criação de clube (nome, sigla, limite 20); consulta com elenco e valores; listagem ordenada                                         |
| 6   | Compra                      | calculadora de taxa e limite; parse de data RN01; compra com contrato e transação                                                   |
| 7   | Venda                       | calculadora de multa; calculadora de comissão; venda                                                                                |
| 8   | Histórico                   | histórico com `summary`; exportação CSV                                                                                             |
| 9   | Escalação                   | formações; escalação (`PUT`/`GET`); saída de titular na venda e ao ficar `DOENTE`                                                   |
| 10  | Partidas                    | motor da partida; `POST /matches`; histórico de partidas; classificação                                                             |
| 11  | Diferenciais                | logs de requisição; CORS; Swagger; lint/formatação; Dockerfile e compose                                                            |
| 12  | README                      | README com todos os itens exigidos                                                                                                  |

### Frontend

| #   | Funcionalidade             | Commits atômicos                                                                                                              |
| --- | -------------------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| 1   | Base do projeto            | projeto Vite + TS; Tailwind; React Router (modo Declarative) com layout; httpClient e formatadores; Vitest; oxlint + Prettier |
| 2   | Componentes compartilhados | DataTable com estados; FormField e inputs; Alert/Toast; ConfirmDialog; Pagination                                             |
| 3   | Tabela de jogadores        | tipos e playersApi; hook de listagem; tabela paginada (F01)                                                                   |
| 4   | Filtros de jogadores       | busca; filtros combináveis; ordenação; bloqueio "só livres" + clube (F02)                                                     |
| 5   | Formulários de jogadores   | schemas Zod; cadastro; edição; alteração de condição; exclusão com confirmação (F03, F04)                                     |
| 6   | Clubes                     | clubsApi; listagem com ordenação; criação (F05)                                                                               |
| 7   | Compra                     | formulário de compra com data em Brasília; resultado da compra (F06)                                                          |
| 8   | Página do clube e venda    | elenco e contrato; venda com confirmação e resultado (F07, F08)                                                               |
| 9   | Histórico                  | tabela do histórico; filtros; summary; exportação CSV (F09–F11)                                                               |
| 10  | Escalação                  | aba de escalação por vagas da formação; aviso de incompleta (F12)                                                             |
| 11  | Partidas                   | nova partida com duelos; histórico com filtro; detalhes; classificação (F13, F14)                                             |
| 12  | Responsividade             | ajustes para 375 px (F5)                                                                                                      |
| 13  | Docker                     | Dockerfile + nginx com fallback de SPA                                                                                        |
| 14  | README                     | README com todos os itens exigidos                                                                                            |

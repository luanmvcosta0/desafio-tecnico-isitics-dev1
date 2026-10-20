# Contrato da API — LigaHub (backend)

Base URL: **`/api/v1`**. JSON em **snake_case**. Regras referenciadas (RNxx, Vx) estão em
[regras-de-negocio.md](regras-de-negocio.md). O roteiro automático da banca confere código HTTP, corpo e
cabeçalhos exatamente como abaixo; campos adicionais nas respostas são permitidos.

## 1. Endpoints

### Jogadores

| Método & Rota | Descrição | Sucesso | Erros |
| --- | --- | --- | --- |
| `GET /players` | Lista paginada, busca, filtros, ordenação (RN07) | 200 | 400, 404 |
| `GET /players/{id}` | Detalhe | 200 | 404 |
| `POST /players` | Cadastro | **201 + Location** | 400 |
| `PUT /players/{id}` | Edita nome, posição, agilidade, idade (RN08) | 200 | 400, 404, 409 |
| `PATCH /players/{id}/condition` | Altera condição | 200 | 400, 404, 409 |
| `DELETE /players/{id}` | Exclusão lógica de jogador livre (RN09) | **204** sem corpo | 404, 409 |

### Clubes

| Método & Rota | Descrição | Sucesso | Erros |
| --- | --- | --- | --- |
| `GET /clubs` | Lista; `sort` opcional | 200 (array) | 400 |
| `POST /clubs` | Cria (`name`, `acronym`) | **201 + Location** | 400, 409 |
| `GET /clubs/{id}` | Clube com elenco (RN11) | 200 | 404 |

### Mercado

| Método & Rota | Descrição | Sucesso | Erros |
| --- | --- | --- | --- |
| `POST /players/{id}/buy` | Compra jogador livre e cria contrato | 200 | 400, 404, 409 |
| `POST /players/{id}/sell` | Vende pelo clube atual (sem corpo) | 200 | 404, 409 |
| `GET /clubs/{id}/transactions` | Histórico com `summary`; filtros `type`, `player_id` | 200 | 400, 404 |
| `GET /clubs/{id}/transactions/export` | Histórico em CSV (RN18) | 200 `text/csv` | 400, 404 (JSON) |

### Liga

| Método & Rota | Descrição | Sucesso | Erros |
| --- | --- | --- | --- |
| `PUT /clubs/{id}/lineup` | Formação + 5 titulares (RN19) | 200 | 400, 404, 409 |
| `GET /clubs/{id}/lineup` | Escalação atual | 200 | 404 |
| `POST /matches` | Joga e grava a partida (RN20) | **201 + Location** | 400, 404, 409 |
| `GET /matches` | Histórico; filtro `club_id` (RN21) | 200 (array) | 400, 404 |
| `GET /matches/{id}` | Partida com 6 duelos | 200 | 404 |
| `GET /standings` | Classificação de todos os clubes | 200 (array) | — |

`GET /players/{id}`, `GET /clubs/{id}`, `GET /clubs/{id}/lineup`, `GET /matches/{id}` e `GET /standings` não
têm query params.

## 2. Query params

| Endpoint | Parâmetro | Valores aceitos | Padrão |
| --- | --- | --- | --- |
| `GET /players` | `search` | texto de 2 a 100 caracteres (após V4) | — |
| `GET /players` | `position` | `GOL`, `ZAG`, `LAT`, `MEI`, `ATA` | — |
| `GET /players` | `condition` | `APTO`, `MAL_CONDICIONADO`, `DOENTE` | — |
| `GET /players` | `free` | `true` (só livres) / `false` (só com clube) | — |
| `GET /players` | `club_id` | id de clube existente (inexistente → 404) | — |
| `GET /players` | `min_age` / `max_age` | inteiro 16–45, `min_age` ≤ `max_age` | extremos |
| `GET /players` | `min_value` / `max_value` | decimal 0–550, até 2 casas, `min` ≤ `max` (sobre o **valor de mercado**) | extremos |
| `GET /players` | `sort` | `name`, `value_asc`, `value_desc`, `age_asc`, `age_desc`, `position`; até **dois**, separados por vírgula, sem espaços, sem repetir o campo (`value_asc,value_desc` → 400) | `name` |
| `GET /players` | `page` | inteiro ≥ 1 | 1 |
| `GET /players` | `limit` | inteiro 1–50 | 10 |
| `GET /clubs` | `sort` | um só: `name`, `net_worth_asc`, `net_worth_desc` | `name` |
| `.../transactions` e `.../export` | `type` | um ou mais de `COMPRA`, `VENDA`, por vírgula, sem espaços, sem repetição | — |
| `.../transactions` e `.../export` | `player_id` | id de jogador existente (inexistente/excluído → 404) | — |
| `GET /matches` | `club_id` | id de clube existente (inexistente → 404) | — |

Regras gerais dos query params:
- Filtros combináveis (o resultado atende a todos; o `summary` respeita todos).
- Ausente → não filtra / usa padrão. Presente com valor vazio (`position=`) → **400**. A correção não envia
  parâmetro repetido.
- Inteiros: só positivos, só dígitos, sem sinal, sem zeros à esquerda, sem ponto, sem expoente → regex
  `^[1-9][0-9]*$`; outra forma → 400.
- Decimais: só `10`, `10.5`, `10.55` (sem sinal, expoente, vírgula ou zeros à esquerda; `0.5` aceito) → regex
  `^(0|[1-9][0-9]*)(\.[0-9]{1,2})?$`.
- Em faixas, vindo só um limite, o outro assume o extremo.
- Parâmetro com nome fora da tabela → ignorado.
- Valor fora do aceito (inclusive caixa trocada: `position=mei`, `type=COMPRA,venda`, `sort=POSITION`) → 400.
- Contraditórios → 400: `min_age > max_age`, `min_value > max_value`, `free=true` com `club_id`.
  `free=false` com `club_id` é aceito.
- Com dois critérios em `sort`, o segundo desempata o primeiro; depois, nome e id.

## 3. Formatos

### Jogador (`PlayerResponse`) — usado em `GET /players/{id}`, itens de `GET /players`, `POST`, `PUT`, `PATCH`

```json
{
  "id": 2, "name": "Meia Lua", "position": "MEI", "agility": 85, "age": 25,
  "condition": "MAL_CONDICIONADO", "base_value": 40.00, "market_value": 37.00,
  "pace": 70, "shooting": 68, "passing": 82, "dribbling": 79, "defending": 55, "physical": 64,
  "potential": 86, "overall": 75, "club_id": null
}
```

### `POST /players`

```json
{ "name": "Meia Lua", "position": "MEI", "agility": 85, "age": 25,
  "pace": 70, "shooting": 68, "passing": 82, "dribbling": 79, "defending": 55, "physical": 64, "potential": 86 }
```
→ `201`, `Location: /api/v1/players/2`, corpo `PlayerResponse` com `condition: "APTO"` e `club_id: null`.

### `PUT /players/{id}`
```json
{ "name": "Davi Melo", "position": "MEI", "agility": 70, "age": 23 }
```
→ `200` com `PlayerResponse`. Os quatro campos são obrigatórios.

### `PATCH /players/{id}/condition`
```json
{ "condition": "MAL_CONDICIONADO" }
```
→ `200` com `PlayerResponse`. Condição atual → 200 sem mudança. `DOENTE` → `APTO` → 409.

### `GET /players`
```json
{
  "data": [ { "...": "PlayerResponse" } ],
  "meta": { "page": 1, "limit": 10, "total_items": 1, "total_pages": 1 }
}
```

### Clube

`POST /clubs` com `{ "name": "Águias FC", "acronym": "AGU" }` → `201`, `Location: /api/v1/clubs/1`:
```json
{ "id": 1, "name": "Águias FC", "acronym": "AGU", "cash": 300.00, "squad_value": 0.00, "net_worth": 300.00 }
```
`GET /clubs` → array desses objetos (sem elenco). `GET /clubs/{id}` → mesmo objeto + `players`, cada um
`PlayerResponse` + `contract`:
```json
{
  "id": 1, "name": "Águias FC", "acronym": "AGU", "cash": 261.15, "squad_value": 37.00, "net_worth": 298.15,
  "players": [
    { "id": 2, "...": "PlayerResponse", "club_id": 1,
      "contract": { "purchase_value": 37.00, "loyalty_until": "2027-03-02T10:15:00-03:00",
                    "remaining_days": 120, "penalty_rate": 10 } }
  ]
}
```

### `POST /players/{id}/buy`
```json
{ "club_id": 1, "loyalty_until": "2027-03-02T10:15-03:00" }
```
→ `200`:
```json
{
  "id": 10, "club_id": 1, "player_id": 2, "player_name": "Meia Lua", "type": "COMPRA",
  "value": 37.00, "fee": 1.85, "penalty": 0.00, "profit": null, "cash_change": -38.85,
  "created_at": "2026-11-02T10:15:00-03:00",
  "contract": { "purchase_value": 37.00, "loyalty_until": "2027-03-02T10:15:00-03:00", "status": "ATIVO" },
  "club_cash": 261.15
}
```
Erros: 400 (`club_id`/`loyalty_until` ausentes ou inválidos; formato fora da RN01; fidelidade não futura ou
> 1.095 dias); 404 (jogador inexistente/excluído; clube inexistente); 409 (jogador com clube; custo > 50% do
caixa; elenco com 12; recompra pelo clube que já vendeu).

### `POST /players/{id}/sell` (sem corpo)
→ `200`:
```json
{
  "id": 11, "club_id": 1, "player_id": 2, "player_name": "Meia Lua", "type": "VENDA",
  "value": 40.00, "fee": 0.30, "penalty": 4.00, "profit": 3.00, "cash_change": 35.70,
  "created_at": "2026-11-05T16:40:00-03:00",
  "purchase_value": 37.00, "remaining_days": 117, "penalty_rate": 10,
  "contract_status": "RESCINDIDO", "club_cash": 296.85
}
```
Erros: 404 (jogador inexistente/excluído); 409 (jogador livre).

### `GET /clubs/{id}/transactions`
```json
{
  "data": [
    { "id": 11, "club_id": 1, "player_id": 2, "player_name": "Meia Lua", "type": "VENDA",
      "value": 40.00, "fee": 0.30, "penalty": 4.00, "profit": 3.00, "cash_change": 35.70,
      "created_at": "2026-11-05T16:40:00-03:00" }
  ],
  "summary": { "total_spent": 0.00, "total_received": 35.70, "total_fees": 0.30,
               "total_penalties": 4.00, "total_profit": 3.00 }
}
```

### `GET /clubs/{id}/transactions/export`
`200`, `Content-Type: text/csv` (com ou sem charset), corpo conforme RN18.

### `PUT /clubs/{id}/lineup`
```json
{ "formation": "2-1-1", "player_ids": [34, 31, 30, 33, 32] }
```
→ `200` (mesmo formato de `GET /clubs/{id}/lineup`):
```json
{ "club_id": 1, "formation": "2-1-1", "complete": true,
  "players": [ { "...": "PlayerResponse completo, na ordem do elenco (RN11)" } ] }
```
Sem escalação: `{ "club_id": 1, "formation": null, "complete": false, "players": [] }`.
Erros: 400 (`formation` ausente/fora da lista; `player_ids` ausente, não lista, tamanho ≠ 5, repetidos, não
positivos); 404 (clube ou jogador inexistente/excluído); 409 (fora do elenco; `DOENTE`; posições que não fecham
a formação).

### `POST /matches`
```json
{ "home_club_id": 1, "away_club_id": 2 }
```
→ `201`, `Location: /api/v1/matches/1`:
```json
{
  "id": 1, "played_at": "2026-11-12T15:00:00-03:00",
  "home": { "club_id": 1, "club_name": "Águias FC", "formation": "2-1-1", "goals": 1 },
  "away": { "club_id": 2, "club_name": "Delta FC", "formation": "2-1-1", "goals": 2 },
  "result": "VISITANTE",
  "duels": [
    { "order": 1, "attacking_club_id": 1,
      "attacker": { "player_id": 34, "player_name": "Otto Rangel", "position": "ATA", "condition": "APTO",
                    "attribute": "shooting", "value": 75 },
      "defender": { "player_id": 40, "player_name": "Ivo Teles", "position": "GOL", "condition": "APTO",
                    "attribute": "defending", "value": 72 },
      "goal": true }
  ]
}
```
`duels` sempre com 6 itens (`order` 1–6). `GET /matches/{id}` → mesmo formato. `GET /matches` → array no mesmo
formato **sem `duels`**, mais recente primeiro.
Erros: 400 (ids ausentes/inválidos; iguais); 404 (clube inexistente); 409 (escalação incompleta).

### `GET /standings`
```json
[
  { "position": 1, "club_id": 2, "club_name": "Delta FC", "played": 1, "wins": 1, "draws": 0, "losses": 0,
    "goals_for": 2, "goals_against": 1, "goal_difference": 1, "points": 3 }
]
```

### Erro (qualquer recusa ou falha)
```json
{ "message": "O custo da compra (50,40) passa do limite de 50% do caixa (50,00)." }
```
`Content-Type: application/json`, sem stack trace. Texto livre, nunca vazio. Campos extras permitidos.

## 4. Códigos de erro globais

| Código | Quando |
| --- | --- |
| 400 | Dado/parâmetro inválido (V1–V6, V9, limites) |
| 404 | Caminho fora do enunciado, recurso/id inexistente (V8). Método não previsto pode ser 404 ou 405 |
| 409 | Regra de negócio violada |
| 500 | Falha inesperada; operação desfeita por inteiro |

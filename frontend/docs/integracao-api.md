# Integração com a API — LigaHub

A especificação completa da API está no backend (`../backend/docs/contrato-api.md` e
`../backend/docs/regras-de-negocio.md`). Aqui fica o que o frontend precisa para consumir a API.

- Base: `${VITE_API_ORIGIN}/api/v1` (padrão `http://localhost:8080/api/v1`), montada em `src/config/env.ts`.
- JSON em **snake_case** nos dois sentidos. Os tipos TypeScript espelham os nomes da API
  (`base_value`, `market_value`, `club_id`…) — o barema não conta esses nomes como fora do padrão.
- Erros: corpo `{ "message": string }`; o `httpClient` converte em `ApiError(status, message)`.
  Falha de rede vira `ApiError(0, ...)`.
- Moedas chegam como número JSON (ex.: `41.44`) e só são **formatadas** (`formatMoney`).
- Datas chegam como `AAAA-MM-DDTHH:MM:SS-03:00`; enviar como `AAAA-MM-DDTHH:MM-03:00` (`toApiDateTime`).

## Endpoints usados por tela

| Tela            | Chamadas                                                                                                                                                                                                                                                                                                                |
| --------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Jogadores       | `GET /players` (query: `search`, `position`, `condition`, `free`, `club_id`, `min_age`, `max_age`, `min_value`, `max_value`, `sort`, `page`, `limit=10`); `POST /players`; `PUT /players/{id}`; `PATCH /players/{id}/condition`; `DELETE /players/{id}` (204); `POST /players/{id}/buy`; `GET /clubs` (opções de clube) |
| Clubes          | `GET /clubs?sort=name\|net_worth_asc\|net_worth_desc`; `POST /clubs`                                                                                                                                                                                                                                                    |
| Página do clube | `GET /clubs/{id}`; `POST /players/{id}/sell`; `GET /clubs/{id}/transactions?type=&player_id=`; `GET /clubs/{id}/transactions/export?type=&player_id=` (CSV); `GET /clubs/{id}/lineup`; `PUT /clubs/{id}/lineup`                                                                                                         |
| Partidas        | `GET /clubs`; `POST /matches`; `GET /matches?club_id=`; `GET /matches/{id}`                                                                                                                                                                                                                                             |
| Classificação   | `GET /standings`                                                                                                                                                                                                                                                                                                        |

## Tipos principais (resumo)

```ts
type Position = 'GOL' | 'ZAG' | 'LAT' | 'MEI' | 'ATA'
type Condition = 'APTO' | 'MAL_CONDICIONADO' | 'DOENTE'
type Formation = '3-1' | '2-2' | '2-1-1' | '1-2-1'
type TransactionType = 'COMPRA' | 'VENDA'
type MatchResult = 'MANDANTE' | 'VISITANTE' | 'EMPATE'
type PlayerSort = 'name' | 'value_asc' | 'value_desc' | 'age_asc' | 'age_desc' | 'position'

interface Player {
  id: number
  name: string
  position: Position
  agility: number
  age: number
  condition: Condition
  base_value: number
  market_value: number
  pace: number
  shooting: number
  passing: number
  dribbling: number
  defending: number
  physical: number
  potential: number
  overall: number
  club_id: number | null
}

interface PaginatedPlayers {
  data: Player[]
  meta: { page: number; limit: number; total_items: number; total_pages: number }
}

interface Club {
  id: number
  name: string
  acronym: string
  cash: number
  squad_value: number
  net_worth: number
}

interface SquadPlayer extends Player {
  contract: {
    purchase_value: number
    loyalty_until: string
    remaining_days: number
    penalty_rate: number
  }
}
interface ClubDetails extends Club {
  players: SquadPlayer[]
}

interface Transaction {
  id: number
  club_id: number
  player_id: number
  player_name: string
  type: TransactionType
  value: number
  fee: number
  penalty: number
  profit: number | null
  cash_change: number
  created_at: string
}
interface TransactionHistory {
  data: Transaction[]
  summary: {
    total_spent: number
    total_received: number
    total_fees: number
    total_penalties: number
    total_profit: number
  }
}

interface PurchaseResult extends Transaction {
  contract: { purchase_value: number; loyalty_until: string; status: 'ATIVO' }
  club_cash: number
}
interface SaleResult extends Transaction {
  purchase_value: number
  remaining_days: number
  penalty_rate: number
  contract_status: 'ENCERRADO' | 'RESCINDIDO'
  club_cash: number
}

interface Lineup {
  club_id: number
  formation: Formation | null
  complete: boolean
  players: Player[]
}

interface DuelPlayer {
  player_id: number
  player_name: string
  position: Position
  condition: Condition
  attribute: 'shooting' | 'passing' | 'dribbling' | 'defending'
  value: number
}
interface Duel {
  order: number
  attacking_club_id: number
  attacker: DuelPlayer
  defender: DuelPlayer
  goal: boolean
}
interface MatchSide {
  club_id: number
  club_name: string
  formation: Formation
  goals: number
}
interface Match {
  id: number
  played_at: string
  home: MatchSide
  away: MatchSide
  result: MatchResult
  duels?: Duel[]
}

interface Standing {
  position: number
  club_id: number
  club_name: string
  played: number
  wins: number
  draws: number
  losses: number
  goals_for: number
  goals_against: number
  goal_difference: number
  points: number
}
```

## Formações e vagas (para montar o formulário de escalação)

| `formation` | Vagas além do goleiro              |
| ----------- | ---------------------------------- |
| `3-1`       | 3 defesa (ZAG/LAT), 1 ataque (ATA) |
| `2-2`       | 2 defesa, 2 ataque                 |
| `2-1-1`     | 2 defesa, 1 meio (MEI), 1 ataque   |
| `1-2-1`     | 1 defesa, 2 meio, 1 ataque         |

O formulário oferece em cada vaga só jogadores do elenco, da posição do setor e sem `DOENTE`, e envia
`{ formation, player_ids: [5 ids] }`.

## CSV

`requestBlob` baixa o arquivo; criar um `URL.createObjectURL(blob)` e disparar o download com um `<a download>`
(ex.: `historico-clube-1.csv`). Erros do export continuam em JSON e viram `ApiError`.

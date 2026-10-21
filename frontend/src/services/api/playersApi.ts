import type {
  Condition,
  PaginatedPlayers,
  Player,
  PlayerCreateInput,
  PlayerListParams,
  PlayerUpdateInput,
} from '../../types/player'
import { requestJson, requestWithoutContent } from './httpClient'

const PLAYERS_PATH = '/players'

export function listPlayers(params: PlayerListParams): Promise<PaginatedPlayers> {
  return requestJson<PaginatedPlayers>(PLAYERS_PATH, { query: { ...params } })
}

export function createPlayer(input: PlayerCreateInput): Promise<Player> {
  return requestJson<Player>(PLAYERS_PATH, { method: 'POST', body: input })
}

export function updatePlayer(id: number, input: PlayerUpdateInput): Promise<Player> {
  return requestJson<Player>(`${PLAYERS_PATH}/${id}`, { method: 'PUT', body: input })
}

export function changePlayerCondition(id: number, condition: Condition): Promise<Player> {
  return requestJson<Player>(`${PLAYERS_PATH}/${id}/condition`, {
    method: 'PATCH',
    body: { condition },
  })
}

export function deletePlayer(id: number): Promise<void> {
  return requestWithoutContent(`${PLAYERS_PATH}/${id}`, { method: 'DELETE' })
}

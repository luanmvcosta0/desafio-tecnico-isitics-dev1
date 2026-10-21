import type { Player, PlayerCreateInput } from '../../types/player'
import { requestJson } from './httpClient'

const PLAYERS_PATH = '/players'

export function createPlayer(input: PlayerCreateInput): Promise<Player> {
  return requestJson<Player>(PLAYERS_PATH, { method: 'POST', body: input })
}

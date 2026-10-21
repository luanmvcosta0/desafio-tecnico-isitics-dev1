import type { PaginatedPlayers, Player } from '../types/player'

export function aPlayer(overrides: Partial<Player> = {}): Player {
  return {
    id: 2,
    name: 'Meia Lua',
    position: 'MEI',
    agility: 85,
    age: 25,
    condition: 'APTO',
    base_value: 40,
    market_value: 40,
    pace: 70,
    shooting: 68,
    passing: 82,
    dribbling: 79,
    defending: 55,
    physical: 64,
    potential: 86,
    overall: 75,
    club_id: null,
    ...overrides,
  }
}

export function aPage(players: Player[], page = 1, totalItems = players.length): PaginatedPlayers {
  return {
    data: players,
    meta: { page, limit: 10, total_items: totalItems, total_pages: Math.ceil(totalItems / 10) },
  }
}

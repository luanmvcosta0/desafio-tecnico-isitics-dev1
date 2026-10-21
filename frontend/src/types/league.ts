import type { Player } from './player'

export const FORMATIONS = ['3-1', '2-2', '2-1-1', '1-2-1'] as const
export type Formation = (typeof FORMATIONS)[number]

export interface Lineup {
  club_id: number
  formation: Formation | null
  complete: boolean
  players: Player[]
}

export interface LineupInput {
  formation: Formation
  player_ids: number[]
}

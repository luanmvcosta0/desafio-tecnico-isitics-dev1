import type { Player } from './player'

export type ClubSort = 'name' | 'net_worth_desc' | 'net_worth_asc'

export interface Club {
  id: number
  name: string
  acronym: string
  cash: number
  squad_value: number
  net_worth: number
}

export interface ClubCreateInput {
  name: string
  acronym: string
}

export interface SquadPlayer extends Player {
  contract: {
    purchase_value: number
    loyalty_until: string
    remaining_days: number
    penalty_rate: number
  } | null
}

export interface ClubDetails extends Club {
  players: SquadPlayer[]
}

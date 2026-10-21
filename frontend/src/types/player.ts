export const POSITIONS = ['GOL', 'ZAG', 'LAT', 'MEI', 'ATA'] as const
export type Position = (typeof POSITIONS)[number]

export const CONDITIONS = ['APTO', 'MAL_CONDICIONADO', 'DOENTE'] as const
export type Condition = (typeof CONDITIONS)[number]

export interface Player {
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

export interface PlayerCreateInput {
  name: string
  position: Position
  agility: number
  age: number
  pace: number
  shooting: number
  passing: number
  dribbling: number
  defending: number
  physical: number
  potential: number
}

export interface PlayerUpdateInput {
  name: string
  position: Position
  agility: number
  age: number
}

export interface PaginatedPlayers {
  data: Player[]
  meta: { page: number; limit: number; total_items: number; total_pages: number }
}

export type PlayerSort = 'name' | 'value_asc' | 'value_desc' | 'age_asc' | 'age_desc' | 'position'

/** Filters typed on the screen, sent as written: the API validates formats and ranges. */
export interface PlayerFilters {
  search: string
  position: Position | ''
  condition: Condition | ''
  free: boolean
  club_id: string
  min_age: string
  max_age: string
  min_value: string
  max_value: string
  sort: PlayerSort
}

export interface PlayerListParams extends PlayerFilters {
  page: number
  limit: number
}

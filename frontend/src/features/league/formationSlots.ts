import type { Formation } from '../../types/league'
import type { Position } from '../../types/player'

export interface LineupSlot {
  label: string
  positions: Position[]
}

const GOALKEEPER: LineupSlot = { label: 'Goleiro', positions: ['GOL'] }
const DEFENSE: Position[] = ['ZAG', 'LAT']

function sector(label: string, positions: Position[], count: number): LineupSlot[] {
  return Array.from({ length: count }, (_, index) => ({
    label: count > 1 ? `${label} ${index + 1}` : label,
    positions,
  }))
}

function slots(defenders: number, midfielders: number, attackers: number): LineupSlot[] {
  return [
    GOALKEEPER,
    ...sector('Defesa (ZAG/LAT)', DEFENSE, defenders),
    ...sector('Meio (MEI)', ['MEI'], midfielders),
    ...sector('Ataque (ATA)', ['ATA'], attackers),
  ]
}

/** The five vacancies of each formation: the goalkeeper and the outfield sectors (RN19). */
export const formationSlots: Record<Formation, LineupSlot[]> = {
  '3-1': slots(3, 0, 1),
  '2-2': slots(2, 0, 2),
  '2-1-1': slots(2, 1, 1),
  '1-2-1': slots(1, 2, 1),
}

import type { Lineup, LineupInput } from '../../types/league'
import { requestJson } from './httpClient'

function lineupPath(clubId: string): string {
  return `/clubs/${encodeURIComponent(clubId)}/lineup`
}

export function getLineup(clubId: string): Promise<Lineup> {
  return requestJson<Lineup>(lineupPath(clubId))
}

export function saveLineup(clubId: string, input: LineupInput): Promise<Lineup> {
  return requestJson<Lineup>(lineupPath(clubId), { method: 'PUT', body: input })
}

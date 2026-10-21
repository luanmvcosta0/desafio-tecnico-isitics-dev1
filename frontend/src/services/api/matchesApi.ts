import type { Match, MatchInput } from '../../types/league'
import { requestJson } from './httpClient'

const MATCHES_PATH = '/matches'

export function playMatch(input: MatchInput): Promise<Match> {
  return requestJson<Match>(MATCHES_PATH, { method: 'POST', body: input })
}

export function listMatches(clubId: string): Promise<Match[]> {
  return requestJson<Match[]>(MATCHES_PATH, { query: { club_id: clubId } })
}

export function getMatch(matchId: string): Promise<Match> {
  return requestJson<Match>(`${MATCHES_PATH}/${encodeURIComponent(matchId)}`)
}

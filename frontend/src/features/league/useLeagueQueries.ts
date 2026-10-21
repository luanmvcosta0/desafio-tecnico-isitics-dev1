import { useCallback } from 'react'
import { useApiQuery } from '../../hooks/useApiQuery'
import { getMatch, listMatches } from '../../services/api/matchesApi'
import { getStandings } from '../../services/api/standingsApi'

export function useMatches(clubId: string) {
  const load = useCallback(() => listMatches(clubId), [clubId])
  return useApiQuery(load)
}

export function useMatch(matchId: string) {
  const load = useCallback(() => getMatch(matchId), [matchId])
  return useApiQuery(load)
}

export function useStandings() {
  return useApiQuery(getStandings)
}

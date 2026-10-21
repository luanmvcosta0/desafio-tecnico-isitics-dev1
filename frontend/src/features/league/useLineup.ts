import { useCallback } from 'react'
import { useApiQuery } from '../../hooks/useApiQuery'
import { getLineup } from '../../services/api/lineupApi'

export function useLineup(clubId: string) {
  const load = useCallback(() => getLineup(clubId), [clubId])
  return useApiQuery(load)
}

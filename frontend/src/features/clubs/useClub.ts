import { useCallback } from 'react'
import { useApiQuery } from '../../hooks/useApiQuery'
import { getClub } from '../../services/api/clubsApi'

export function useClub(clubId: string) {
  const load = useCallback(() => getClub(clubId), [clubId])
  return useApiQuery(load)
}

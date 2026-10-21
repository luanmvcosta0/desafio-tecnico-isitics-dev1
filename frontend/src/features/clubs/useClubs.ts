import { useCallback } from 'react'
import { useApiQuery } from '../../hooks/useApiQuery'
import { listClubs } from '../../services/api/clubsApi'
import type { ClubSort } from '../../types/club'

export function useClubs(sort: ClubSort = 'name') {
  const load = useCallback(() => listClubs(sort), [sort])
  return useApiQuery(load)
}

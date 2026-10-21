import { useCallback } from 'react'
import { useApiQuery } from '../../hooks/useApiQuery'
import { listPlayers } from '../../services/api/playersApi'

export const PLAYERS_PAGE_SIZE = 10

export function usePlayers(page: number) {
  const load = useCallback(() => listPlayers({ page, limit: PLAYERS_PAGE_SIZE }), [page])
  return useApiQuery(load)
}

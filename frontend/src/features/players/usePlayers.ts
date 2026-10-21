import { useCallback } from 'react'
import { useApiQuery } from '../../hooks/useApiQuery'
import { listPlayers } from '../../services/api/playersApi'
import type { PlayerFilters } from '../../types/player'

export const PLAYERS_PAGE_SIZE = 10

export function usePlayers(page: number, filters: PlayerFilters) {
  const load = useCallback(
    () => listPlayers({ ...filters, page, limit: PLAYERS_PAGE_SIZE }),
    [page, filters],
  )
  return useApiQuery(load)
}

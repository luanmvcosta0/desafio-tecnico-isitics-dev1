import { useCallback } from 'react'
import { useApiQuery } from '../../hooks/useApiQuery'
import { getTransactionHistory } from '../../services/api/transactionsApi'
import type { TransactionFilters, TransactionHistory } from '../../types/market'

export const NO_TRANSACTION_FILTERS: TransactionFilters = { types: [], player_id: '' }

export interface PlayerOption {
  id: number
  name: string
}

interface HistoryWithPlayers {
  history: TransactionHistory
  players: PlayerOption[]
}

function playersIn(history: TransactionHistory): PlayerOption[] {
  const players = new Map(history.data.map((item) => [item.player_id, item.player_name]))
  return [...players].map(([id, name]) => ({ id, name }))
}

function isFiltered(filters: TransactionFilters): boolean {
  return filters.types.length > 0 || filters.player_id !== ''
}

/**
 * Filtered history of the club plus the players that appear in its whole history (the choices of
 * the player filter). The unfiltered history is only requested again when a filter is active.
 */
export function useTransactionHistory(clubId: string, filters: TransactionFilters) {
  const load = useCallback(async (): Promise<HistoryWithPlayers> => {
    const [history, wholeHistory] = await Promise.all([
      getTransactionHistory(clubId, filters),
      isFiltered(filters) ? getTransactionHistory(clubId, NO_TRANSACTION_FILTERS) : null,
    ])
    return { history, players: playersIn(wholeHistory ?? history) }
  }, [clubId, filters])
  return useApiQuery(load)
}

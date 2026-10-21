import type { TransactionFilters, TransactionHistory } from '../../types/market'
import { requestBlob, requestJson, type QueryParams } from './httpClient'

function historyPath(clubId: string): string {
  return `/clubs/${encodeURIComponent(clubId)}/transactions`
}

function toQuery({ types, player_id }: TransactionFilters): QueryParams {
  return { type: types.join(','), player_id }
}

export function getTransactionHistory(
  clubId: string,
  filters: TransactionFilters,
): Promise<TransactionHistory> {
  return requestJson<TransactionHistory>(historyPath(clubId), { query: toQuery(filters) })
}

/** The CSV of the API with the same filters of the table; errors come back as JSON. */
export function exportTransactions(clubId: string, filters: TransactionFilters): Promise<Blob> {
  return requestBlob(`${historyPath(clubId)}/export`, { query: toQuery(filters) })
}

import type { PurchaseInput, PurchaseResult, SaleResult } from '../../types/market'
import { requestJson } from './httpClient'

export function buyPlayer(playerId: number, input: PurchaseInput): Promise<PurchaseResult> {
  return requestJson<PurchaseResult>(`/players/${playerId}/buy`, { method: 'POST', body: input })
}

export function sellPlayer(playerId: number): Promise<SaleResult> {
  return requestJson<SaleResult>(`/players/${playerId}/sell`, { method: 'POST' })
}

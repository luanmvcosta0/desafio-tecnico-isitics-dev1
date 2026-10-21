import type { PurchaseInput, PurchaseResult } from '../../types/market'
import { requestJson } from './httpClient'

export function buyPlayer(playerId: number, input: PurchaseInput): Promise<PurchaseResult> {
  return requestJson<PurchaseResult>(`/players/${playerId}/buy`, { method: 'POST', body: input })
}

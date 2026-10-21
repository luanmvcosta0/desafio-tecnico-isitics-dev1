export type TransactionType = 'COMPRA' | 'VENDA'

export interface Transaction {
  id: number
  club_id: number
  player_id: number
  player_name: string
  type: TransactionType
  value: number
  fee: number
  penalty: number
  profit: number | null
  cash_change: number
  created_at: string
}

export interface PurchaseInput {
  club_id: number
  loyalty_until: string
}

export interface PurchaseResult extends Transaction {
  contract: { purchase_value: number; loyalty_until: string; status: 'ATIVO' }
  club_cash: number
}

export interface SaleResult extends Transaction {
  purchase_value: number
  remaining_days: number
  penalty_rate: number
  contract_status: 'ENCERRADO' | 'RESCINDIDO'
  club_cash: number
}

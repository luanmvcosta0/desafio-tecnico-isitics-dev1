import { SummaryList } from '../../components/ui/SummaryList'
import type { SaleResult } from '../../types/market'
import { formatMoney, formatSignedMoney } from '../../utils/format'

interface SaleSummaryProps {
  result: SaleResult
}

/** Sale values exactly as returned by the API, only formatted. */
export function SaleSummary({ result }: SaleSummaryProps) {
  const items = [
    { label: 'Valor de compra', value: formatMoney(result.purchase_value) },
    { label: 'Valor de venda', value: formatMoney(result.value) },
    { label: 'Dias restantes', value: String(result.remaining_days) },
    { label: `Multa (${result.penalty_rate}%)`, value: formatMoney(result.penalty) },
    { label: 'Lucro', value: formatSignedMoney(result.profit ?? 0) },
    { label: 'Comissão', value: formatMoney(result.fee) },
    { label: 'Crédito', value: formatMoney(result.cash_change) },
    { label: 'Novo caixa', value: formatMoney(result.club_cash) },
  ]

  return <SummaryList label="Resultado da venda" items={items} columnsClassName="grid-cols-2" />
}

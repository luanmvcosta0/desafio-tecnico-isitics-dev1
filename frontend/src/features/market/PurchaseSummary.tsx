import { SummaryList } from '../../components/ui/SummaryList'
import type { PurchaseResult } from '../../types/market'
import { formatDateTime, formatMoney } from '../../utils/format'

interface PurchaseSummaryProps {
  result: PurchaseResult
}

/** Values returned by the API; the total cost (value + fee) is the only sum made on screen. */
export function PurchaseSummary({ result }: PurchaseSummaryProps) {
  const items = [
    { label: 'Valor', value: formatMoney(result.value) },
    { label: 'Taxa', value: formatMoney(result.fee) },
    { label: 'Custo total', value: formatMoney(result.value + result.fee) },
    { label: 'Fim da fidelidade', value: formatDateTime(result.contract.loyalty_until) },
    { label: 'Novo caixa', value: formatMoney(result.club_cash) },
  ]

  return <SummaryList label="Resultado da compra" items={items} columnsClassName="grid-cols-2" />
}

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

  return (
    <dl aria-label="Resultado da compra" className="grid grid-cols-2 gap-3">
      {items.map((item) => (
        <div key={item.label}>
          <dt className="text-xs text-slate-500">{item.label}</dt>
          <dd className="font-medium">{item.value}</dd>
        </div>
      ))}
    </dl>
  )
}

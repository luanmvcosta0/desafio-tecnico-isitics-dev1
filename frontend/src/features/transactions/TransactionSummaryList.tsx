import { SummaryList } from '../../components/ui/SummaryList'
import type { TransactionSummary } from '../../types/market'
import { formatMoney, formatSignedMoney } from '../../utils/format'

interface TransactionSummaryListProps {
  summary: TransactionSummary
}

/** Totals of the API for the filtered history, shown below the table. */
export function TransactionSummaryList({ summary }: TransactionSummaryListProps) {
  return (
    <SummaryList
      label="Totais do histórico"
      columnsClassName="grid-cols-2 sm:grid-cols-3 lg:grid-cols-5"
      items={[
        { label: 'Total pago em compras', value: formatMoney(summary.total_spent) },
        { label: 'Total recebido em vendas', value: formatMoney(summary.total_received) },
        { label: 'Total de taxas e comissões', value: formatMoney(summary.total_fees) },
        { label: 'Total de multas', value: formatMoney(summary.total_penalties) },
        { label: 'Lucro total', value: formatSignedMoney(summary.total_profit) },
      ]}
    />
  )
}

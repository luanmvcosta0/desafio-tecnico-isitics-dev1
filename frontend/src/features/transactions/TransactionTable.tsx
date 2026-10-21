import { DataTable, type Column } from '../../components/ui/DataTable'
import type { Transaction } from '../../types/market'
import { formatDateTime, formatMoney, formatSignedMoney } from '../../utils/format'
import { transactionTypeLabels } from './transactionLabels'

const NO_PROFIT = '—'

const columns: Column<Transaction>[] = [
  { header: 'Data e hora', render: (item) => formatDateTime(item.created_at) },
  { header: 'Tipo', render: (item) => transactionTypeLabels[item.type] },
  { header: 'Jogador', render: (item) => item.player_name },
  { header: 'Valor', render: (item) => formatMoney(item.value) },
  { header: 'Taxa ou comissão', render: (item) => formatMoney(item.fee) },
  { header: 'Multa', render: (item) => formatMoney(item.penalty) },
  {
    header: 'Lucro',
    render: (item) => (item.profit === null ? NO_PROFIT : formatSignedMoney(item.profit)),
  },
  {
    header: 'Variação do caixa',
    render: (item) => (
      <span className={item.cash_change < 0 ? 'text-red-700' : 'text-emerald-700'}>
        {formatSignedMoney(item.cash_change)}
      </span>
    ),
  },
]

interface TransactionTableProps {
  transactions: Transaction[] | null
  isLoading: boolean
  error: string | null
  onRetry: () => void
}

export function TransactionTable({
  transactions,
  isLoading,
  error,
  onRetry,
}: TransactionTableProps) {
  return (
    <DataTable
      caption="Histórico de transações"
      columns={columns}
      rows={transactions}
      getRowKey={(item) => item.id}
      isLoading={isLoading}
      error={error}
      emptyMessage="Nenhuma transação encontrada."
      onRetry={onRetry}
    />
  )
}

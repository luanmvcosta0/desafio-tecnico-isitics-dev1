import { useCallback, useState } from 'react'
import { Alert } from '../../components/ui/Alert'
import { Button } from '../../components/ui/Button'
import { useMutation } from '../../hooks/useMutation'
import { exportTransactions } from '../../services/api/transactionsApi'
import type { TransactionFilters } from '../../types/market'
import { downloadBlob } from '../../utils/download'
import { TransactionFilterBar } from './TransactionFilterBar'
import { TransactionSummaryList } from './TransactionSummaryList'
import { TransactionTable } from './TransactionTable'
import { NO_TRANSACTION_FILTERS, useTransactionHistory } from './useTransactionHistory'

interface TransactionHistorySectionProps {
  clubId: string
}

export function TransactionHistorySection({ clubId }: TransactionHistorySectionProps) {
  const [filters, setFilters] = useState<TransactionFilters>(NO_TRANSACTION_FILTERS)
  const query = useTransactionHistory(clubId, filters)
  const exportCsv = useCallback(
    async (current: TransactionFilters) =>
      downloadBlob(await exportTransactions(clubId, current), `historico-clube-${clubId}.csv`),
    [clubId],
  )
  const csv = useMutation(exportCsv)

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <TransactionFilterBar
          filters={filters}
          players={query.data?.players ?? []}
          onChange={setFilters}
        />
        <Button variant="secondary" onClick={() => csv.submit(filters)} disabled={csv.isSubmitting}>
          {csv.isSubmitting ? 'Exportando…' : 'Exportar CSV'}
        </Button>
      </div>
      {csv.errorMessage && <Alert variant="error">{csv.errorMessage}</Alert>}
      <TransactionTable
        transactions={query.data?.history.data ?? null}
        isLoading={query.isLoading}
        error={query.error}
        onRetry={query.reload}
      />
      {query.data && <TransactionSummaryList summary={query.data.history.summary} />}
    </div>
  )
}

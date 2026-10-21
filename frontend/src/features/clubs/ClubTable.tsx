import { DataTable, type Column } from '../../components/ui/DataTable'
import type { Club } from '../../types/club'
import { formatMoney } from '../../utils/format'

interface ClubTableProps {
  clubs: Club[] | null
  isLoading: boolean
  error: string | null
  onRetry: () => void
}

const columns: Column<Club>[] = [
  { header: 'Nome', render: (club) => <span className="font-medium">{club.name}</span> },
  { header: 'Sigla', render: (club) => club.acronym },
  { header: 'Caixa', render: (club) => formatMoney(club.cash) },
  { header: 'Valor do elenco', render: (club) => formatMoney(club.squad_value) },
  { header: 'Patrimônio', render: (club) => formatMoney(club.net_worth) },
]

export function ClubTable({ clubs, isLoading, error, onRetry }: ClubTableProps) {
  return (
    <DataTable
      caption="Clubes"
      columns={columns}
      rows={clubs}
      getRowKey={(club) => club.id}
      isLoading={isLoading}
      error={error}
      emptyMessage="Nenhum clube cadastrado."
      onRetry={onRetry}
    />
  )
}

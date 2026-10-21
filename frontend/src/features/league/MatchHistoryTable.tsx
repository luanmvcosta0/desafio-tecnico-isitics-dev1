import { Link } from 'react-router'
import { matchDetailsPath } from '../../app/routes'
import { DataTable, type Column } from '../../components/ui/DataTable'
import type { Match } from '../../types/league'
import { formatDateTime } from '../../utils/format'

const columns: Column<Match>[] = [
  { header: 'Data e hora', render: (match) => formatDateTime(match.played_at) },
  { header: 'Mandante', render: (match) => match.home.club_name },
  {
    header: 'Placar',
    render: (match) => (
      <span className="font-semibold">
        {match.home.goals} × {match.away.goals}
      </span>
    ),
  },
  { header: 'Visitante', render: (match) => match.away.club_name },
  {
    header: 'Duelos',
    render: (match) => (
      <Link to={matchDetailsPath(match.id)} className="text-brand-700 font-medium hover:underline">
        Ver duelos
      </Link>
    ),
  },
]

interface MatchHistoryTableProps {
  matches: Match[] | null
  isLoading: boolean
  error: string | null
  onRetry: () => void
}

export function MatchHistoryTable({ matches, isLoading, error, onRetry }: MatchHistoryTableProps) {
  return (
    <DataTable
      caption="Histórico de partidas"
      columns={columns}
      rows={matches}
      getRowKey={(match) => match.id}
      isLoading={isLoading}
      error={error}
      emptyMessage="Nenhuma partida jogada."
      onRetry={onRetry}
    />
  )
}

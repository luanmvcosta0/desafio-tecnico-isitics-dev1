import { DataTable, type Column } from '../../components/ui/DataTable'
import type { Standing } from '../../types/league'

function signed(value: number): string {
  return value > 0 ? `+${value}` : String(value)
}

const columns: Column<Standing>[] = [
  { header: 'Posição', render: (row) => `${row.position}º` },
  { header: 'Clube', render: (row) => <span className="font-medium">{row.club_name}</span> },
  { header: 'Jogos', render: (row) => row.played },
  { header: 'Vitórias', render: (row) => row.wins },
  { header: 'Empates', render: (row) => row.draws },
  { header: 'Derrotas', render: (row) => row.losses },
  { header: 'Gols marcados', render: (row) => row.goals_for },
  { header: 'Gols sofridos', render: (row) => row.goals_against },
  { header: 'Saldo', render: (row) => signed(row.goal_difference) },
  { header: 'Pontos', render: (row) => <span className="font-semibold">{row.points}</span> },
]

interface StandingsTableProps {
  standings: Standing[] | null
  isLoading: boolean
  error: string | null
  onRetry: () => void
}

export function StandingsTable({ standings, isLoading, error, onRetry }: StandingsTableProps) {
  return (
    <DataTable
      caption="Classificação"
      columns={columns}
      rows={standings}
      getRowKey={(row) => row.club_id}
      isLoading={isLoading}
      error={error}
      emptyMessage="Nenhum clube cadastrado."
      onRetry={onRetry}
    />
  )
}

import { Button } from '../../components/ui/Button'
import { DataTable, type Column } from '../../components/ui/DataTable'
import type { SquadPlayer } from '../../types/club'
import { formatDateTime, formatMoney } from '../../utils/format'
import { conditionLabels } from '../players/playerLabels'
import { PositionCode } from '../players/PositionCode'

const NO_CONTRACT = '—'

interface SquadTableProps {
  players: SquadPlayer[] | null
  isLoading: boolean
  error: string | null
  onRetry: () => void
  onSell: (player: SquadPlayer) => void
}

export function SquadTable({ players, isLoading, error, onRetry, onSell }: SquadTableProps) {
  const columns: Column<SquadPlayer>[] = [
    { header: 'Nome', render: (player) => <span className="font-medium">{player.name}</span> },
    { header: 'Posição', render: (player) => <PositionCode position={player.position} /> },
    { header: 'Overall', render: (player) => player.overall },
    { header: 'Condição', render: (player) => conditionLabels[player.condition] },
    { header: 'Valor de mercado', render: (player) => formatMoney(player.market_value) },
    {
      header: 'Fim da fidelidade',
      render: (player) =>
        player.contract ? formatDateTime(player.contract.loyalty_until) : NO_CONTRACT,
    },
    {
      header: 'Dias restantes',
      render: (player) => player.contract?.remaining_days ?? NO_CONTRACT,
    },
    {
      header: '% multa',
      render: (player) => (player.contract ? `${player.contract.penalty_rate}%` : NO_CONTRACT),
    },
    {
      header: 'Ações',
      render: (player) => (
        <Button
          variant="secondary"
          onClick={() => onSell(player)}
          aria-label={`Vender ${player.name}`}
        >
          Vender
        </Button>
      ),
    },
  ]

  return (
    <DataTable
      caption="Elenco"
      columns={columns}
      rows={players}
      getRowKey={(player) => player.id}
      isLoading={isLoading}
      error={error}
      emptyMessage="O clube ainda não tem jogadores. Compre jogadores livres na tela de Jogadores."
      onRetry={onRetry}
    />
  )
}

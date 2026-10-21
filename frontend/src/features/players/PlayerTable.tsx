import { Button } from '../../components/ui/Button'
import { DataTable, type Column } from '../../components/ui/DataTable'
import type { Player } from '../../types/player'
import { formatMoney } from '../../utils/format'
import { conditionLabels, FREE_PLAYER_LABEL, positionLabels } from './playerLabels'

interface PlayerTableProps {
  players: Player[] | null
  clubAcronyms: Map<number, string>
  isLoading: boolean
  error: string | null
  onRetry: () => void
  onEdit: (player: Player) => void
  onChangeCondition: (player: Player) => void
  onDelete: (player: Player) => void
  onBuy: (player: Player) => void
}

export function PlayerTable({
  players,
  clubAcronyms,
  isLoading,
  error,
  onRetry,
  onEdit,
  onChangeCondition,
  onDelete,
  onBuy,
}: PlayerTableProps) {
  const columns: Column<Player>[] = [
    { header: 'Nome', render: (player) => <span className="font-medium">{player.name}</span> },
    {
      header: 'Posição',
      render: (player) => <abbr title={positionLabels[player.position]}>{player.position}</abbr>,
    },
    { header: 'Overall', render: (player) => player.overall },
    { header: 'Agilidade', render: (player) => player.agility },
    { header: 'Idade', render: (player) => player.age },
    { header: 'Condição', render: (player) => conditionLabels[player.condition] },
    { header: 'Valor base', render: (player) => formatMoney(player.base_value) },
    {
      header: 'Valor de mercado',
      render: (player) => formatMoney(player.market_value),
    },
    {
      header: 'Clube',
      render: (player) =>
        player.club_id === null
          ? FREE_PLAYER_LABEL
          : (clubAcronyms.get(player.club_id) ?? `#${player.club_id}`),
    },
    {
      header: 'Ações',
      render: (player) => (
        <div className="flex justify-center gap-1">
          <Button
            variant="secondary"
            onClick={() => onEdit(player)}
            aria-label={`Editar ${player.name}`}
          >
            Editar
          </Button>
          <Button
            variant="secondary"
            onClick={() => onChangeCondition(player)}
            aria-label={`Alterar condição de ${player.name}`}
          >
            Condição
          </Button>
          {player.club_id === null && (
            <Button onClick={() => onBuy(player)} aria-label={`Comprar ${player.name}`}>
              Comprar
            </Button>
          )}
          {player.club_id === null && (
            <Button
              variant="secondary"
              onClick={() => onDelete(player)}
              aria-label={`Excluir ${player.name}`}
            >
              Excluir
            </Button>
          )}
        </div>
      ),
    },
  ]

  return (
    <DataTable
      caption="Jogadores"
      columns={columns}
      rows={players}
      getRowKey={(player) => player.id}
      isLoading={isLoading}
      error={error}
      emptyMessage="Nenhum jogador encontrado."
      onRetry={onRetry}
    />
  )
}

import type { Player } from '../../types/player'
import { formatMoney } from '../../utils/format'
import { conditionLabels, FREE_PLAYER_LABEL, positionLabels } from './playerLabels'

interface PlayerSummaryProps {
  player: Player
}

/** Values computed by the API for a player, only formatted for display. */
export function PlayerSummary({ player }: PlayerSummaryProps) {
  const items = [
    { label: 'Posição', value: positionLabels[player.position] },
    { label: 'Overall', value: String(player.overall) },
    { label: 'Valor base', value: formatMoney(player.base_value) },
    { label: 'Valor de mercado', value: formatMoney(player.market_value) },
    { label: 'Condição', value: conditionLabels[player.condition] },
    { label: 'Clube', value: player.club_id === null ? FREE_PLAYER_LABEL : `#${player.club_id}` },
  ]

  return (
    <section
      aria-label={`Resumo de ${player.name}`}
      className="rounded-xl border border-slate-200 p-4"
    >
      <h2 className="font-semibold">{player.name}</h2>
      <dl className="mt-3 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
        {items.map((item) => (
          <div key={item.label}>
            <dt className="text-xs text-slate-500">{item.label}</dt>
            <dd className="font-medium">{item.value}</dd>
          </div>
        ))}
      </dl>
    </section>
  )
}

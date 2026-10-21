import { DataTable, type Column } from '../../components/ui/DataTable'
import type { Duel, DuelPlayer, Match } from '../../types/league'
import { conditionLabels } from '../players/playerLabels'
import { duelAttributeLabels } from './leagueLabels'

function describe(player: DuelPlayer): string {
  return `${player.player_name} (${player.position}, ${conditionLabels[player.condition]})`
}

function valueOf(player: DuelPlayer): string {
  return `${duelAttributeLabels[player.attribute]} ${player.value}`
}

interface DuelTableProps {
  match: Match
}

/** The six duels returned by the API, never recalculated (RN20). */
export function DuelTable({ match }: DuelTableProps) {
  const clubNames = new Map([
    [match.home.club_id, match.home.club_name],
    [match.away.club_id, match.away.club_name],
  ])
  const columns: Column<Duel>[] = [
    { header: 'Duelo', render: (duel) => duel.order },
    { header: 'Ataque de', render: (duel) => clubNames.get(duel.attacking_club_id) },
    { header: 'Atacante', render: (duel) => describe(duel.attacker) },
    { header: 'Ataque', render: (duel) => valueOf(duel.attacker) },
    { header: 'Defensor', render: (duel) => describe(duel.defender) },
    { header: 'Defesa', render: (duel) => valueOf(duel.defender) },
    {
      header: 'Gol',
      render: (duel) => (
        <span className={duel.goal ? 'font-semibold text-emerald-700' : 'text-slate-500'}>
          {duel.goal ? 'Gol' : 'Não'}
        </span>
      ),
    },
  ]

  return (
    <DataTable
      caption="Duelos da partida"
      columns={columns}
      rows={match.duels ?? []}
      getRowKey={(duel) => duel.order}
      isLoading={false}
      error={null}
      emptyMessage="Partida sem duelos."
    />
  )
}

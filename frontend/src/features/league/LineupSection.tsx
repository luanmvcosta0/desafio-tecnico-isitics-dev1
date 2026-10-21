import { useState } from 'react'
import { Alert } from '../../components/ui/Alert'
import { DataTable, type Column } from '../../components/ui/DataTable'
import type { Lineup } from '../../types/league'
import type { Player } from '../../types/player'
import { conditionLabels, positionLabels } from '../players/playerLabels'
import { LineupForm } from './LineupForm'
import { useLineup } from './useLineup'

const columns: Column<Player>[] = [
  {
    header: 'Posição',
    render: (player) => <abbr title={positionLabels[player.position]}>{player.position}</abbr>,
  },
  { header: 'Titular', render: (player) => <span className="font-medium">{player.name}</span> },
  { header: 'Overall', render: (player) => player.overall },
  { header: 'Condição', render: (player) => conditionLabels[player.condition] },
]

interface LineupSectionProps {
  clubId: string
  squad: Player[]
}

/** Lineup returned by the API, the warning when incomplete and the form to save a new one. */
export function LineupSection({ clubId, squad }: LineupSectionProps) {
  const query = useLineup(clubId)
  const [notice, setNotice] = useState<string | null>(null)
  const lineup = query.data

  function handleSaved(saved: Lineup) {
    setNotice(`Escalação ${saved.formation} salva.`)
    query.reload()
  }

  return (
    <div className="flex flex-col gap-4">
      {notice && <Alert variant="success">{notice}</Alert>}
      {lineup && !lineup.complete && (
        <Alert variant="warning">
          {lineup.formation
            ? `Escalação ${lineup.formation} incompleta: o clube não pode jogar até escalar os 5 titulares.`
            : 'O clube ainda não tem escalação e não pode jogar partidas.'}
        </Alert>
      )}
      <h3 className="font-semibold">
        Titulares{lineup?.formation ? ` · formação ${lineup.formation}` : ''}
      </h3>
      <DataTable
        caption="Titulares"
        columns={columns}
        rows={lineup?.players ?? null}
        getRowKey={(player) => player.id}
        isLoading={query.isLoading}
        error={query.error}
        emptyMessage="Nenhum titular escalado."
        onRetry={query.reload}
      />
      {lineup && (
        <section className="rounded-xl border border-slate-200 p-4">
          <h3 className="mb-3 font-semibold">Nova escalação</h3>
          <LineupForm
            key={JSON.stringify(lineup)}
            clubId={clubId}
            squad={squad}
            lineup={lineup}
            onSaved={handleSaved}
          />
        </section>
      )}
    </div>
  )
}

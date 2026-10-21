import { useState } from 'react'
import { PageHeader } from '../components/layout/PageHeader'
import { FormField } from '../components/ui/FormField'
import { Select } from '../components/ui/Select'
import { useClubs } from '../features/clubs/useClubs'
import { DuelTable } from '../features/league/DuelTable'
import { MatchForm } from '../features/league/MatchForm'
import { MatchHistoryTable } from '../features/league/MatchHistoryTable'
import { MatchScore } from '../features/league/MatchScore'
import { useMatches } from '../features/league/useLeagueQueries'
import type { Match } from '../types/league'

export function MatchesPage() {
  const clubs = useClubs()
  const [clubFilter, setClubFilter] = useState('')
  const matches = useMatches(clubFilter)
  const [played, setPlayed] = useState<Match | null>(null)
  const clubOptions = (clubs.data ?? []).map((club) => ({
    value: String(club.id),
    label: club.name,
  }))

  function handlePlayed(match: Match) {
    setPlayed(match)
    matches.reload()
  }

  return (
    <>
      <PageHeader title="Partidas" description="Jogue partidas e consulte o histórico." />
      <div className="flex flex-col gap-6">
        <section className="rounded-xl border border-slate-200 p-4 sm:p-6">
          <h2 className="mb-4 text-lg font-semibold">Nova partida</h2>
          <MatchForm clubs={clubs.data ?? []} onPlayed={handlePlayed} />
        </section>
        {played && (
          <section aria-label="Resultado da partida" className="flex flex-col gap-3">
            <MatchScore match={played} />
            <DuelTable match={played} />
          </section>
        )}
        <section className="flex flex-col gap-3">
          <div className="flex flex-wrap items-end justify-between gap-4">
            <h2 className="text-lg font-semibold">Histórico de partidas</h2>
            <div className="w-full sm:w-64">
              <FormField label="Filtrar por clube" htmlFor="matches-club">
                <Select
                  id="matches-club"
                  placeholder="Todos"
                  options={clubOptions}
                  value={clubFilter}
                  onChange={(event) => setClubFilter(event.target.value)}
                />
              </FormField>
            </div>
          </div>
          <MatchHistoryTable
            matches={matches.data}
            isLoading={matches.isLoading}
            error={matches.error}
            onRetry={matches.reload}
          />
        </section>
      </div>
    </>
  )
}

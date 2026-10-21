import { PageHeader } from '../components/layout/PageHeader'
import { StandingsTable } from '../features/league/StandingsTable'
import { useStandings } from '../features/league/useLeagueQueries'

export function StandingsPage() {
  const standings = useStandings()

  return (
    <>
      <PageHeader title="Classificação" description="Pontos, saldo e gols de todos os clubes." />
      <StandingsTable
        standings={standings.data}
        isLoading={standings.isLoading}
        error={standings.error}
        onRetry={standings.reload}
      />
    </>
  )
}

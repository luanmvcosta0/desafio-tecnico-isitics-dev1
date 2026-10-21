import { Link, useParams } from 'react-router'
import { routes } from '../app/routes'
import { PageHeader } from '../components/layout/PageHeader'
import { Alert } from '../components/ui/Alert'
import { DuelTable } from '../features/league/DuelTable'
import { MatchScore } from '../features/league/MatchScore'
import { useMatch } from '../features/league/useLeagueQueries'

export function MatchDetailsPage() {
  const { matchId = '' } = useParams()
  const match = useMatch(matchId)

  return (
    <>
      <PageHeader
        title={`Partida #${matchId}`}
        description="Placar e os 6 duelos da partida."
        actions={
          <Link to={routes.matches} className="text-brand-600 text-sm font-medium hover:underline">
            Voltar às partidas
          </Link>
        }
      />
      {match.isLoading && <p role="status">Carregando…</p>}
      {match.error && <Alert variant="error">{match.error}</Alert>}
      {match.data && (
        <div className="flex flex-col gap-3">
          <MatchScore match={match.data} />
          <DuelTable match={match.data} />
        </div>
      )}
    </>
  )
}

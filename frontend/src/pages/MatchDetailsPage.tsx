import { useParams } from 'react-router'
import { PageHeader } from '../components/layout/PageHeader'

export function MatchDetailsPage() {
  const { matchId } = useParams()
  return <PageHeader title={`Partida #${matchId}`} description="Placar e os 6 duelos da partida." />
}

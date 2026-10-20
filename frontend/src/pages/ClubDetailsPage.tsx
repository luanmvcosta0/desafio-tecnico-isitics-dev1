import { useParams } from 'react-router'
import { PageHeader } from '../components/layout/PageHeader'

export function ClubDetailsPage() {
  const { clubId } = useParams()
  return (
    <PageHeader
      title={`Clube #${clubId}`}
      description="Elenco, contratos, histórico de transações e escalação."
    />
  )
}

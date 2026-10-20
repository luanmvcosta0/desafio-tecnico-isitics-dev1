import { Link } from 'react-router'
import { routes } from '../app/routes'
import { PageHeader } from '../components/layout/PageHeader'

export function NotFoundPage() {
  return (
    <PageHeader
      title="Página não encontrada"
      description="O endereço acessado não existe."
      actions={
        <Link to={routes.home} className="text-brand-600 text-sm font-medium hover:underline">
          Voltar ao início
        </Link>
      }
    />
  )
}

import { useState } from 'react'
import { Link, useParams } from 'react-router'
import { routes } from '../app/routes'
import { PageHeader } from '../components/layout/PageHeader'
import { SummaryList } from '../components/ui/SummaryList'
import { SquadTable } from '../features/clubs/SquadTable'
import { useClub } from '../features/clubs/useClub'
import { SaleDialog } from '../features/market/SaleDialog'
import type { SquadPlayer } from '../types/club'
import { formatMoney } from '../utils/format'

export function ClubDetailsPage() {
  const { clubId = '' } = useParams()
  const club = useClub(clubId)
  const [selling, setSelling] = useState<SquadPlayer | null>(null)

  return (
    <>
      <PageHeader
        title={club.data ? `${club.data.name} (${club.data.acronym})` : 'Clube'}
        description="Caixa, elenco com contratos e venda de jogadores."
        actions={
          <Link to={routes.clubs} className="text-brand-600 text-sm font-medium hover:underline">
            Voltar aos clubes
          </Link>
        }
      />
      <div className="flex flex-col gap-4">
        {club.data && (
          <section className="rounded-xl border border-slate-200 p-4">
            <SummaryList
              label="Valores do clube"
              items={[
                { label: 'Caixa', value: formatMoney(club.data.cash) },
                { label: 'Valor do elenco', value: formatMoney(club.data.squad_value) },
                { label: 'Patrimônio', value: formatMoney(club.data.net_worth) },
              ]}
            />
          </section>
        )}
        <h2 className="text-lg font-semibold">Elenco</h2>
        <SquadTable
          players={club.data?.players ?? null}
          isLoading={club.isLoading}
          error={club.error}
          onRetry={club.reload}
          onSell={setSelling}
        />
      </div>
      {selling && (
        <SaleDialog player={selling} onSold={club.reload} onClose={() => setSelling(null)} />
      )}
    </>
  )
}

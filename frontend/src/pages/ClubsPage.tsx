import { useState } from 'react'
import { PageHeader } from '../components/layout/PageHeader'
import { Alert } from '../components/ui/Alert'
import { Button } from '../components/ui/Button'
import { FormField } from '../components/ui/FormField'
import { Select } from '../components/ui/Select'
import { ClubCreateForm } from '../features/clubs/ClubCreateForm'
import { ClubTable } from '../features/clubs/ClubTable'
import { useClubs } from '../features/clubs/useClubs'
import type { Club, ClubSort } from '../types/club'

const sortOptions: { value: ClubSort; label: string }[] = [
  { value: 'name', label: 'Nome (A–Z)' },
  { value: 'net_worth_desc', label: 'Patrimônio (maior primeiro)' },
  { value: 'net_worth_asc', label: 'Patrimônio (menor primeiro)' },
]

export function ClubsPage() {
  const [sort, setSort] = useState<ClubSort>('name')
  const [isCreating, setIsCreating] = useState(false)
  const [notice, setNotice] = useState<string | null>(null)
  const clubs = useClubs(sort)

  function handleCreated(club: Club) {
    setIsCreating(false)
    setNotice(`Clube ${club.name} criado com caixa de 300,00.`)
    clubs.reload()
  }

  return (
    <>
      <PageHeader
        title="Clubes"
        description="Clubes da liga, com caixa, valor do elenco e patrimônio."
        actions={
          !isCreating && (
            <Button
              onClick={() => {
                setNotice(null)
                setIsCreating(true)
              }}
            >
              Novo clube
            </Button>
          )
        }
      />
      <div className="flex flex-col gap-4">
        {isCreating && (
          <section className="rounded-xl border border-slate-200 p-4 sm:p-6">
            <h2 className="mb-4 text-lg font-semibold">Novo clube</h2>
            <ClubCreateForm onCreated={handleCreated} onCancel={() => setIsCreating(false)} />
          </section>
        )}
        {notice && <Alert variant="success">{notice}</Alert>}
        <div className="w-full sm:w-72">
          <FormField label="Ordenar por" htmlFor="club-sort">
            <Select
              id="club-sort"
              value={sort}
              options={sortOptions}
              onChange={(event) => setSort(event.target.value as ClubSort)}
            />
          </FormField>
        </div>
        <ClubTable
          clubs={clubs.data}
          isLoading={clubs.isLoading}
          error={clubs.error}
          onRetry={clubs.reload}
        />
      </div>
    </>
  )
}

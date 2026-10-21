import { useState } from 'react'
import { PageHeader } from '../components/layout/PageHeader'
import { Alert } from '../components/ui/Alert'
import { Button } from '../components/ui/Button'
import { PlayerCreateForm } from '../features/players/PlayerCreateForm'
import { PlayerSummary } from '../features/players/PlayerSummary'
import type { Player } from '../types/player'

export function PlayersPage() {
  const [isCreating, setIsCreating] = useState(false)
  const [createdPlayer, setCreatedPlayer] = useState<Player | null>(null)

  function handleCreated(player: Player) {
    setCreatedPlayer(player)
    setIsCreating(false)
  }

  function openForm() {
    setCreatedPlayer(null)
    setIsCreating(true)
  }

  return (
    <>
      <PageHeader
        title="Jogadores"
        description="Cadastro, busca, filtros e condição física dos jogadores."
        actions={!isCreating && <Button onClick={openForm}>Novo jogador</Button>}
      />
      <div className="flex flex-col gap-4">
        {isCreating && (
          <section className="rounded-xl border border-slate-200 p-4 sm:p-6">
            <h2 className="mb-4 text-lg font-semibold">Novo jogador</h2>
            <PlayerCreateForm onCreated={handleCreated} onCancel={() => setIsCreating(false)} />
          </section>
        )}
        {createdPlayer && (
          <>
            <Alert variant="success">Jogador {createdPlayer.name} cadastrado com sucesso.</Alert>
            <PlayerSummary player={createdPlayer} />
          </>
        )}
      </div>
    </>
  )
}

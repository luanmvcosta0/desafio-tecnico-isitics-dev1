import { useMemo, useState } from 'react'
import { PageHeader } from '../components/layout/PageHeader'
import { Alert } from '../components/ui/Alert'
import { Button } from '../components/ui/Button'
import { ConfirmDialog } from '../components/ui/ConfirmDialog'
import { Modal } from '../components/ui/Modal'
import { Pagination } from '../components/ui/Pagination'
import { useClubs } from '../features/clubs/useClubs'
import { PurchaseDialog } from '../features/market/PurchaseDialog'
import { ConditionForm } from '../features/players/ConditionForm'
import { PlayerCreateForm } from '../features/players/PlayerCreateForm'
import { PlayerEditForm } from '../features/players/PlayerEditForm'
import { PlayerFilterForm } from '../features/players/PlayerFilterForm'
import { EMPTY_PLAYER_FILTERS } from '../features/players/playerFilterDefaults'
import { PlayerSummary } from '../features/players/PlayerSummary'
import { PlayerTable } from '../features/players/PlayerTable'
import { usePlayers } from '../features/players/usePlayers'
import { useMutation } from '../hooks/useMutation'
import { deletePlayer } from '../services/api/playersApi'
import type { Player, PlayerFilters } from '../types/player'

type DialogType = 'edit' | 'condition' | 'delete' | 'buy'
type Dialog = { type: DialogType; player: Player } | null

export function PlayersPage() {
  const [page, setPage] = useState(1)
  const [isCreating, setIsCreating] = useState(false)
  const [createdPlayer, setCreatedPlayer] = useState<Player | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [dialog, setDialog] = useState<Dialog>(null)
  const [filters, setFilters] = useState<PlayerFilters>(EMPTY_PLAYER_FILTERS)
  const players = usePlayers(page, filters)
  const clubs = useClubs()
  const clubAcronyms = useMemo(
    () => new Map((clubs.data ?? []).map((club) => [club.id, club.acronym])),
    [clubs.data],
  )
  const removal = useMutation(deletePlayer, {
    onSuccess: () => {
      finishAction(`Jogador ${dialog?.player.name} excluído.`)
      const isLastOfPage = players.data?.data.length === 1 && page > 1
      if (isLastOfPage) {
        setPage(page - 1)
      }
    },
  })

  function finishAction(message: string) {
    setDialog(null)
    setCreatedPlayer(null)
    setNotice(message)
    players.reload()
  }

  function handleCreated(player: Player) {
    setIsCreating(false)
    finishAction(`Jogador ${player.name} cadastrado com sucesso.`)
    setCreatedPlayer(player)
  }

  function openDialog(type: DialogType, player: Player) {
    removal.reset()
    setNotice(null)
    setDialog({ type, player })
  }

  function applyFilters(next: PlayerFilters) {
    setPage(1)
    setFilters(next)
  }

  const closeDialog = () => setDialog(null)
  const totalItems = players.data?.meta.total_items

  return (
    <>
      <PageHeader
        title="Jogadores"
        description="Cadastro, busca, filtros e condição física dos jogadores."
        actions={
          !isCreating && (
            <Button
              onClick={() => {
                setNotice(null)
                setCreatedPlayer(null)
                setIsCreating(true)
              }}
            >
              Novo jogador
            </Button>
          )
        }
      />
      <div className="flex flex-col gap-4">
        {isCreating && (
          <section className="rounded-xl border border-slate-200 p-4 sm:p-6">
            <h2 className="mb-4 text-lg font-semibold">Novo jogador</h2>
            <PlayerCreateForm onCreated={handleCreated} onCancel={() => setIsCreating(false)} />
          </section>
        )}
        {notice && <Alert variant="success">{notice}</Alert>}
        {createdPlayer && <PlayerSummary player={createdPlayer} />}

        <PlayerFilterForm clubs={clubs.data ?? []} onApply={applyFilters} />
        <p className="text-sm text-slate-600" aria-live="polite">
          {totalItems !== undefined && `${totalItems} jogador(es) encontrado(s)`}
        </p>
        <PlayerTable
          players={players.data?.data ?? null}
          clubAcronyms={clubAcronyms}
          isLoading={players.isLoading}
          error={players.error}
          onRetry={players.reload}
          onEdit={(player) => openDialog('edit', player)}
          onChangeCondition={(player) => openDialog('condition', player)}
          onDelete={(player) => openDialog('delete', player)}
          onBuy={(player) => openDialog('buy', player)}
        />
        {players.data && (
          <Pagination
            page={page}
            totalPages={players.data.meta.total_pages}
            onPageChange={setPage}
          />
        )}
      </div>

      {dialog?.type === 'edit' && (
        <Modal title={`Editar ${dialog.player.name}`} onClose={closeDialog}>
          <PlayerEditForm
            player={dialog.player}
            onSaved={(player) => finishAction(`Jogador ${player.name} atualizado.`)}
            onCancel={closeDialog}
          />
        </Modal>
      )}
      {dialog?.type === 'condition' && (
        <Modal title={`Condição de ${dialog.player.name}`} onClose={closeDialog}>
          <ConditionForm
            player={dialog.player}
            onSaved={(player) => finishAction(`Condição de ${player.name} atualizada.`)}
            onCancel={closeDialog}
          />
        </Modal>
      )}
      {dialog?.type === 'buy' && (
        <PurchaseDialog
          player={dialog.player}
          clubs={clubs.data ?? []}
          onPurchased={() => {
            players.reload()
            clubs.reload()
          }}
          onClose={closeDialog}
        />
      )}
      {dialog?.type === 'delete' && (
        <ConfirmDialog
          title="Excluir jogador"
          confirmLabel="Excluir"
          onConfirm={() => removal.submit(dialog.player.id)}
          onCancel={closeDialog}
          isSubmitting={removal.isSubmitting}
          errorMessage={removal.errorMessage}
        >
          Excluir <strong>{dialog.player.name}</strong>? O jogador deixa de aparecer na lista, mas o
          histórico de transações continua.
        </ConfirmDialog>
      )}
    </>
  )
}

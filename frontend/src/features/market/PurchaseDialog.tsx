import { useState } from 'react'
import { Alert } from '../../components/ui/Alert'
import { Button } from '../../components/ui/Button'
import { Modal } from '../../components/ui/Modal'
import type { Club } from '../../types/club'
import type { PurchaseResult } from '../../types/market'
import type { Player } from '../../types/player'
import { PurchaseForm } from './PurchaseForm'
import { PurchaseSummary } from './PurchaseSummary'

interface PurchaseDialogProps {
  player: Player
  clubs: Club[]
  onPurchased: () => void
  onClose: () => void
}

/** Purchase form and, once the API accepts it, the purchase result. */
export function PurchaseDialog({ player, clubs, onPurchased, onClose }: PurchaseDialogProps) {
  const [result, setResult] = useState<PurchaseResult | null>(null)

  function handlePurchased(purchase: PurchaseResult) {
    setResult(purchase)
    onPurchased()
  }

  return (
    <Modal title={`Comprar ${player.name}`} onClose={onClose}>
      {result ? (
        <div className="flex flex-col gap-4">
          <Alert variant="success">
            {player.name} comprado por {clubs.find((club) => club.id === result.club_id)?.name}.
          </Alert>
          <PurchaseSummary result={result} />
          <div className="flex justify-end">
            <Button onClick={onClose}>Fechar</Button>
          </div>
        </div>
      ) : (
        <PurchaseForm
          player={player}
          clubs={clubs}
          onPurchased={handlePurchased}
          onCancel={onClose}
        />
      )}
    </Modal>
  )
}

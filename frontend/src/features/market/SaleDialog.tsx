import { useState } from 'react'
import { Alert } from '../../components/ui/Alert'
import { Button } from '../../components/ui/Button'
import { ConfirmDialog } from '../../components/ui/ConfirmDialog'
import { Modal } from '../../components/ui/Modal'
import { useMutation } from '../../hooks/useMutation'
import { sellPlayer } from '../../services/api/marketApi'
import type { SaleResult } from '../../types/market'
import type { SquadPlayer } from '../../types/club'
import { SaleSummary } from './SaleSummary'

interface SaleDialogProps {
  player: SquadPlayer
  onSold: () => void
  onClose: () => void
}

/** Confirmation of the sale and, once the API accepts it, the sale result. */
export function SaleDialog({ player, onSold, onClose }: SaleDialogProps) {
  const [result, setResult] = useState<SaleResult | null>(null)
  const sale = useMutation(sellPlayer, {
    onSuccess: (sold: SaleResult) => {
      setResult(sold)
      onSold()
    },
  })

  if (result) {
    return (
      <Modal title={`Venda de ${player.name}`} onClose={onClose}>
        <div className="flex flex-col gap-4">
          <Alert variant="success">
            {player.name} vendido; contrato {result.contract_status.toLowerCase()}.
          </Alert>
          <SaleSummary result={result} />
          <div className="flex justify-end">
            <Button onClick={onClose}>Fechar</Button>
          </div>
        </div>
      </Modal>
    )
  }

  return (
    <ConfirmDialog
      title={`Vender ${player.name}`}
      confirmLabel="Vender"
      onConfirm={() => sale.submit(player.id)}
      onCancel={onClose}
      isSubmitting={sale.isSubmitting}
      errorMessage={sale.errorMessage}
    >
      Vender <strong>{player.name}</strong> pelo valor de mercado atual? Antes do fim da fidelidade
      há multa de {player.contract?.penalty_rate ?? 0}% sobre o valor, e a comissão incide sobre o
      lucro.
    </ConfirmDialog>
  )
}

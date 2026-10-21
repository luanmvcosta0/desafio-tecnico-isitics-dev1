import type { ReactNode } from 'react'
import { Alert } from './Alert'
import { Button } from './Button'
import { Modal } from './Modal'

interface ConfirmDialogProps {
  title: string
  children: ReactNode
  confirmLabel: string
  onConfirm: () => void
  onCancel: () => void
  isSubmitting: boolean
  errorMessage: string | null
}

export function ConfirmDialog({
  title,
  children,
  confirmLabel,
  onConfirm,
  onCancel,
  isSubmitting,
  errorMessage,
}: ConfirmDialogProps) {
  return (
    <Modal title={title} onClose={onCancel}>
      <div className="flex flex-col gap-4">
        <div className="text-sm text-slate-700">{children}</div>
        {errorMessage && <Alert variant="error">{errorMessage}</Alert>}
        <div className="flex justify-end gap-2">
          <Button variant="secondary" onClick={onCancel} disabled={isSubmitting}>
            Cancelar
          </Button>
          <Button variant="danger" onClick={onConfirm} disabled={isSubmitting}>
            {isSubmitting ? 'Aguarde…' : confirmLabel}
          </Button>
        </div>
      </div>
    </Modal>
  )
}

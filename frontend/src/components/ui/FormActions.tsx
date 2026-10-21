import { Alert } from './Alert'
import { Button } from './Button'

interface FormActionsProps {
  submitLabel: string
  isSubmitting: boolean
  errorMessage: string | null
  onCancel: () => void
}

/** API refusal message plus the cancel and submit buttons of every form. */
export function FormActions({
  submitLabel,
  isSubmitting,
  errorMessage,
  onCancel,
}: FormActionsProps) {
  return (
    <>
      {errorMessage && <Alert variant="error">{errorMessage}</Alert>}
      <div className="flex flex-wrap justify-end gap-2">
        <Button variant="secondary" onClick={onCancel} disabled={isSubmitting}>
          Cancelar
        </Button>
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Salvando…' : submitLabel}
        </Button>
      </div>
    </>
  )
}

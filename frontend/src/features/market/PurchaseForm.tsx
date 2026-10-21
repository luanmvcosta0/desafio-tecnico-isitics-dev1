import { zodResolver } from '@hookform/resolvers/zod'
import { useCallback } from 'react'
import { useForm } from 'react-hook-form'
import { fieldErrorId } from '../../components/ui/fieldStyles'
import { FormActions } from '../../components/ui/FormActions'
import { FormField } from '../../components/ui/FormField'
import { Select } from '../../components/ui/Select'
import { TextInput } from '../../components/ui/TextInput'
import { useMutation } from '../../hooks/useMutation'
import { buyPlayer } from '../../services/api/marketApi'
import type { Club } from '../../types/club'
import type { PurchaseInput, PurchaseResult } from '../../types/market'
import type { Player } from '../../types/player'
import { purchaseSchema, type PurchaseValues } from './purchaseSchemas'

interface PurchaseFormProps {
  player: Player
  clubs: Club[]
  onPurchased: (result: PurchaseResult) => void
  onCancel: () => void
}

export function PurchaseForm({ player, clubs, onPurchased, onCancel }: PurchaseFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<PurchaseValues, unknown, PurchaseInput>({
    resolver: zodResolver(purchaseSchema),
    defaultValues: { club_id: '', loyalty_until: '' },
  })
  const purchase = useCallback((input: PurchaseInput) => buyPlayer(player.id, input), [player.id])
  const { submit, isSubmitting, errorMessage } = useMutation(purchase, {
    onSuccess: onPurchased,
  })
  const clubOptions = clubs.map((club) => ({
    value: String(club.id),
    label: `${club.acronym} · ${club.name}`,
  }))

  return (
    <form
      noValidate
      aria-label="Compra de jogador"
      onSubmit={handleSubmit((values) => submit(values))}
      className="flex flex-col gap-5"
    >
      <FormField label="Clube comprador" htmlFor="purchase-club" error={errors.club_id?.message}>
        <Select
          id="purchase-club"
          placeholder="Selecione"
          options={clubOptions}
          aria-invalid={errors.club_id ? true : undefined}
          aria-describedby={errors.club_id ? fieldErrorId('purchase-club') : undefined}
          {...register('club_id')}
        />
      </FormField>
      <FormField
        label="Fim da fidelidade (horário de Brasília)"
        htmlFor="purchase-loyalty"
        error={errors.loyalty_until?.message}
      >
        <TextInput
          id="purchase-loyalty"
          type="datetime-local"
          aria-invalid={errors.loyalty_until ? true : undefined}
          aria-describedby={errors.loyalty_until ? fieldErrorId('purchase-loyalty') : undefined}
          {...register('loyalty_until')}
        />
      </FormField>
      <FormActions
        submitLabel="Confirmar compra"
        isSubmitting={isSubmitting}
        errorMessage={errorMessage}
        onCancel={onCancel}
      />
    </form>
  )
}

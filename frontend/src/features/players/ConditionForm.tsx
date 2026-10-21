import { zodResolver } from '@hookform/resolvers/zod'
import { useCallback } from 'react'
import { useForm } from 'react-hook-form'
import { fieldErrorId } from '../../components/ui/fieldStyles'
import { FormActions } from '../../components/ui/FormActions'
import { FormField } from '../../components/ui/FormField'
import { Select } from '../../components/ui/Select'
import { useMutation } from '../../hooks/useMutation'
import { changePlayerCondition } from '../../services/api/playersApi'
import type { Condition, Player } from '../../types/player'
import { conditionOptions } from './playerLabels'
import { conditionChangeSchema, type ConditionChangeValues } from './playerSchemas'

interface ConditionFormProps {
  player: Player
  onSaved: (player: Player) => void
  onCancel: () => void
}

export function ConditionForm({ player, onSaved, onCancel }: ConditionFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ConditionChangeValues, unknown, { condition: Condition }>({
    resolver: zodResolver(conditionChangeSchema),
    defaultValues: { condition: player.condition },
  })
  const save = useCallback(
    (condition: Condition) => changePlayerCondition(player.id, condition),
    [player.id],
  )
  const { submit, isSubmitting, errorMessage } = useMutation(save, { onSuccess: onSaved })
  const error = errors.condition?.message

  return (
    <form
      noValidate
      aria-label="Alteração de condição"
      onSubmit={handleSubmit((values) => submit(values.condition))}
      className="flex flex-col gap-5"
    >
      <FormField label="Condição" htmlFor="player-condition" error={error}>
        <Select
          id="player-condition"
          options={conditionOptions}
          aria-invalid={error ? true : undefined}
          aria-describedby={error ? fieldErrorId('player-condition') : undefined}
          {...register('condition')}
        />
      </FormField>
      <FormActions
        submitLabel="Salvar condição"
        isSubmitting={isSubmitting}
        errorMessage={errorMessage}
        onCancel={onCancel}
      />
    </form>
  )
}

import { zodResolver } from '@hookform/resolvers/zod'
import { useCallback } from 'react'
import { FormProvider, useForm } from 'react-hook-form'
import { useMutation } from '../../hooks/useMutation'
import { updatePlayer } from '../../services/api/playersApi'
import type { Player, PlayerUpdateInput } from '../../types/player'
import { FormActions } from '../../components/ui/FormActions'
import { PlayerProfileFields } from './PlayerProfileFields'
import { playerUpdateSchema, type PlayerUpdateValues } from './playerSchemas'

interface PlayerEditFormProps {
  player: Player
  onSaved: (player: Player) => void
  onCancel: () => void
}

export function PlayerEditForm({ player, onSaved, onCancel }: PlayerEditFormProps) {
  const form = useForm<PlayerUpdateValues, unknown, PlayerUpdateInput>({
    resolver: zodResolver(playerUpdateSchema),
    defaultValues: {
      name: player.name,
      position: player.position,
      agility: player.agility,
      age: player.age,
    },
  })
  const save = useCallback(
    (input: PlayerUpdateInput) => updatePlayer(player.id, input),
    [player.id],
  )
  const { submit, isSubmitting, errorMessage } = useMutation(save, { onSuccess: onSaved })

  return (
    <FormProvider {...form}>
      <form
        noValidate
        aria-label="Edição de jogador"
        onSubmit={form.handleSubmit((values) => submit(values))}
        className="flex flex-col gap-5"
      >
        <div className="grid gap-4 sm:grid-cols-2">
          <PlayerProfileFields />
        </div>
        <FormActions
          submitLabel="Salvar alterações"
          isSubmitting={isSubmitting}
          errorMessage={errorMessage}
          onCancel={onCancel}
        />
      </form>
    </FormProvider>
  )
}

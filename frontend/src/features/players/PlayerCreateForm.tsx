import { zodResolver } from '@hookform/resolvers/zod'
import { FormProvider, useForm } from 'react-hook-form'
import { useMutation } from '../../hooks/useMutation'
import { createPlayer } from '../../services/api/playersApi'
import type { Player, PlayerCreateInput } from '../../types/player'
import { FormActions } from './FormActions'
import { PlayerNumberField, type PlayerNumberFieldName } from './PlayerNumberField'
import { PlayerProfileFields } from './PlayerProfileFields'
import { playerCreateSchema, type PlayerCreateValues } from './playerSchemas'

const technicalFields: { name: PlayerNumberFieldName; label: string }[] = [
  { name: 'pace', label: 'Velocidade' },
  { name: 'shooting', label: 'Finalização' },
  { name: 'passing', label: 'Passe' },
  { name: 'dribbling', label: 'Drible' },
  { name: 'defending', label: 'Defesa' },
  { name: 'physical', label: 'Físico' },
  { name: 'potential', label: 'Potencial' },
]

interface PlayerCreateFormProps {
  onCreated: (player: Player) => void
  onCancel: () => void
}

export function PlayerCreateForm({ onCreated, onCancel }: PlayerCreateFormProps) {
  const form = useForm<PlayerCreateValues, unknown, PlayerCreateInput>({
    resolver: zodResolver(playerCreateSchema),
  })
  const { submit, isSubmitting, errorMessage } = useMutation(createPlayer, {
    onSuccess: onCreated,
  })

  return (
    <FormProvider {...form}>
      <form
        noValidate
        aria-label="Cadastro de jogador"
        onSubmit={form.handleSubmit((values) => submit(values))}
        className="flex flex-col gap-5"
      >
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <PlayerProfileFields />
        </div>

        <fieldset className="rounded-lg border border-slate-200 p-4">
          <legend className="px-1 text-sm font-semibold text-slate-700">
            Indicadores técnicos (1–99)
          </legend>
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-4 lg:grid-cols-7">
            {technicalFields.map((field) => (
              <PlayerNumberField key={field.name} {...field} />
            ))}
          </div>
        </fieldset>

        <FormActions
          submitLabel="Cadastrar jogador"
          isSubmitting={isSubmitting}
          errorMessage={errorMessage}
          onCancel={onCancel}
        />
      </form>
    </FormProvider>
  )
}

import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Alert } from '../../components/ui/Alert'
import { Button } from '../../components/ui/Button'
import { fieldErrorId } from '../../components/ui/fieldStyles'
import { FormField } from '../../components/ui/FormField'
import { Select } from '../../components/ui/Select'
import { TextInput } from '../../components/ui/TextInput'
import { useMutation } from '../../hooks/useMutation'
import { createPlayer } from '../../services/api/playersApi'
import { POSITIONS, type Player, type PlayerCreateInput } from '../../types/player'
import { positionLabels } from './playerLabels'
import { playerCreateSchema, type PlayerCreateValues } from './playerSchemas'

type NumericField = Exclude<keyof PlayerCreateInput, 'name' | 'position'>

const profileFields: { name: NumericField; label: string }[] = [
  { name: 'agility', label: 'Agilidade (1–100)' },
  { name: 'age', label: 'Idade (16–45)' },
]

const technicalFields: { name: NumericField; label: string }[] = [
  { name: 'pace', label: 'Velocidade' },
  { name: 'shooting', label: 'Finalização' },
  { name: 'passing', label: 'Passe' },
  { name: 'dribbling', label: 'Drible' },
  { name: 'defending', label: 'Defesa' },
  { name: 'physical', label: 'Físico' },
  { name: 'potential', label: 'Potencial' },
]

const positionOptions = POSITIONS.map((position) => ({
  value: position,
  label: `${position} · ${positionLabels[position]}`,
}))

interface PlayerCreateFormProps {
  onCreated: (player: Player) => void
  onCancel: () => void
}

export function PlayerCreateForm({ onCreated, onCancel }: PlayerCreateFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<PlayerCreateValues, unknown, PlayerCreateInput>({
    resolver: zodResolver(playerCreateSchema),
  })
  const { submit, isSubmitting, errorMessage } = useMutation(createPlayer, {
    onSuccess: onCreated,
  })

  function numberField({ name, label }: { name: NumericField; label: string }) {
    const id = `player-${name}`
    const error = errors[name]?.message
    return (
      <FormField key={name} label={label} htmlFor={id} error={error}>
        <TextInput
          id={id}
          type="number"
          inputMode="numeric"
          aria-invalid={error ? true : undefined}
          aria-describedby={error ? fieldErrorId(id) : undefined}
          {...register(name, { valueAsNumber: true })}
        />
      </FormField>
    )
  }

  return (
    <form
      noValidate
      aria-label="Cadastro de jogador"
      onSubmit={handleSubmit((values) => submit(values))}
      className="flex flex-col gap-5"
    >
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div className="sm:col-span-2">
          <FormField label="Nome" htmlFor="player-name" error={errors.name?.message}>
            <TextInput
              id="player-name"
              autoComplete="off"
              aria-invalid={errors.name ? true : undefined}
              aria-describedby={errors.name ? fieldErrorId('player-name') : undefined}
              {...register('name')}
            />
          </FormField>
        </div>
        <FormField label="Posição" htmlFor="player-position" error={errors.position?.message}>
          <Select
            id="player-position"
            placeholder="Selecione"
            options={positionOptions}
            aria-invalid={errors.position ? true : undefined}
            aria-describedby={errors.position ? fieldErrorId('player-position') : undefined}
            {...register('position')}
          />
        </FormField>
        {profileFields.map(numberField)}
      </div>

      <fieldset className="rounded-lg border border-slate-200 p-4">
        <legend className="px-1 text-sm font-semibold text-slate-700">
          Indicadores técnicos (1–99)
        </legend>
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4 lg:grid-cols-7">
          {technicalFields.map(numberField)}
        </div>
      </fieldset>

      {errorMessage && <Alert variant="error">{errorMessage}</Alert>}

      <div className="flex flex-wrap justify-end gap-2">
        <Button variant="secondary" onClick={onCancel} disabled={isSubmitting}>
          Cancelar
        </Button>
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Salvando…' : 'Cadastrar jogador'}
        </Button>
      </div>
    </form>
  )
}

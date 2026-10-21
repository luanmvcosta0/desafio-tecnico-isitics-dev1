import { useFormContext } from 'react-hook-form'
import { fieldErrorId } from '../../components/ui/fieldStyles'
import { FormField } from '../../components/ui/FormField'
import { Select } from '../../components/ui/Select'
import { TextInput } from '../../components/ui/TextInput'
import { POSITIONS } from '../../types/player'
import { PlayerNumberField } from './PlayerNumberField'
import { positionLabels } from './playerLabels'
import type { PlayerUpdateValues } from './playerSchemas'

const positionOptions = POSITIONS.map((position) => ({
  value: position,
  label: `${position} · ${positionLabels[position]}`,
}))

/** Name, position, agility and age: the fields shared by the registration and the edition. */
export function PlayerProfileFields() {
  const {
    register,
    formState: { errors },
  } = useFormContext<PlayerUpdateValues>()

  return (
    <>
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
      <PlayerNumberField name="agility" label="Agilidade (1–100)" />
      <PlayerNumberField name="age" label="Idade (16–45)" />
    </>
  )
}

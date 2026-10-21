import { useFormContext } from 'react-hook-form'
import { fieldErrorId } from '../../components/ui/fieldStyles'
import { FormField } from '../../components/ui/FormField'
import { TextInput } from '../../components/ui/TextInput'
import type { PlayerCreateInput } from '../../types/player'
import type { PlayerCreateValues } from './playerSchemas'

export type PlayerNumberFieldName = Exclude<keyof PlayerCreateInput, 'name' | 'position'>

interface PlayerNumberFieldProps {
  name: PlayerNumberFieldName
  label: string
}

/** Integer field of the player forms, sent to the API as a JSON number. */
export function PlayerNumberField({ name, label }: PlayerNumberFieldProps) {
  const {
    register,
    formState: { errors },
  } = useFormContext<PlayerCreateValues>()
  const id = `player-${name}`
  const error = errors[name]?.message
  return (
    <FormField label={label} htmlFor={id} error={error}>
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

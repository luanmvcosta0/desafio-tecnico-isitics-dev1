import { zodResolver } from '@hookform/resolvers/zod'
import { useCallback } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { fieldErrorId } from '../../components/ui/fieldStyles'
import { FormActions } from '../../components/ui/FormActions'
import { FormField } from '../../components/ui/FormField'
import { Select } from '../../components/ui/Select'
import { useMutation } from '../../hooks/useMutation'
import { saveLineup } from '../../services/api/lineupApi'
import { FORMATIONS, type Lineup, type LineupInput } from '../../types/league'
import type { Player } from '../../types/player'
import { formationSlots } from './formationSlots'
import { lineupSchema, type LineupValues } from './lineupSchemas'

const EMPTY_STARTERS = ['', '', '', '', '']

interface LineupFormProps {
  clubId: string
  squad: Player[]
  lineup: Lineup
  onSaved: (lineup: Lineup) => void
}

function initialValues(lineup: Lineup): Partial<LineupValues> {
  if (!lineup.complete || lineup.formation === null) {
    return { formation: lineup.formation ?? undefined, starters: EMPTY_STARTERS }
  }
  return {
    formation: lineup.formation,
    starters: lineup.players.map((player) => String(player.id)),
  }
}

/** One select per vacancy, offering only squad players of the sector who are not DOENTE. */
export function LineupForm({ clubId, squad, lineup, onSaved }: LineupFormProps) {
  const {
    register,
    handleSubmit,
    control,
    setValue,
    formState: { errors },
  } = useForm<LineupValues, unknown, LineupInput>({
    resolver: zodResolver(lineupSchema),
    defaultValues: initialValues(lineup),
  })
  const save = useCallback((input: LineupInput) => saveLineup(clubId, input), [clubId])
  const { submit, isSubmitting, errorMessage } = useMutation(save, { onSuccess: onSaved })
  const formation = useWatch({ control, name: 'formation' })
  const starters = useWatch({ control, name: 'starters' })
  const slots = formation ? formationSlots[formation] : []

  function optionsFor(slotIndex: number) {
    const chosenElsewhere = starters.filter((id, index) => index !== slotIndex && id !== '')
    return squad
      .filter(
        (player) =>
          slots[slotIndex].positions.includes(player.position) &&
          player.condition !== 'DOENTE' &&
          !chosenElsewhere.includes(String(player.id)),
      )
      .map((player) => ({ value: String(player.id), label: `${player.name} (${player.position})` }))
  }

  return (
    <form
      noValidate
      aria-label="Escalação"
      onSubmit={handleSubmit((values) => submit(values))}
      className="flex flex-col gap-4"
    >
      <div className="w-full sm:w-64">
        <FormField label="Formação" htmlFor="lineup-formation" error={errors.formation?.message}>
          <Select
            id="lineup-formation"
            placeholder="Selecione"
            options={FORMATIONS.map((code) => ({ value: code, label: code }))}
            aria-invalid={errors.formation ? true : undefined}
            aria-describedby={errors.formation ? fieldErrorId('lineup-formation') : undefined}
            {...register('formation', {
              onChange: () => setValue('starters', EMPTY_STARTERS),
            })}
          />
        </FormField>
      </div>
      {slots.length > 0 && (
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
          {slots.map((slot, index) => {
            const id = `lineup-slot-${index}`
            const error = errors.starters?.[index]?.message
            return (
              <FormField key={id} label={slot.label} htmlFor={id} error={error}>
                <Select
                  id={id}
                  placeholder="Selecione"
                  options={optionsFor(index)}
                  aria-invalid={error ? true : undefined}
                  aria-describedby={error ? fieldErrorId(id) : undefined}
                  {...register(`starters.${index}`)}
                />
              </FormField>
            )
          })}
        </div>
      )}
      <FormActions
        submitLabel="Salvar escalação"
        isSubmitting={isSubmitting}
        errorMessage={errorMessage}
        onCancel={() => setValue('starters', EMPTY_STARTERS)}
        cancelLabel="Limpar vagas"
      />
    </form>
  )
}

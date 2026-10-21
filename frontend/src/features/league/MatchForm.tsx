import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Alert } from '../../components/ui/Alert'
import { Button } from '../../components/ui/Button'
import { fieldErrorId } from '../../components/ui/fieldStyles'
import { FormField } from '../../components/ui/FormField'
import { Select } from '../../components/ui/Select'
import { useMutation } from '../../hooks/useMutation'
import { playMatch } from '../../services/api/matchesApi'
import type { Club } from '../../types/club'
import type { Match, MatchInput } from '../../types/league'
import { matchSchema, type MatchValues } from './matchSchemas'

interface MatchFormProps {
  clubs: Club[]
  onPlayed: (match: Match) => void
}

export function MatchForm({ clubs, onPlayed }: MatchFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<MatchValues, unknown, MatchInput>({
    resolver: zodResolver(matchSchema),
    defaultValues: { home_club_id: '', away_club_id: '' },
  })
  const { submit, isSubmitting, errorMessage } = useMutation(playMatch, { onSuccess: onPlayed })
  const options = clubs.map((club) => ({ value: String(club.id), label: club.name }))

  return (
    <form
      noValidate
      aria-label="Nova partida"
      onSubmit={handleSubmit((values) => submit(values))}
      className="flex flex-col gap-4"
    >
      <div className="grid gap-4 sm:grid-cols-2">
        <FormField label="Mandante" htmlFor="match-home" error={errors.home_club_id?.message}>
          <Select
            id="match-home"
            placeholder="Selecione"
            options={options}
            aria-invalid={errors.home_club_id ? true : undefined}
            aria-describedby={errors.home_club_id ? fieldErrorId('match-home') : undefined}
            {...register('home_club_id')}
          />
        </FormField>
        <FormField label="Visitante" htmlFor="match-away" error={errors.away_club_id?.message}>
          <Select
            id="match-away"
            placeholder="Selecione"
            options={options}
            aria-invalid={errors.away_club_id ? true : undefined}
            aria-describedby={errors.away_club_id ? fieldErrorId('match-away') : undefined}
            {...register('away_club_id')}
          />
        </FormField>
      </div>
      {errorMessage && <Alert variant="error">{errorMessage}</Alert>}
      <div className="flex justify-end">
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Jogando…' : 'Jogar partida'}
        </Button>
      </div>
    </form>
  )
}

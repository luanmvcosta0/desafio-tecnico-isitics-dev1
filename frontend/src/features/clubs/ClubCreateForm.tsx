import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { fieldErrorId } from '../../components/ui/fieldStyles'
import { FormActions } from '../../components/ui/FormActions'
import { FormField } from '../../components/ui/FormField'
import { TextInput } from '../../components/ui/TextInput'
import { useMutation } from '../../hooks/useMutation'
import { createClub } from '../../services/api/clubsApi'
import type { Club, ClubCreateInput } from '../../types/club'
import { clubCreateSchema, type ClubCreateValues } from './clubSchemas'

interface ClubCreateFormProps {
  onCreated: (club: Club) => void
  onCancel: () => void
}

export function ClubCreateForm({ onCreated, onCancel }: ClubCreateFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ClubCreateValues, unknown, ClubCreateInput>({
    resolver: zodResolver(clubCreateSchema),
  })
  const { submit, isSubmitting, errorMessage } = useMutation(createClub, { onSuccess: onCreated })

  return (
    <form
      noValidate
      aria-label="Criação de clube"
      onSubmit={handleSubmit((values) => submit(values))}
      className="flex flex-col gap-5"
    >
      <div className="grid gap-4 sm:grid-cols-3">
        <div className="sm:col-span-2">
          <FormField label="Nome" htmlFor="club-name" error={errors.name?.message}>
            <TextInput
              id="club-name"
              autoComplete="off"
              aria-invalid={errors.name ? true : undefined}
              aria-describedby={errors.name ? fieldErrorId('club-name') : undefined}
              {...register('name')}
            />
          </FormField>
        </div>
        <FormField label="Sigla" htmlFor="club-acronym" error={errors.acronym?.message}>
          <TextInput
            id="club-acronym"
            autoComplete="off"
            maxLength={3}
            aria-invalid={errors.acronym ? true : undefined}
            aria-describedby={errors.acronym ? fieldErrorId('club-acronym') : undefined}
            {...register('acronym')}
          />
        </FormField>
      </div>
      <FormActions
        submitLabel="Criar clube"
        isSubmitting={isSubmitting}
        errorMessage={errorMessage}
        onCancel={onCancel}
      />
    </form>
  )
}

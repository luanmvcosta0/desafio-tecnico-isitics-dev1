import { z } from 'zod'

const NAME_MIN_LENGTH = 3
const NAME_MAX_LENGTH = 60
const ACRONYM_LENGTH = 3

/** Required fields and single-field limits; name format and acronym × name stay with the API. */
export const clubCreateSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, { error: 'Campo obrigatório.' })
    .min(NAME_MIN_LENGTH, { error: `O nome deve ter pelo menos ${NAME_MIN_LENGTH} caracteres.` })
    .max(NAME_MAX_LENGTH, { error: `O nome deve ter no máximo ${NAME_MAX_LENGTH} caracteres.` }),
  acronym: z
    .string()
    .min(1, { error: 'Campo obrigatório.' })
    .regex(/^[A-Z]{3}$/, { error: `A sigla deve ter ${ACRONYM_LENGTH} letras maiúsculas (A–Z).` }),
})

export type ClubCreateValues = z.input<typeof clubCreateSchema>

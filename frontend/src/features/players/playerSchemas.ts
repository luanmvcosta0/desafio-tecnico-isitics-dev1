import { z } from 'zod'
import { CONDITIONS, POSITIONS } from '../../types/player'

const NAME_MIN_LENGTH = 3
const NAME_MAX_LENGTH = 100
const AGILITY_MIN = 1
const AGILITY_MAX = 100
const AGE_MIN = 16
const AGE_MAX = 45
const ATTRIBUTE_MIN = 1
const ATTRIBUTE_MAX = 99

function integerBetween(min: number, max: number) {
  const rangeMessage = `Informe um número inteiro de ${min} a ${max}.`
  return z
    .number({ error: 'Campo obrigatório.' })
    .int({ error: rangeMessage })
    .min(min, { error: rangeMessage })
    .max(max, { error: rangeMessage })
}

const attribute = integerBetween(ATTRIBUTE_MIN, ATTRIBUTE_MAX)

const profileFields = {
  name: z
    .string()
    .trim()
    .min(1, { error: 'Campo obrigatório.' })
    .min(NAME_MIN_LENGTH, { error: `O nome deve ter pelo menos ${NAME_MIN_LENGTH} caracteres.` })
    .max(NAME_MAX_LENGTH, { error: `O nome deve ter no máximo ${NAME_MAX_LENGTH} caracteres.` }),
  position: z.enum(POSITIONS, { error: 'Selecione a posição.' }),
  agility: integerBetween(AGILITY_MIN, AGILITY_MAX),
  age: integerBetween(AGE_MIN, AGE_MAX),
}

/** Required fields and single-field limits; cross-field rules stay with the API. */
export const playerCreateSchema = z.object({
  ...profileFields,
  pace: attribute,
  shooting: attribute,
  passing: attribute,
  dribbling: attribute,
  defending: attribute,
  physical: attribute,
  potential: attribute,
})

export type PlayerCreateValues = z.input<typeof playerCreateSchema>

export const playerUpdateSchema = z.object(profileFields)

export type PlayerUpdateValues = z.input<typeof playerUpdateSchema>

export const conditionChangeSchema = z.object({
  condition: z.enum(CONDITIONS, { error: 'Selecione a condição.' }),
})

export type ConditionChangeValues = z.input<typeof conditionChangeSchema>

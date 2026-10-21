import { z } from 'zod'
import { FORMATIONS } from '../../types/league'

const STARTERS = 5

/** Formation and one starter in each of the 5 vacancies; squad rules stay with the API. */
export const lineupSchema = z
  .object({
    formation: z.enum(FORMATIONS, { error: 'Selecione a formação.' }),
    starters: z
      .array(z.string().min(1, { error: 'Escolha o titular desta vaga.' }))
      .length(STARTERS),
  })
  .transform(({ formation, starters }) => ({ formation, player_ids: starters.map(Number) }))

export type LineupValues = z.input<typeof lineupSchema>

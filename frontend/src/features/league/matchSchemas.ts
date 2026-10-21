import { z } from 'zod'

/** Both clubs are required; equal clubs and incomplete lineups are answered by the API. */
export const matchSchema = z.object({
  home_club_id: z.string().min(1, { error: 'Selecione o mandante.' }).transform(Number),
  away_club_id: z.string().min(1, { error: 'Selecione o visitante.' }).transform(Number),
})

export type MatchValues = z.input<typeof matchSchema>

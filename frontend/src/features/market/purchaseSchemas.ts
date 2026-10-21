import { z } from 'zod'
import { toApiDateTime } from '../../utils/format'

/** Club and loyalty end are required; the date typed in Brasília time is sent with -03:00. */
export const purchaseSchema = z.object({
  club_id: z.string().min(1, { error: 'Selecione o clube comprador.' }).transform(Number),
  loyalty_until: z
    .string()
    .min(1, { error: 'Informe a data e a hora do fim da fidelidade.' })
    .transform(toApiDateTime),
})

export type PurchaseValues = z.input<typeof purchaseSchema>

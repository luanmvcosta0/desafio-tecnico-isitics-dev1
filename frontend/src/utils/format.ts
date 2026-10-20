const BRASILIA_TIME_ZONE = 'America/Sao_Paulo'
const BRASILIA_OFFSET = '-03:00'

const moneyFormatter = new Intl.NumberFormat('pt-BR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

const dateTimeFormatter = new Intl.DateTimeFormat('pt-BR', {
  timeZone: BRASILIA_TIME_ZONE,
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
})

/** Formats a value already computed by the API: 2 decimal places and decimal comma. */
export function formatMoney(value: number): string {
  return moneyFormatter.format(value)
}

/** Cash variation with an explicit sign: "+35,70" for income, "-38,85" for expenses. */
export function formatSignedMoney(value: number): string {
  const sign = value > 0 ? '+' : value < 0 ? '-' : ''
  return `${sign}${moneyFormatter.format(Math.abs(value))}`
}

/** Shows an API date-time in Brasília time, e.g. "05/11/2026, 16:40". */
export function formatDateTime(isoDateTime: string): string {
  return dateTimeFormatter.format(new Date(isoDateTime))
}

/**
 * Converts the value of an `<input type="datetime-local">` (typed in Brasília time) to the
 * format required by the API (RN01): `AAAA-MM-DDTHH:MM-03:00`.
 */
export function toApiDateTime(localDateTime: string): string {
  return `${localDateTime.slice(0, 16)}${BRASILIA_OFFSET}`
}

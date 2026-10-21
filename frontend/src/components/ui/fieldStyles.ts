export const inputClassName =
  'w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100 aria-invalid:border-red-400'

/** Id of the validation message of a field, referenced by `aria-describedby`. */
export function fieldErrorId(fieldId: string): string {
  return `${fieldId}-error`
}

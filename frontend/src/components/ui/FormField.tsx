import type { ReactNode } from 'react'
import { fieldErrorId } from './fieldStyles'

interface FormFieldProps {
  label: string
  htmlFor: string
  error?: string
  children: ReactNode
}

/** Label, input and the validation message shown right below the field. */
export function FormField({ label, htmlFor, error, children }: FormFieldProps) {
  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={htmlFor} className="text-sm font-medium text-slate-700">
        {label}
      </label>
      {children}
      {error && (
        <p id={fieldErrorId(htmlFor)} className="text-xs text-red-600">
          {error}
        </p>
      )}
    </div>
  )
}

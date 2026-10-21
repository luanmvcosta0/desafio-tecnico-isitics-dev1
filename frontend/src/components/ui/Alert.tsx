import type { ReactNode } from 'react'

type AlertVariant = 'success' | 'warning' | 'error'

interface AlertProps {
  variant: AlertVariant
  children: ReactNode
}

const variantClassNames: Record<AlertVariant, string> = {
  success: 'border-emerald-200 bg-emerald-50 text-emerald-800',
  warning: 'border-amber-200 bg-amber-50 text-amber-900',
  error: 'border-red-200 bg-red-50 text-red-800',
}

export function Alert({ variant, children }: AlertProps) {
  return (
    <div
      role={variant === 'error' ? 'alert' : 'status'}
      className={`rounded-lg border px-4 py-3 text-sm ${variantClassNames[variant]}`}
    >
      {children}
    </div>
  )
}

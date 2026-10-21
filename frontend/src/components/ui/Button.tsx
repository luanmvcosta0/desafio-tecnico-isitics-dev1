import type { ButtonHTMLAttributes, MouseEvent } from 'react'

type ButtonVariant = 'primary' | 'secondary' | 'danger'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant
}

const variantClassNames: Record<ButtonVariant, string> = {
  primary: 'bg-brand-600 text-white hover:bg-brand-700',
  secondary: 'border border-slate-300 bg-white text-slate-700 hover:bg-slate-50',
  danger: 'bg-red-600 text-white hover:bg-red-700',
}

const SINGLE_CLICK = 1

/**
 * Shared button. The extra clicks of a double click are ignored, so a fast API answer to the first
 * click can never let the second one send the same action again.
 */
export function Button({
  variant = 'primary',
  type = 'button',
  className = '',
  onClick,
  ...props
}: ButtonProps) {
  function handleClick(event: MouseEvent<HTMLButtonElement>) {
    if (event.detail > SINGLE_CLICK) {
      event.preventDefault()
      return
    }
    onClick?.(event)
  }

  return (
    <button
      type={type}
      className={`rounded-lg px-4 py-2 text-sm font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-60 ${variantClassNames[variant]} ${className}`}
      onClick={handleClick}
      {...props}
    />
  )
}

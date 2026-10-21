import type { InputHTMLAttributes, Ref } from 'react'
import { inputClassName } from './fieldStyles'

interface TextInputProps extends InputHTMLAttributes<HTMLInputElement> {
  ref?: Ref<HTMLInputElement>
}

export function TextInput({ className = '', ...props }: TextInputProps) {
  return <input className={`${inputClassName} ${className}`} {...props} />
}

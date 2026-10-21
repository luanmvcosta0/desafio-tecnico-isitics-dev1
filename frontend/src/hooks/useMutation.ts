import { useCallback, useRef, useState } from 'react'
import { ApiError } from '../services/api/httpClient'

const UNEXPECTED_ERROR_MESSAGE = 'Erro inesperado. Tente novamente.'

interface MutationOptions<TResult> {
  onSuccess?: (result: TResult) => void
}

function toErrorMessage(error: unknown): string {
  return error instanceof ApiError ? error.message : UNEXPECTED_ERROR_MESSAGE
}

/**
 * Runs an action that changes data. While a call is pending, new calls are ignored, so a double
 * click never sends the same action twice. The API `message` of a refusal is kept in `errorMessage`.
 */
export function useMutation<TInput, TResult>(
  action: (input: TInput) => Promise<TResult>,
  { onSuccess }: MutationOptions<TResult> = {},
) {
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const pending = useRef(false)

  const submit = useCallback(
    async (input: TInput) => {
      if (pending.current) {
        return
      }
      pending.current = true
      setIsSubmitting(true)
      setErrorMessage(null)
      try {
        const result = await action(input)
        onSuccess?.(result)
      } catch (error) {
        setErrorMessage(toErrorMessage(error))
      } finally {
        pending.current = false
        setIsSubmitting(false)
      }
    },
    [action, onSuccess],
  )

  const reset = useCallback(() => setErrorMessage(null), [])

  return { submit, isSubmitting, errorMessage, reset }
}

import { useCallback, useEffect, useMemo, useState } from 'react'
import { errorMessageOf } from '../services/api/httpClient'

const UNEXPECTED_ERROR_MESSAGE = 'Erro inesperado ao carregar os dados.'

interface QueryResult<T> {
  request: object
  data: T | null
  error: string | null
}

/**
 * Loads data from the API whenever `load` changes (wrap it in `useCallback` with its inputs).
 * `reload` fetches again after a change. The query is loading until the result of the current
 * request arrives; results of outdated requests are discarded.
 */
export function useApiQuery<T>(load: () => Promise<T>) {
  const [version, setVersion] = useState(0)
  const [result, setResult] = useState<QueryResult<T> | null>(null)
  const request = useMemo(() => ({ load, version }), [load, version])

  useEffect(() => {
    let isCurrent = true
    request
      .load()
      .then((data) => {
        if (isCurrent) {
          setResult({ request, data, error: null })
        }
      })
      .catch((error: unknown) => {
        if (isCurrent) {
          setResult({ request, data: null, error: errorMessageOf(error, UNEXPECTED_ERROR_MESSAGE) })
        }
      })
    return () => {
      isCurrent = false
    }
  }, [request])

  const reload = useCallback(() => setVersion((current) => current + 1), [])
  const isLoading = result?.request !== request

  return {
    data: result?.data ?? null,
    isLoading,
    error: isLoading ? null : (result?.error ?? null),
    reload,
  }
}

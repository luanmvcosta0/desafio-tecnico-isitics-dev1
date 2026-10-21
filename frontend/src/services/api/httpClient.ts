import { env } from '../../config/env'

const NETWORK_ERROR_MESSAGE = 'Não foi possível conectar à API. Tente novamente.'
const UNKNOWN_ERROR_MESSAGE = 'A API recusou a operação.'

export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

/** The API `message` of a refusal, or `fallback` for any other failure. */
export function errorMessageOf(error: unknown, fallback: string): string {
  return error instanceof ApiError ? error.message : fallback
}

type QueryValue = string | number | boolean | undefined
export type QueryParams = Record<string, QueryValue>

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
  query?: QueryParams
}

export function buildUrl(path: string, query: QueryParams = {}): string {
  const url = new URL(`${env.apiBaseUrl}${path}`)
  Object.entries(query)
    .filter(([, value]) => value !== undefined && value !== '')
    .forEach(([key, value]) => url.searchParams.set(key, String(value)))
  return url.toString()
}

async function send(path: string, options: RequestOptions): Promise<Response> {
  const { method = 'GET', body, query } = options
  let response: Response
  try {
    response = await fetch(buildUrl(path, query), {
      method,
      headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch {
    throw new ApiError(0, NETWORK_ERROR_MESSAGE)
  }
  if (!response.ok) {
    throw new ApiError(response.status, await readErrorMessage(response))
  }
  return response
}

async function readErrorMessage(response: Response): Promise<string> {
  try {
    const payload: unknown = await response.json()
    if (isErrorPayload(payload) && payload.message.trim() !== '') {
      return payload.message
    }
  } catch {
    return UNKNOWN_ERROR_MESSAGE
  }
  return UNKNOWN_ERROR_MESSAGE
}

function isErrorPayload(payload: unknown): payload is { message: string } {
  return (
    typeof payload === 'object' &&
    payload !== null &&
    typeof (payload as { message?: unknown }).message === 'string'
  )
}

export async function requestJson<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const response = await send(path, options)
  return (await response.json()) as T
}

export async function requestWithoutContent(
  path: string,
  options: RequestOptions = {},
): Promise<void> {
  await send(path, options)
}

export async function requestBlob(path: string, options: RequestOptions = {}): Promise<Blob> {
  const response = await send(path, options)
  return response.blob()
}

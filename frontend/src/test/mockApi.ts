import { vi } from 'vitest'

type Handler = (url: URL, init: RequestInit | undefined) => Response | Promise<Response>

export function jsonResponse(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

/**
 * Mocks `fetch` routing each call by "METHOD /path" (path without /api/v1). Unmatched calls fail
 * the test, so every request the screen sends must be expected.
 */
export function mockApi(handlers: Record<string, Handler>) {
  return vi.spyOn(globalThis, 'fetch').mockImplementation(async (input, init) => {
    const url = new URL(String(input))
    const key = `${init?.method ?? 'GET'} ${url.pathname.replace('/api/v1', '')}`
    const handler = handlers[key]
    if (!handler) {
      throw new Error(`Unexpected request: ${key}`)
    }
    return handler(url, init)
  })
}

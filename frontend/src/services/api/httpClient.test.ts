import { describe, expect, it, vi } from 'vitest'
import { ApiError, buildUrl, requestJson, requestWithoutContent } from './httpClient'

function mockFetch(response: Response) {
  return vi.spyOn(globalThis, 'fetch').mockResolvedValue(response)
}

function jsonResponse(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

describe('buildUrl', () => {
  it('prefixes /api/v1 and skips empty query params', () => {
    expect(buildUrl('/players', { search: 'lua', position: undefined, page: 2, club_id: '' })).toBe(
      'http://localhost:8080/api/v1/players?search=lua&page=2',
    )
  })
})

describe('requestJson', () => {
  it('sends the body as JSON and returns the parsed response', async () => {
    const fetchSpy = mockFetch(jsonResponse(201, { id: 1 }))

    const result = await requestJson<{ id: number }>('/clubs', {
      method: 'POST',
      body: { name: 'Águias FC', acronym: 'AGU' },
    })

    expect(result).toEqual({ id: 1 })
    expect(fetchSpy).toHaveBeenCalledWith('http://localhost:8080/api/v1/clubs', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: 'Águias FC', acronym: 'AGU' }),
    })
  })

  it('throws ApiError with the message returned by the API', async () => {
    mockFetch(jsonResponse(409, { message: 'Limite de 50% do caixa excedido.' }))

    await expect(requestJson('/players/1/buy', { method: 'POST' })).rejects.toEqual(
      new ApiError(409, 'Limite de 50% do caixa excedido.'),
    )
  })

  it('throws a connection error when the API is unreachable', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new TypeError('Failed to fetch'))

    await expect(requestJson('/players')).rejects.toMatchObject({ status: 0 })
  })
})

describe('requestWithoutContent', () => {
  it('accepts 204 responses without body', async () => {
    mockFetch(new Response(null, { status: 204 }))

    await expect(requestWithoutContent('/players/9', { method: 'DELETE' })).resolves.toBeUndefined()
  })
})

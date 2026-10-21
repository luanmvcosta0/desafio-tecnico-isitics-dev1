import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { jsonResponse, mockApi } from '../test/mockApi'
import { StandingsPage } from './StandingsPage'

const row = (position: number, name: string, points: number, difference: number) => ({
  position,
  club_id: position,
  club_name: name,
  played: 2,
  wins: points / 3,
  draws: 0,
  losses: 2 - points / 3,
  goals_for: 3,
  goals_against: 3 - difference,
  goal_difference: difference,
  points,
})

describe('StandingsPage', () => {
  it('shows every column in the API order', async () => {
    mockApi({
      'GET /standings': () =>
        jsonResponse(200, [row(1, 'Gama SC', 6, 2), row(2, 'Águias FC', 0, -2)]),
    })
    render(<StandingsPage />)

    await screen.findByText('Gama SC')
    const rows = screen.getAllByRole('row').slice(1)
    expect(rows[0]).toHaveTextContent('1ºGama SC220031+26')
    expect(rows[1]).toHaveTextContent('2ºÁguias FC200235-20')
  })

  it('shows the error state with retry when the API is down', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new TypeError('Failed to fetch'))
    render(<StandingsPage />)

    expect(await screen.findByRole('alert')).toHaveTextContent('Não foi possível conectar à API.')
    expect(screen.getByRole('button', { name: 'Tentar novamente' })).toBeInTheDocument()
  })
})

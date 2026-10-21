import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it } from 'vitest'
import { aMatch } from '../test/matchFixtures'
import { jsonResponse, mockApi } from '../test/mockApi'
import { MatchDetailsPage } from './MatchDetailsPage'

function renderAt(path: string) {
  render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/matches/:matchId" element={<MatchDetailsPage />} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('MatchDetailsPage', () => {
  it('shows the recorded score and duels', async () => {
    mockApi({ 'GET /matches/1': () => jsonResponse(200, aMatch) })
    renderAt('/matches/1')

    expect(await screen.findByText('Águias FC 1 × 2 Delta FC')).toBeInTheDocument()
    expect(screen.getAllByRole('row').slice(1)).toHaveLength(6)
    expect(screen.getByText('Lino Matos (ATA, Apto)')).toBeInTheDocument()
  })

  it('shows the API message for an unknown match', async () => {
    mockApi({ 'GET /matches/9': () => jsonResponse(404, { message: 'Partida 9 não encontrada.' }) })
    renderAt('/matches/9')

    expect(await screen.findByRole('alert')).toHaveTextContent('Partida 9 não encontrada.')
  })
})

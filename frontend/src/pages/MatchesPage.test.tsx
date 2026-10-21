import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router'
import { describe, expect, it } from 'vitest'
import { aMatch } from '../test/matchFixtures'
import { jsonResponse, mockApi } from '../test/mockApi'
import type { Match } from '../types/league'
import { MatchesPage } from './MatchesPage'

const clubs = [
  { id: 1, name: 'Águias FC', acronym: 'AGU', cash: 300, squad_value: 0, net_worth: 300 },
  { id: 2, name: 'Delta FC', acronym: 'DFC', cash: 300, squad_value: 0, net_worth: 300 },
]
const { duels: _duels, ...older } = { ...aMatch, id: 1 }
const listed: Match[] = [{ ...older, id: 2, played_at: '2026-11-13T09:30:00-03:00' }, older]

function renderPage() {
  render(
    <MemoryRouter>
      <MatchesPage />
    </MemoryRouter>,
  )
  return userEvent.setup()
}

describe('MatchesPage', () => {
  it('requires both clubs before playing', async () => {
    const fetchSpy = mockApi({
      'GET /clubs': () => jsonResponse(200, clubs),
      'GET /matches': () => jsonResponse(200, []),
    })
    const user = renderPage()
    await screen.findByText('Nenhuma partida jogada.')

    await user.click(screen.getByRole('button', { name: 'Jogar partida' }))

    expect(screen.getByLabelText('Mandante')).toHaveAccessibleDescription('Selecione o mandante.')
    expect(screen.getByLabelText('Visitante')).toHaveAccessibleDescription('Selecione o visitante.')
    expect(fetchSpy).toHaveBeenCalledTimes(2)
  })

  it('plays once even on double click and shows the score and the six duels', async () => {
    let body: unknown
    let plays = 0
    mockApi({
      'GET /clubs': () => jsonResponse(200, clubs),
      'GET /matches': () => jsonResponse(200, plays > 0 ? [older] : []),
      'POST /matches': (_url, init) => {
        plays += 1
        body = JSON.parse(init?.body as string)
        return jsonResponse(201, aMatch)
      },
    })
    const user = renderPage()
    await screen.findByText('Nenhuma partida jogada.')

    await user.selectOptions(screen.getByLabelText('Mandante'), 'Águias FC')
    await user.selectOptions(screen.getByLabelText('Visitante'), 'Delta FC')
    await user.dblClick(screen.getByRole('button', { name: 'Jogar partida' }))

    const result = within(await screen.findByRole('region', { name: 'Resultado da partida' }))
    expect(result.getByText('Águias FC 1 × 2 Delta FC')).toBeInTheDocument()
    expect(result.getByText(/Vitória do visitante/)).toBeInTheDocument()
    const duelRows = result.getAllByRole('row').slice(1)
    expect(duelRows).toHaveLength(6)
    expect(duelRows[0]).toHaveTextContent('Otto Rangel (ATA, Apto)')
    expect(duelRows[0]).toHaveTextContent('Finalização 75')
    expect(duelRows[0]).toHaveTextContent('Gol')
    expect(duelRows[2]).toHaveTextContent('Não')
    expect(plays).toBe(1)
    expect(body).toEqual({ home_club_id: 1, away_club_id: 2 })
    expect(await screen.findByRole('link', { name: 'Ver duelos' })).toHaveAttribute(
      'href',
      '/matches/1',
    )
  })

  it('shows the API message when a club cannot play', async () => {
    const message = 'O clube Águias FC não tem escalação completa (5 titulares) e não pode jogar.'
    mockApi({
      'GET /clubs': () => jsonResponse(200, clubs),
      'GET /matches': () => jsonResponse(200, []),
      'POST /matches': () => jsonResponse(409, { message }),
    })
    const user = renderPage()
    await screen.findByText('Nenhuma partida jogada.')

    await user.selectOptions(screen.getByLabelText('Mandante'), 'Águias FC')
    await user.selectOptions(screen.getByLabelText('Visitante'), 'Delta FC')
    await user.click(screen.getByRole('button', { name: 'Jogar partida' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(message)
  })

  it('lists the history in the API order and filters by club', async () => {
    const queries: string[] = []
    mockApi({
      'GET /clubs': () => jsonResponse(200, clubs),
      'GET /matches': (url) => {
        queries.push(url.search)
        return jsonResponse(200, url.searchParams.has('club_id') ? [older] : listed)
      },
    })
    const user = renderPage()

    expect(await screen.findByText('13/11/2026, 09:30')).toBeInTheDocument()
    const rows = screen.getAllByRole('row').slice(1)
    expect(rows.map((row) => row.querySelector('td')?.textContent)).toEqual([
      '13/11/2026, 09:30',
      '12/11/2026, 15:00',
    ])
    expect(rows[0]).toHaveTextContent('Águias FC1 × 2Delta FC')

    await user.selectOptions(screen.getByLabelText('Filtrar por clube'), 'Delta FC')

    await screen.findByText('12/11/2026, 15:00')
    expect(screen.queryByText('13/11/2026, 09:30')).not.toBeInTheDocument()
    expect(queries).toEqual(['', '?club_id=2'])
  })
})

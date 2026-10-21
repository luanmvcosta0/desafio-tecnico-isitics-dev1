import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { jsonResponse, mockApi } from '../../test/mockApi'
import { aPlayer } from '../../test/playerFixtures'
import type { Lineup } from '../../types/league'
import { LineupSection } from './LineupSection'

const goalkeeper = aPlayer({ id: 1, name: 'Rui Prado', position: 'GOL', club_id: 1 })
const defender = aPlayer({ id: 2, name: 'Téo Brandão', position: 'ZAG', club_id: 1 })
const fullBack = aPlayer({ id: 3, name: 'Davi Lins', position: 'LAT', club_id: 1 })
const midfielder = aPlayer({ id: 4, name: 'Lauro Neves', position: 'MEI', club_id: 1 })
const attacker = aPlayer({ id: 5, name: 'Otto Rangel', position: 'ATA', club_id: 1 })
const sickGoalkeeper = aPlayer({
  id: 6,
  name: 'Ivo Doente',
  position: 'GOL',
  condition: 'DOENTE',
  club_id: 1,
})
const squad = [goalkeeper, sickGoalkeeper, defender, fullBack, midfielder, attacker]

const noLineup: Lineup = { club_id: 1, formation: null, complete: false, players: [] }
const saved: Lineup = {
  club_id: 1,
  formation: '2-1-1',
  complete: true,
  players: [goalkeeper, defender, fullBack, midfielder, attacker],
}

function optionsOf(select: HTMLElement) {
  return within(select)
    .getAllByRole('option')
    .map((option) => option.textContent)
}

function renderSection() {
  render(<LineupSection clubId="1" squad={squad} />)
  return userEvent.setup()
}

describe('LineupSection', () => {
  it('warns that a club without lineup cannot play', async () => {
    mockApi({ 'GET /clubs/1/lineup': () => jsonResponse(200, noLineup) })
    renderSection()

    expect(
      await screen.findByText('O clube ainda não tem escalação e não pode jogar partidas.'),
    ).toBeInTheDocument()
    expect(screen.getByText('Nenhum titular escalado.')).toBeInTheDocument()
  })

  it('warns about an incomplete lineup and keeps its formation', async () => {
    mockApi({
      'GET /clubs/1/lineup': () =>
        jsonResponse(200, { ...saved, complete: false, players: saved.players.slice(0, 4) }),
    })
    renderSection()

    expect(
      await screen.findByText(
        'Escalação 2-1-1 incompleta: o clube não pode jogar até escalar os 5 titulares.',
      ),
    ).toBeInTheDocument()
  })

  it('offers per vacancy only squad players of the sector who are not DOENTE', async () => {
    mockApi({ 'GET /clubs/1/lineup': () => jsonResponse(200, noLineup) })
    const user = renderSection()

    await user.selectOptions(await screen.findByLabelText('Formação'), '2-1-1')

    expect(optionsOf(screen.getByLabelText('Goleiro'))).toEqual(['Selecione', 'Rui Prado (GOL)'])
    expect(optionsOf(screen.getByLabelText('Defesa (ZAG/LAT) 1'))).toEqual([
      'Selecione',
      'Téo Brandão (ZAG)',
      'Davi Lins (LAT)',
    ])
    await user.selectOptions(screen.getByLabelText('Defesa (ZAG/LAT) 1'), 'Téo Brandão (ZAG)')
    expect(optionsOf(screen.getByLabelText('Defesa (ZAG/LAT) 2'))).toEqual([
      'Selecione',
      'Davi Lins (LAT)',
    ])
    expect(optionsOf(screen.getByLabelText('Meio (MEI)'))).toEqual([
      'Selecione',
      'Lauro Neves (MEI)',
    ])
  })

  it('requires the formation and every vacancy before sending', async () => {
    const fetchSpy = mockApi({ 'GET /clubs/1/lineup': () => jsonResponse(200, noLineup) })
    const user = renderSection()
    await screen.findByLabelText('Formação')

    await user.click(screen.getByRole('button', { name: 'Salvar escalação' }))
    expect(screen.getByLabelText('Formação')).toHaveAccessibleDescription('Selecione a formação.')

    await user.selectOptions(screen.getByLabelText('Formação'), '1-2-1')
    await user.click(screen.getByRole('button', { name: 'Salvar escalação' }))
    expect(screen.getByLabelText('Goleiro')).toHaveAccessibleDescription(
      'Escolha o titular desta vaga.',
    )
    expect(fetchSpy).toHaveBeenCalledTimes(1)
  })

  it('saves the lineup and shows the starters returned by the API', async () => {
    let body: unknown
    let current = noLineup
    mockApi({
      'GET /clubs/1/lineup': () => jsonResponse(200, current),
      'PUT /clubs/1/lineup': (_url, init) => {
        body = JSON.parse(init?.body as string)
        current = saved
        return jsonResponse(200, saved)
      },
    })
    const user = renderSection()

    await user.selectOptions(await screen.findByLabelText('Formação'), '2-1-1')
    await user.selectOptions(screen.getByLabelText('Goleiro'), 'Rui Prado (GOL)')
    await user.selectOptions(screen.getByLabelText('Defesa (ZAG/LAT) 1'), 'Davi Lins (LAT)')
    await user.selectOptions(screen.getByLabelText('Defesa (ZAG/LAT) 2'), 'Téo Brandão (ZAG)')
    await user.selectOptions(screen.getByLabelText('Meio (MEI)'), 'Lauro Neves (MEI)')
    await user.selectOptions(screen.getByLabelText('Ataque (ATA)'), 'Otto Rangel (ATA)')
    await user.click(screen.getByRole('button', { name: 'Salvar escalação' }))

    expect(await screen.findByText('Escalação 2-1-1 salva.')).toBeInTheDocument()
    await screen.findByRole('cell', { name: 'Rui Prado' })
    expect(body).toEqual({ formation: '2-1-1', player_ids: [1, 3, 2, 4, 5] })
    const rows = screen.getAllByRole('row').slice(1)
    expect(rows.map((row) => row.querySelector('td')?.textContent)).toEqual([
      'GOL',
      'ZAG',
      'LAT',
      'MEI',
      'ATA',
    ])
  })

  it('shows the API message when the lineup is refused', async () => {
    const message = 'O jogador Rui Prado está DOENTE e não pode ser escalado.'
    mockApi({
      'GET /clubs/1/lineup': () => jsonResponse(200, saved),
      'PUT /clubs/1/lineup': () => jsonResponse(409, { message }),
    })
    const user = renderSection()

    await user.click(await screen.findByRole('button', { name: 'Salvar escalação' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(message)
  })
})

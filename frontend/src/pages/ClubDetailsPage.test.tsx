import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it } from 'vitest'
import { jsonResponse, mockApi } from '../test/mockApi'
import { aPlayer } from '../test/playerFixtures'
import type { ClubDetails } from '../types/club'
import type { SaleResult } from '../types/market'
import { ClubDetailsPage } from './ClubDetailsPage'

const squadPlayer = {
  ...aPlayer({ club_id: 1 }),
  contract: {
    purchase_value: 37,
    loyalty_until: '2027-03-02T10:15:00-03:00',
    remaining_days: 117,
    penalty_rate: 10,
  },
}

const club: ClubDetails = {
  id: 1,
  name: 'Águias FC',
  acronym: 'AGU',
  cash: 261.15,
  squad_value: 40,
  net_worth: 301.15,
  players: [squadPlayer],
}

const sale: SaleResult = {
  id: 11,
  club_id: 1,
  player_id: 2,
  player_name: 'Meia Lua',
  type: 'VENDA',
  value: 40,
  fee: 0.3,
  penalty: 4,
  profit: 3,
  cash_change: 35.7,
  created_at: '2026-11-05T16:40:00-03:00',
  purchase_value: 37,
  remaining_days: 117,
  penalty_rate: 10,
  contract_status: 'RESCINDIDO',
  club_cash: 296.85,
}

function renderAt(path: string) {
  render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/clubs/:clubId" element={<ClubDetailsPage />} />
      </Routes>
    </MemoryRouter>,
  )
  return userEvent.setup()
}

function valueOf(container: HTMLElement, label: string) {
  return within(container).getByText(label).nextSibling
}

describe('ClubDetailsPage', () => {
  it('shows the club values and the squad with the contract columns', async () => {
    mockApi({ 'GET /clubs/1': () => jsonResponse(200, club) })
    renderAt('/clubs/1')

    expect(
      await screen.findByRole('heading', { level: 1, name: 'Águias FC (AGU)' }),
    ).toBeInTheDocument()
    const values = screen.getByLabelText('Valores do clube')
    expect(valueOf(values, 'Caixa')).toHaveTextContent('261,15')
    expect(valueOf(values, 'Valor do elenco')).toHaveTextContent('40,00')
    expect(valueOf(values, 'Patrimônio')).toHaveTextContent('301,15')
    const row = within(screen.getByRole('row', { name: /Meia Lua/ }))
    expect(row.getByText('02/03/2027, 10:15')).toBeInTheDocument()
    expect(row.getByText('117')).toBeInTheDocument()
    expect(row.getByText('10%')).toBeInTheDocument()
  })

  it('sells after confirmation, shows the result and reloads the squad', async () => {
    let sold = false
    mockApi({
      'GET /clubs/1': () => jsonResponse(200, sold ? { ...club, cash: 296.85, players: [] } : club),
      'POST /players/2/sell': () => {
        sold = true
        return jsonResponse(200, sale)
      },
    })
    const user = renderAt('/clubs/1')

    await user.click(await screen.findByRole('button', { name: 'Vender Meia Lua' }))
    const confirm = within(screen.getByRole('dialog', { name: 'Vender Meia Lua' }))
    await user.click(confirm.getByRole('button', { name: 'Vender' }))

    const summary = await screen.findByLabelText('Resultado da venda')
    expect(valueOf(summary, 'Valor de compra')).toHaveTextContent('37,00')
    expect(valueOf(summary, 'Valor de venda')).toHaveTextContent('40,00')
    expect(valueOf(summary, 'Dias restantes')).toHaveTextContent('117')
    expect(valueOf(summary, 'Multa (10%)')).toHaveTextContent('4,00')
    expect(valueOf(summary, 'Lucro')).toHaveTextContent('+3,00')
    expect(valueOf(summary, 'Comissão')).toHaveTextContent('0,30')
    expect(valueOf(summary, 'Crédito')).toHaveTextContent('35,70')
    expect(valueOf(summary, 'Novo caixa')).toHaveTextContent('296,85')

    await user.click(screen.getByRole('button', { name: 'Fechar' }))
    expect(
      await screen.findByText(
        'O clube ainda não tem jogadores. Compre jogadores livres na tela de Jogadores.',
      ),
    ).toBeInTheDocument()
    expect(valueOf(screen.getByLabelText('Valores do clube'), 'Caixa')).toHaveTextContent('296,85')
  })

  it('shows the API message when the sale is refused', async () => {
    const message = 'O jogador Meia Lua está livre; só jogadores de um clube podem ser vendidos.'
    mockApi({
      'GET /clubs/1': () => jsonResponse(200, club),
      'POST /players/2/sell': () => jsonResponse(409, { message }),
    })
    const user = renderAt('/clubs/1')

    await user.click(await screen.findByRole('button', { name: 'Vender Meia Lua' }))
    await user.click(screen.getByRole('button', { name: 'Vender' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(message)
  })

  it('shows the API message for an unknown club', async () => {
    mockApi({ 'GET /clubs/9': () => jsonResponse(404, { message: 'Clube 9 não encontrado.' }) })
    renderAt('/clubs/9')

    expect(await screen.findByRole('alert')).toHaveTextContent('Clube 9 não encontrado.')
  })

  it('opens the transaction history in its own tab', async () => {
    mockApi({
      'GET /clubs/1': () => jsonResponse(200, club),
      'GET /clubs/1/transactions': () =>
        jsonResponse(200, {
          data: [sale],
          summary: {
            total_spent: 0,
            total_received: 35.7,
            total_fees: 0.3,
            total_penalties: 4,
            total_profit: 3,
          },
        }),
    })
    const user = renderAt('/clubs/1')
    await screen.findByRole('button', { name: 'Vender Meia Lua' })

    await user.click(screen.getByRole('tab', { name: 'Histórico de transações' }))

    expect(screen.getByRole('tab', { name: 'Histórico de transações' })).toHaveAttribute(
      'aria-selected',
      'true',
    )
    expect(await screen.findByRole('cell', { name: 'Venda' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Vender Meia Lua' })).not.toBeInTheDocument()
  })
})

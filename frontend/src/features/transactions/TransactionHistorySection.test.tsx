import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { jsonResponse, mockApi } from '../../test/mockApi'
import type { Transaction, TransactionHistory } from '../../types/market'
import { TransactionHistorySection } from './TransactionHistorySection'

const sale: Transaction = {
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
}

const purchase: Transaction = {
  ...sale,
  id: 10,
  player_id: 3,
  player_name: 'Otto Rangel',
  type: 'COMPRA',
  value: 37,
  fee: 1.85,
  penalty: 0,
  profit: null,
  cash_change: -38.85,
  created_at: '2026-11-02T10:15:00-03:00',
}

const wholeHistory: TransactionHistory = {
  data: [sale, purchase],
  summary: {
    total_spent: 38.85,
    total_received: 35.7,
    total_fees: 2.15,
    total_penalties: 4,
    total_profit: 3,
  },
}

const onlySales: TransactionHistory = {
  data: [sale],
  summary: {
    total_spent: 0,
    total_received: 35.7,
    total_fees: 0.3,
    total_penalties: 4,
    total_profit: 3,
  },
}

function valueOf(label: string) {
  return within(screen.getByLabelText('Totais do histórico')).getByText(label).nextSibling
}

describe('TransactionHistorySection', () => {
  it('shows the transactions of the API in order, with signed cash change and totals', async () => {
    mockApi({ 'GET /clubs/1/transactions': () => jsonResponse(200, wholeHistory) })
    render(<TransactionHistorySection clubId="1" />)

    await screen.findByRole('cell', { name: 'Otto Rangel' })
    const rows = screen.getAllByRole('row').slice(1)
    expect(rows.map((row) => row.textContent)).toEqual([
      '05/11/2026, 16:40VendaMeia Lua40,000,304,00+3,00+35,70',
      '02/11/2026, 10:15CompraOtto Rangel37,001,850,00—-38,85',
    ])
    expect(valueOf('Total pago em compras')).toHaveTextContent('38,85')
    expect(valueOf('Total recebido em vendas')).toHaveTextContent('35,70')
    expect(valueOf('Total de taxas e comissões')).toHaveTextContent('2,15')
    expect(valueOf('Total de multas')).toHaveTextContent('4,00')
    expect(valueOf('Lucro total')).toHaveTextContent('+3,00')
  })

  it('filters by types and by a player of the whole history', async () => {
    const queries: string[] = []
    mockApi({
      'GET /clubs/1/transactions': (url) => {
        queries.push(url.search)
        return jsonResponse(200, url.search === '' ? wholeHistory : onlySales)
      },
    })
    const user = userEvent.setup()
    render(<TransactionHistorySection clubId="1" />)
    await screen.findByRole('cell', { name: 'Otto Rangel' })

    await user.click(screen.getByLabelText('Venda'))
    await user.click(screen.getByLabelText('Compra'))
    await user.selectOptions(screen.getByLabelText('Jogador'), 'Otto Rangel')

    await vi.waitFor(() =>
      expect(screen.queryByRole('cell', { name: 'Otto Rangel' })).not.toBeInTheDocument(),
    )
    expect(queries).toContain('?type=VENDA')
    expect(queries.at(-1)).toBe('')
    expect(queries).toContain('?type=COMPRA%2CVENDA&player_id=3')
    expect(screen.getByLabelText('Jogador')).toHaveTextContent('Meia Lua')
    expect(valueOf('Total pago em compras')).toHaveTextContent('0,00')
  })

  it('shows the API message when a filter is refused', async () => {
    mockApi({
      'GET /clubs/1/transactions': (url) =>
        url.searchParams.has('player_id')
          ? jsonResponse(404, { message: 'Jogador 3 não encontrado.' })
          : jsonResponse(200, wholeHistory),
    })
    const user = userEvent.setup()
    render(<TransactionHistorySection clubId="1" />)
    await screen.findByRole('cell', { name: 'Otto Rangel' })

    await user.selectOptions(screen.getByLabelText('Jogador'), 'Otto Rangel')

    expect(await screen.findByRole('alert')).toHaveTextContent('Jogador 3 não encontrado.')
  })

  it('downloads the CSV of the API with the applied filters', async () => {
    const exported: string[] = []
    mockApi({
      'GET /clubs/1/transactions': () => jsonResponse(200, wholeHistory),
      'GET /clubs/1/transactions/export': (url) => {
        exported.push(url.search)
        return new Response('data_hora;tipo\n', { headers: { 'Content-Type': 'text/csv' } })
      },
    })
    const createObjectURL = vi.fn(() => 'blob:csv')
    URL.createObjectURL = createObjectURL
    URL.revokeObjectURL = vi.fn()
    const click = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {})
    const user = userEvent.setup()
    render(<TransactionHistorySection clubId="1" />)
    await screen.findByRole('cell', { name: 'Otto Rangel' })

    await user.click(screen.getByLabelText('Venda'))
    await user.click(screen.getByRole('button', { name: 'Exportar CSV' }))

    await vi.waitFor(() => expect(click).toHaveBeenCalledOnce())
    expect(exported).toEqual(['?type=VENDA'])
    expect(createObjectURL).toHaveBeenCalledOnce()
  })

  it('shows the API message when the export fails', async () => {
    mockApi({
      'GET /clubs/1/transactions': () => jsonResponse(200, wholeHistory),
      'GET /clubs/1/transactions/export': () =>
        jsonResponse(404, { message: 'Clube 1 não encontrado.' }),
    })
    const user = userEvent.setup()
    render(<TransactionHistorySection clubId="1" />)
    await screen.findByRole('cell', { name: 'Otto Rangel' })

    await user.click(screen.getByRole('button', { name: 'Exportar CSV' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Clube 1 não encontrado.')
  })
})

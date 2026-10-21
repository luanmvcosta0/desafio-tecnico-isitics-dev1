import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { jsonResponse, mockApi } from '../../test/mockApi'
import { aPlayer } from '../../test/playerFixtures'
import type { Club } from '../../types/club'
import type { PurchaseResult } from '../../types/market'
import { PurchaseDialog } from './PurchaseDialog'

const aguias: Club = {
  id: 1,
  name: 'Águias FC',
  acronym: 'AGU',
  cash: 300,
  squad_value: 0,
  net_worth: 300,
}

const purchase: PurchaseResult = {
  id: 10,
  club_id: 1,
  player_id: 2,
  player_name: 'Meia Lua',
  type: 'COMPRA',
  value: 37,
  fee: 1.85,
  penalty: 0,
  profit: null,
  cash_change: -38.85,
  created_at: '2026-11-02T10:15:00-03:00',
  contract: { purchase_value: 37, loyalty_until: '2027-03-02T10:15:00-03:00', status: 'ATIVO' },
  club_cash: 261.15,
}

function renderDialog() {
  const onPurchased = vi.fn()
  render(
    <PurchaseDialog
      player={aPlayer()}
      clubs={[aguias]}
      onPurchased={onPurchased}
      onClose={vi.fn()}
    />,
  )
  return { onPurchased, user: userEvent.setup() }
}

describe('PurchaseDialog', () => {
  it('requires the club and the loyalty end before sending', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch')
    const { user } = renderDialog()

    await user.click(screen.getByRole('button', { name: 'Confirmar compra' }))

    expect(screen.getByLabelText('Clube comprador')).toHaveAccessibleDescription(
      'Selecione o clube comprador.',
    )
    expect(
      screen.getByLabelText('Fim da fidelidade (horário de Brasília)'),
    ).toHaveAccessibleDescription('Informe a data e a hora do fim da fidelidade.')
    expect(fetchSpy).not.toHaveBeenCalled()
  })

  it('sends the club id as number and the date with -03:00, then shows the result', async () => {
    let body: unknown
    mockApi({
      'POST /players/2/buy': (_url, init) => {
        body = JSON.parse(init?.body as string)
        return jsonResponse(200, purchase)
      },
    })
    const { user, onPurchased } = renderDialog()

    await user.selectOptions(screen.getByLabelText('Clube comprador'), '1')
    await user.type(
      screen.getByLabelText('Fim da fidelidade (horário de Brasília)'),
      '2027-03-02T10:15',
    )
    await user.click(screen.getByRole('button', { name: 'Confirmar compra' }))

    const summary = within(await screen.findByLabelText('Resultado da compra'))
    expect(summary.getByText('Valor').nextSibling).toHaveTextContent('37,00')
    expect(summary.getByText('Taxa').nextSibling).toHaveTextContent('1,85')
    expect(summary.getByText('Custo total').nextSibling).toHaveTextContent('38,85')
    expect(summary.getByText('Fim da fidelidade').nextSibling).toHaveTextContent(
      '02/03/2027, 10:15',
    )
    expect(summary.getByText('Novo caixa').nextSibling).toHaveTextContent('261,15')
    expect(screen.getByRole('status')).toHaveTextContent('Meia Lua comprado por Águias FC.')
    expect(body).toEqual({ club_id: 1, loyalty_until: '2027-03-02T10:15-03:00' })
    expect(onPurchased).toHaveBeenCalledOnce()
  })

  it('shows the API message when the purchase is refused', async () => {
    const message = 'O custo da compra (50,40) passa do limite de 50% do caixa (50,00).'
    mockApi({ 'POST /players/2/buy': () => jsonResponse(409, { message }) })
    const { user, onPurchased } = renderDialog()

    await user.selectOptions(screen.getByLabelText('Clube comprador'), '1')
    await user.type(
      screen.getByLabelText('Fim da fidelidade (horário de Brasília)'),
      '2027-03-02T10:15',
    )
    await user.click(screen.getByRole('button', { name: 'Confirmar compra' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(message)
    expect(onPurchased).not.toHaveBeenCalled()
  })
})

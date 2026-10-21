import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { PlayersPage } from './PlayersPage'

describe('PlayersPage', () => {
  it('shows the values computed by the API after registering a player', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          id: 7,
          name: 'Davi Lins',
          position: 'LAT',
          agility: 60,
          age: 19,
          condition: 'APTO',
          base_value: 38.08,
          market_value: 41.89,
          pace: 72,
          shooting: 70,
          passing: 78,
          dribbling: 76,
          defending: 70,
          physical: 66,
          potential: 84,
          overall: 72,
          club_id: null,
        }),
        { status: 201, headers: { 'Content-Type': 'application/json' } },
      ),
    )
    const user = userEvent.setup()
    render(<PlayersPage />)

    await user.click(screen.getByRole('button', { name: 'Novo jogador' }))
    const values: Record<string, string> = {
      Nome: 'Davi Lins',
      'Agilidade (1–100)': '60',
      'Idade (16–45)': '19',
      Velocidade: '72',
      Finalização: '70',
      Passe: '78',
      Drible: '76',
      Defesa: '70',
      Físico: '66',
      Potencial: '84',
    }
    for (const [label, value] of Object.entries(values)) {
      await user.type(screen.getByLabelText(label), value)
    }
    await user.selectOptions(screen.getByLabelText('Posição'), 'LAT')
    await user.click(screen.getByRole('button', { name: 'Cadastrar jogador' }))

    expect(await screen.findByRole('status')).toHaveTextContent(
      'Jogador Davi Lins cadastrado com sucesso.',
    )
    const summary = screen.getByRole('region', { name: 'Resumo de Davi Lins' })
    expect(summary).toHaveTextContent('Overall72')
    expect(summary).toHaveTextContent('Valor base38,08')
    expect(summary).toHaveTextContent('Valor de mercado41,89')
    expect(summary).toHaveTextContent('ClubeLivre')
    expect(screen.queryByRole('form', { name: 'Cadastro de jogador' })).not.toBeInTheDocument()
  })
})
